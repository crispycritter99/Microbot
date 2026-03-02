package net.runelite.client.plugins.microbot.aiofighterretro.combat;

import lombok.SneakyThrows;
import net.runelite.api.Actor;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aiofighterretro.AIOFighterConfig;
import net.runelite.client.plugins.microbot.aiofighterretro.AIOFighterPluginLocal;
import net.runelite.client.plugins.microbot.aiofighterretro.enums.AttackStyle;
import net.runelite.client.plugins.microbot.aiofighterretro.enums.AttackStyleMapper;
import net.runelite.client.plugins.microbot.aiofighterretro.enums.State;
import net.runelite.client.plugins.microbot.api.actor.Rs2ActorModel;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcCache;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.ActorModel;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldArea;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.item.Rs2EnsouledHead;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
//import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
//import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.api.npc.models.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import org.slf4j.event.Level;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static net.runelite.api.gameval.VarbitID.*;

public class AttackNpcScript extends Script {

    public static Actor currentNpc = null;
    public static AtomicReference<List<Rs2NpcModel>> filteredAttackableNpcs = new AtomicReference<>(new ArrayList<>());
    public static Rs2WorldArea attackableArea = null;
    public static volatile int cachedTargetNpcIndex = -1;
    private boolean messageShown = false;
    private int noNpcCount = 0;
    @Inject
    private Rs2NpcCache npcCache;
    public static Rs2NpcCache cache = Microbot.getRs2NpcCache();
    public static void skipNpc() {
        currentNpc = null;
    }

    @SneakyThrows
    public void run(AIOFighterConfig config) {
        try {
            Rs2NpcManager.loadJson();
            Rs2Antiban.resetAntibanSettings();
            Rs2Antiban.antibanSetupTemplates.applyCombatSetup();
            Rs2Antiban.setActivityIntensity(ActivityIntensity.EXTREME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run() || !config.toggleCombat())
                    return;

                if (config.centerLocation().distanceTo(Rs2Player.getWorldLocation()) < config.attackRadius() &&
                        !config.centerLocation().equals(new WorldPoint(0, 0, 0)) && AIOFighterPluginLocal.getState() != State.BANKING) {
                    if (ShortestPathPlugin.getPathfinder() != null)
                        Rs2Walker.setTarget(null);
                    AIOFighterPluginLocal.setState(State.IDLE);
                }

                if (config.state().equals(State.BANKING) || config.state().equals(State.WALKING))
                    return;
                cache = Microbot.getRs2NpcCache();
                if (config.reanimateEnsouledHeads()) {
//                    System.out.println("diddy 1");
                    Rs2EnsouledHead head = Rs2EnsouledHead.getReanimatableHead();
//                    System.out.println(head);
                    if (head != null) {
                        boolean prevPause = Microbot.pauseAllScripts.getAndSet(true);
                        try {
                            if (head.reanimate()) {
//                                sleepUntil(() -> Rs2Npc.getNpcsForPlayer(Rs2EnsouledHead::isNpcReanimated).findAny().isPresent(), 15000);
                                sleepUntil(() -> cache.query().where(npc-> npc.getName()!=null&&npc.getName().toLowerCase().contains("reanimated")).nearest().isInteractingWithPlayer(), 15000);
                            }
                        } finally {
                            Microbot.pauseAllScripts.set(prevPause);
                        }
                    }

                    Rs2NpcModel reanimated = cache.query().where(npc-> npc.getName()!=null&&npc.getName().toLowerCase().contains("reanimated")).nearest();
                    if (reanimated != null&&!Rs2Player.getInteracting().getName().equals(reanimated.getName())) {
                        reanimated.click("attack");
                        return;
                    }
                    else if (Rs2Player.isInteracting()&&Rs2Player.getInteracting() == reanimated){
                        return;
                    }
                }
//                System.out.println("diddy 2");
                if(config.useThralls()&&Rs2Player.isInCombat()&&!Rs2Magic.isThrallActive()&&Rs2Inventory.hasItem("Book of the dead")) {
                    Rs2Magic.cast(config.ThrallType().toMagicAction());
                }

                attackableArea = new Rs2WorldArea(config.centerLocation().toWorldArea());
                attackableArea = attackableArea.offset(config.attackRadius());
                List<String> npcsToAttack = Arrays.stream(config.attackableNpcs().split(","))
                        .map(x -> x.trim().toLowerCase())
                        .collect(Collectors.toList());
//                cache.query().where(npc -> npc.getWorldLocation().distanceTo(config.centerLocation()) <= config.attackRadius())
//                                .where(npc -> npc.getName() != null && !npcsToAttack.isEmpty() && npcsToAttack.stream().anyMatch(npc.getName()::equalsIgnoreCase))
//                                        .
//                filteredAttackableNpcs.set(
//                        Rs2Npc.getAttackableNpcs(config.attackReachableNpcs())
//                                .filter(npc -> npc.getWorldLocation().distanceTo(config.centerLocation()) <= config.attackRadius())
//                                .filter(npc -> npc.getName() != null && !npcsToAttack.isEmpty() && npcsToAttack.stream().anyMatch(npc.getName()::equalsIgnoreCase))
//                                .sorted(Comparator.comparingInt((Rs2NpcModel npc) -> npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
//                                        .thenComparingInt(npc -> Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
//                                .collect(Collectors.toList())
//                );
//                final List<Rs2NpcModel> attackableNpcs = new ArrayList<>();
                System.out.println("diddy 3");
//                for (var attackableNpc : filteredAttackableNpcs.get()) {
//                    if (attackableNpc == null || attackableNpc.getName() == null) continue;
//                    for (var npcToAttack : npcsToAttack) {
//                        if (npcToAttack.equalsIgnoreCase(attackableNpc.getName())) {
//                            attackableNpcs.add(attackableNpc);
//                        }
//                    }
//                }
//                filteredAttackableNpcs.set(attackableNpcs);

                final List<Rs2NpcModel> attackableNpcs =
                        cache
                                .query()
                                .where(npc -> npc.getHealthPercentage() > 0)
                                .where(npc -> !npc.isDead())
                                .where(npc ->  !npc.isInteracting()|| Objects.equals(npc.getInteracting(), Microbot.getClient().getLocalPlayer()))
                                .where(npc -> npc.getWorldLocation()
                                        .distanceTo(config.centerLocation()) <= config.attackRadius())
                                .where(npc -> npc.getName() != null
                                        && !npcsToAttack.isEmpty()
                                        &&npcsToAttack.stream().anyMatch(npc.getName()::equalsIgnoreCase))
                                .where(npc -> !config.attackReachableNpcs()
                                        //|| new Rs2WorldPoint(Microbot.getClient().getLocalPlayer().getWorldLocation()).distanceToPath(npc.getWorldLocation()) < Integer.MAX_VALUE//
                                )
                                .toList();
                System.out.println("diddy 3.1");
                attackableNpcs.sort(
                        Comparator
                                .comparingInt((Rs2NpcModel npc) ->
                                        npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
                                .thenComparingInt(value ->
                                        value.getLocalLocation().distanceTo(
                                                Microbot.getClient().getLocalPlayer().getLocalLocation()))
                );
                System.out.println("diddy 3.2");
                filteredAttackableNpcs.set(attackableNpcs);
                // Check if we should pause while looting is happening
                if (Microbot.pauseAllScripts.get()) {
                    return; // Don't attack while looting
                }
                System.out.println("diddy 4");
                // Check if we need to update our cached target (but not while waiting for loot)
                if (!AIOFighterPluginLocal.isWaitingForLoot()) {
                    Actor currentInteracting = Rs2Player.getInteracting();
                    if (currentInteracting instanceof Rs2NpcModel) {
                        Rs2NpcModel npc = (Rs2NpcModel) currentInteracting;
                        // Update our cached target to who we're fighting
                        if (npc.getHealthRatio() > 0 && !npc.isDead()) {
                            cachedTargetNpcIndex = npc.getIndex();
                        }
                    }
                }
//                System.out.println("diddy 5");
                // Check if our cached target died
                if (config.toggleWaitForLoot() && !AIOFighterPluginLocal.isWaitingForLoot() && cachedTargetNpcIndex != -1) {
                    // Find the NPC by index using Rs2 API
//                    Rs2NpcModel cachedNpcModel = Rs2Npc.getNpcByIndex(cachedTargetNpcIndex);
                    Rs2NpcModel cachedNpcModel=cache.query().where(npc -> npc.getIndex()==cachedTargetNpcIndex).nearest();;
                    if (cachedNpcModel != null && (cachedNpcModel.isDead() || (cachedNpcModel.getHealthRatio() == 0 && cachedNpcModel.getHealthScale() > 0))) {
                        AIOFighterPluginLocal.setWaitingForLoot(true);
                        AIOFighterPluginLocal.setLastNpcKilledTime(System.currentTimeMillis());
                        Microbot.status = "Waiting for loot...";
                        Microbot.log("NPC died, waiting for loot...");
                        cachedTargetNpcIndex = -1;
                        return;
                    }
                }
//                System.out.println("diddy 6");
                // Check if we're waiting for loot
                if (config.toggleWaitForLoot() && AIOFighterPluginLocal.isWaitingForLoot()) {
                    long timeSinceKill = System.currentTimeMillis() - AIOFighterPluginLocal.getLastNpcKilledTime();
                    int timeoutMs = config.lootWaitTimeout() * 1000;
                    if (timeSinceKill >= timeoutMs) {
                        // Timeout reached, resume combat
                        AIOFighterPluginLocal.clearWaitForLoot("Loot wait timeout reached, resuming combat");
                        cachedTargetNpcIndex = -1; // Clear cached NPC on timeout
                    } else {
                        // Still waiting for loot, don't attack
                        int secondsLeft = (int) Math.max(1, TimeUnit.MILLISECONDS.toSeconds(timeoutMs - timeSinceKill));
                        Microbot.status = "Waiting for loot... " + secondsLeft + "s";
                        return;
                    }
                }
//                System.out.println("diddy 7");
                if (config.toggleCenterTile() && config.centerLocation().getX() == 0
                        && config.centerLocation().getY() == 0) {
                    if (!messageShown) {
                        Microbot.showMessage("Please set a center location");
                        messageShown = true;
                    }
                    return;
                }
                messageShown = false;
//                System.out.println("diddy 8");
                if (Rs2AntibanSettings.antibanEnabled && Rs2AntibanSettings.actionCooldownChance > 0) {
                    if (Rs2AntibanSettings.actionCooldownActive) {
                        AIOFighterPluginLocal.setState(State.COMBAT);
                        System.out.println("diddy 8");
                        handleItemOnNpcToKill(config);
                        return;
                    }
                }
//                else {
//                    if (Rs2Combat.inCombat()) {
//                        AIOFighterPluginLocal.setState(State.COMBAT);
//                        handleItemOnNpcToKill(config);
//                        return;
//                    }
//                }
//                if (Rs2Player.isInteracting())return;
                if (!attackableNpcs.isEmpty()) {
                    noNpcCount = 0;

                    Rs2NpcModel npc = attackableNpcs.stream().findFirst().orElse(null);

                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    npc.click("attack");
                    Microbot.status = "Attacking " + npc.getName();
                    sleep(600);
                    Rs2Antiban.actionCooldown();
                    //sleepUntil(Rs2Player::isInteracting, 1000);
                    System.out.println("diddy 9");
                    if (config.togglePrayer()) {
                        if (!config.toggleQuickPray()) {
                            AttackStyle attackStyle = AttackStyleMapper
                                    .mapToAttackStyle(Rs2NpcManager.getAttackStyle(npc.getId()));
                            if (attackStyle != null) {
                                switch (attackStyle) {
                                    case MAGE:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                                        break;
                                    case MELEE:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
                                        break;
                                    case RANGED:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                                        break;
                                }
                            }
                        } else {
                            Rs2Prayer.toggleQuickPrayer(true);
                        }
                    }


                } else {
                    if (Rs2Player.getWorldLocation().isInArea(attackableArea)) {
                        Microbot.log(Level.INFO, "No attackable NPC found");
                        noNpcCount++;
                        if (noNpcCount > 60 && config.slayerMode()) {
                            Microbot.log(Level.INFO, "No attackable NPC found for 60 ticks, resetting slayer task");
                            AIOFighterPluginLocal.addBlacklistedSlayerNpcs(Rs2Slayer.slayerTaskMonsterTarget);
                            noNpcCount = 0;
                            SlayerScript.reset();
                        }
                    } else {
                        Rs2Walker.walkTo(config.centerLocation(), 0);
                        AIOFighterPluginLocal.setState(State.WALKING);
                    }

                }
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }


    /**
     * item on npcs that need to kill like rockslug
     */
    private void handleItemOnNpcToKill(AIOFighterConfig config) {
//        Rs2NpcModel npc = Rs2Npc.getNpcsForPlayer(ActorModel::isDead).findFirst().orElse(null);
        Rs2NpcModel npc = cache.query().where(Rs2ActorModel::isDead).nearest();
        List<String> lizardVariants = new ArrayList<>(Arrays.asList("Lizard", "Desert Lizard", "Small Lizard"));
        if (npc == null) return;
        if (Microbot.getVarbitValue(SLAYER_AUTOKILL_DESERTLIZARDS) == 0 && lizardVariants.contains(npc.getName()) && npc.getHealthRatio() < 5) {
            if (Rs2Bank.isOpen()) return;
            if (!Rs2Inventory.use(ItemID.SLAYER_ICY_WATER)) return ;
            sleep(100);
            if (Rs2Inventory.isItemSelected()){
                npc.click();
            }
            Rs2Player.waitForAnimation();
        } else if (Microbot.getVarbitValue(SLAYER_AUTOKILL_ROCKSLUGS) == 0 && npc.getName().equalsIgnoreCase("rockslug") && npc.getHealthRatio() < 5) {
            if (Rs2Bank.isOpen()) return;
            if (!Rs2Inventory.use(ItemID.SLAYER_BAG_OF_SALT)) return;
            sleep(100);
            if (Rs2Inventory.isItemSelected()){
                npc.click();
            }
            Rs2Player.waitForAnimation();
        } else if (Microbot.getVarbitValue(SLAYER_AUTOKILL_GARGOYLES) == 0 && npc.getName().equalsIgnoreCase("gargoyle") && npc.getHealthRatio() < 3) {
            if (Rs2Bank.isOpen()) return ;
            if (!Rs2Inventory.use(ItemID.SLAYER_ROCK_HAMMER)) return;
            sleep(100);
            if (Rs2Inventory.isItemSelected()){
                npc.click();
            }
            Rs2Player.waitForAnimation();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
