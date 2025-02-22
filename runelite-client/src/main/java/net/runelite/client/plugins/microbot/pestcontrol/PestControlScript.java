package net.runelite.client.plugins.microbot.pestcontrol;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.pestcontrol.Portal;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isQuickPrayerEnabled;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.distanceToRegion;
import static net.runelite.client.plugins.pestcontrol.Portal.*;

public class PestControlScript extends Script {
    public static double version = 2.1;

    boolean walkToCenter = false;
    PestControlConfig config;

    private static final Set<Integer> SPINNER_IDS = ImmutableSet.of(
            NpcID.SPINNER,
            NpcID.SPINNER_1710,
            NpcID.SPINNER_1711,
            NpcID.SPINNER_1712,
            NpcID.SPINNER_1713
    );

    private static final Set<Integer> BRAWLER_IDS = ImmutableSet.of(
            NpcID.BRAWLER,
            NpcID.BRAWLER_1736,
            NpcID.BRAWLER_1738,
            NpcID.BRAWLER_1737,
            NpcID.BRAWLER_1735
    );

    final int distanceToPortal = 8;
    public static final boolean DEBUG = false;

    public static List<Portal> portals = List.of(PURPLE, BLUE, RED, YELLOW);

    private void resetPortals() {
        for (Portal portal : portals) {
            portal.setHasShield(true);
        }
    }

    public boolean run(PestControlConfig config) {
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                final boolean isInPestControl = Microbot.getClient().getWidget(WidgetInfo.PEST_CONTROL_BLUE_SHIELD) != null;
                final boolean isInBoat = Microbot.getClient().getWidget(WidgetInfo.PEST_CONTROL_BOAT_INFO) != null;
                if (isInPestControl) {
                    if (!isQuickPrayerEnabled() && Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER) != 0 && config.quickPrayer()) {
                        final Widget prayerOrb = Rs2Widget.getWidget(ComponentID.MINIMAP_QUICK_PRAYER_ORB);
                        if (prayerOrb != null) {
                            Microbot.getMouse().click(prayerOrb.getCanvasLocation());
                            sleep(1000, 1500);
                        }
                    }
                    if (!walkToCenter) {
                        WorldPoint worldPoint = WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), 32, 17, Microbot.getClient().getPlane());
                        Rs2Walker.walkWithState(worldPoint, 3);
                        if (worldPoint.distanceTo(Rs2Player.getWorldLocation()) > 4) {
                            return;
                        } else {
                            walkToCenter = true;
                        }
                    }

                    Rs2Combat.setSpecState(true, config.specialAttackPercentage() * 10);
                    Widget activity = Rs2Widget.getWidget(26738700); //145 = 100%
                    if (activity != null && activity.getChild(0).getWidth() <= 20 && !Rs2Combat.inCombat()) {
                        Optional<Rs2NpcModel> attackableNpc = Rs2Npc.getAttackableNpcs().findFirst();
//                        Microbot.log(attackableNpc.toString()+"");
                        attackableNpc.ifPresent(rs2NpcModel -> Rs2Npc.interact(rs2NpcModel.getId(),"attack"));
                        return;
                    }

//                    var brawler = Rs2Npc.getNpc("brawler");
//                    if (brawler != null && brawler.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < 3) {
//                        Rs2Npc.interact(brawler,"attack");
//                        sleepUntil(() -> !Rs2Combat.inCombat());
//                        return;
//                    }

                    if (Microbot.getClient().getLocalPlayer().isInteracting())
                        return;



                    if (handleAttack(PestControlNpc.BRAWLER, 1)
                            || handleAttack(PestControlNpc.PORTAL, 1)
                            || handleAttack(PestControlNpc.SPINNER, 1)) {
                        return;
                    }

                    if (handleAttack(PestControlNpc.BRAWLER, 2)
                            || handleAttack(PestControlNpc.PORTAL, 2)
                            || handleAttack(PestControlNpc.SPINNER, 2)) {
                        return;
                    }
                    if (handleAttack(PestControlNpc.BRAWLER, 3)
                            || handleAttack(PestControlNpc.PORTAL, 3)
                            || handleAttack(PestControlNpc.SPINNER, 3)) {
                        return;
                    }
                    Rs2NpcModel portal = Arrays.stream(Rs2Npc.getPestControlPortals()).findFirst().orElse(null);
                    if (portal != null) {
                        if (Rs2Npc.interact(portal.getId(),"attack")) {
                            sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                        }
                    } else {
                        if (!Microbot.getClient().getLocalPlayer().isInteracting()) {
                            Optional<Rs2NpcModel> attackableNpc = Rs2Npc.getAttackableNpcs().findFirst();
//                            Microbot.log(attackableNpc.toString()+"");
                            attackableNpc.ifPresent(rs2NpcModel -> Rs2Npc.interact(rs2NpcModel.getId(),"attack"));
                        }
                    }

                } else {
                    Rs2Walker.setTarget(null);
                    resetPortals();
                    walkToCenter = false;
                    sleep(Rs2Random.between(1600, 1800));
                    if (!isInBoat) {
                        if (Microbot.getClient().getLocalPlayer().getCombatLevel() >= 100) {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_25632);
                        } else if (Microbot.getClient().getLocalPlayer().getCombatLevel() >= 70) {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_25631);
                        } else {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_14315);
                        }
                        sleepUntil(() -> Microbot.getClient().getWidget(WidgetInfo.PEST_CONTROL_BOAT_INFO) != null, 3000);
                    } else {
                        if (config.alchInBoat() && !config.alchItem().equalsIgnoreCase("")) {
                            Rs2Magic.alch(config.alchItem());
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Microbot.log(ex.getMessage());
            }
        }, 0, 300, TimeUnit.MILLISECONDS);
        return true;
    }

    public void shutDown() {
        super.shutdown();
    }

    private boolean handleAttack(PestControlNpc npcType, int priority) {
        if (priority == 1) {
            if (config.Priority1() == npcType) {
                if (npcType == PestControlNpc.BRAWLER) {
                    return attackBrawler();
                } else if (npcType == PestControlNpc.PORTAL) {
                    return attackPortals();
                } else if (npcType == PestControlNpc.SPINNER) {
//                    Microbot.log("priority attack spinner");
                    return attackSpinner();
                }
            }
        } else if (priority == 2) {
            if (config.Priority2() == npcType) {
                if (npcType == PestControlNpc.BRAWLER) {
                    return attackBrawler();
                } else if (npcType == PestControlNpc.PORTAL) {
                    return attackPortals();
                } else if (npcType == PestControlNpc.SPINNER) {
                    return attackSpinner();
                }
            }
        } else {
            if (config.Priority2() == npcType) {
                if (npcType == PestControlNpc.BRAWLER) {
                    return attackBrawler();
                } else if (npcType == PestControlNpc.PORTAL) {
                    return attackPortals();
                } else if (npcType == PestControlNpc.SPINNER) {
                    return attackSpinner();
                }
            }
        }

        return false;
    }

    public Portal getClosestAttackablePortal() {
        List<Pair<Portal, Integer>> distancesToPortal = new ArrayList();
        for (Portal portal : portals) {
            if (!portal.isHasShield() && !portal.getHitPoints().getText().trim().equals("0")) {
                distancesToPortal.add(Pair.of(portal, distanceToRegion(portal.getRegionX(), portal.getRegionY())));
            }
        }
        if (distancesToPortal.size()!=0) {
            Pair<Portal, Integer> closestPortal = distancesToPortal.stream().min(Map.Entry.comparingByValue()).get();

            return closestPortal.getKey();
        }
        return null;
    }

    private static boolean attackPortal() {
        if (!Microbot.getClient().getLocalPlayer().isInteracting()) {
            Rs2NpcModel npcPortal = Rs2Npc.getNpc("portal");
            if (npcPortal == null) return false;
            NPCComposition npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcDefinition(npcPortal.getId()));
            if (Arrays.stream(npc.getActions()).anyMatch(x -> x != null && x.equalsIgnoreCase("attack"))) {
                if (npcPortal == null) return false;
                if (Rs2Combat.inCombat()) return false;
                if (npcPortal.isInteracting() && npcPortal.getInteracting() != Microbot.getClient().getLocalPlayer() && !Rs2Player.isInMulti())
                    return false;
                if (Rs2Npc.interact(npcPortal,"attack")){Microbot.log("portal was attacked");};
            } else {
                return false;
            }
        }
        return false;
    }

//Microbot.log("attacking portal");
    //                Microbot.log(""+(npcPortal == null));
//                Microbot.log(""+!Rs2Npc.hasLineOfSight(npcPortal));
//                Microbot.log(""+(npcPortal.isInteracting() && npcPortal.getInteracting() != Microbot.getClient().getLocalPlayer() && !Rs2Player.isInMulti()));
    private boolean attackPortals() {
        Portal closestAttackablePortal = getClosestAttackablePortal();
        for (Portal portal : portals) {
            if (!portal.isHasShield() && !portal.getHitPoints().getText().trim().equals("0") && closestAttackablePortal == portal) {
                if (!Rs2Walker.isCloseToRegion(distanceToPortal, portal.getRegionX(), portal.getRegionY())) {
                    Rs2Walker.walkTo(WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), portal.getRegionX(), portal.getRegionY(), Microbot.getClient().getPlane()), 10);
                    attackPortal();
                } else {
                    attackPortal();
                }
                return true;
            }
        }
        return false;
    }

    private boolean attackSpinner() {
        for (int spinner : SPINNER_IDS) {
            Rs2NpcModel npcSpinner = Rs2Npc.getNpc(spinner);
            if (npcSpinner != null) {
//                                Microbot.log(""+(npcSpinner == null));
//                Microbot.log(""+!Rs2Npc.hasLineOfSight(npcSpinner));
//                Microbot.log(""+(npcSpinner.isInteracting() && npcSpinner.getInteracting() != Microbot.getClient().getLocalPlayer() && !Rs2Player.isInMulti()));
                if (npcSpinner == null) return false;
                if (Rs2Combat.inCombat()) return false;
                if (npcSpinner.isInteracting() && npcSpinner.getInteracting() != Microbot.getClient().getLocalPlayer() && !Rs2Player.isInMulti())
                    return false;
//            if (Rs2Npc.attack(spinner)) {
                if (Rs2Npc.interact(npcSpinner, "attack")) {
                    Microbot.log("attacking spinner");
                    sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackBrawler() {
        for (int brawler : BRAWLER_IDS) {
            if (Rs2Npc.interact(brawler, "attack")) {
                sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                return true;
            }
        }
        return false;
    }
}
