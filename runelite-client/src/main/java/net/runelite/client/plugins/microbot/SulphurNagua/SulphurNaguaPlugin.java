package net.runelite.client.plugins.microbot.SulphurNagua;

import com.google.inject.Provides;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.SulphurNagua.enums.State;
import net.runelite.client.plugins.microbot.aiofighter.AIOFighterConfig;
import net.runelite.client.plugins.microbot.aiofighter.bank.BankerScript;
import net.runelite.client.plugins.microbot.aiofighter.cannon.CannonScript;
import net.runelite.client.plugins.microbot.aiofighter.combat.*;
import net.runelite.client.plugins.microbot.aiofighter.combat.AttackNpcScript;
import net.runelite.client.plugins.microbot.aiofighter.combat.PotionManagerScript;
import net.runelite.client.plugins.microbot.aiofighter.loot.LootScript;
import net.runelite.client.plugins.microbot.aiofighter.safety.SafetyScript;
import net.runelite.client.plugins.microbot.aiofighter.skill.AttackStyleScript;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=red>Neon</font>] " + "Sulphur Nagua",
        description = "Moons of Peril bot for blood moon only",
        tags = {"microbot", "Moons of Peril", "boss"},
        enabledByDefault = false
)
@Slf4j
public class SulphurNaguaPlugin extends Plugin {
    @Inject
    private SulphurNaguaConfig config;
    @Provides
    SulphurNaguaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SulphurNaguaConfig.class);
    }
    @Setter
    public static int cooldown = 0;
    private final CannonScript cannonScript = new CannonScript();
    private final net.runelite.client.plugins.microbot.aiofighter.combat.AttackNpcScript attackNpc = new AttackNpcScript();

    private final FoodScript foodScript = new FoodScript();
    private final PrayerPotionScript prayerPotionScript = new PrayerPotionScript();
    private final LootScript lootScript = new LootScript();
    private final SafeSpot safeSpotScript = new SafeSpot();
    private final FlickerScript flickerScript = new FlickerScript();
    private final UseSpecialAttackScript useSpecialAttackScript = new UseSpecialAttackScript();

    private final BuryScatterScript buryScatterScript = new BuryScatterScript();
    private final AttackStyleScript attackStyleScript = new AttackStyleScript();
    private final BankerScript bankerScript = new BankerScript();
    private final PrayerScript prayerScript = new PrayerScript();
    private final HighAlchScript highAlchScript = new HighAlchScript();
    private final net.runelite.client.plugins.microbot.aiofighter.combat.PotionManagerScript potionManagerScript = new PotionManagerScript();
    private final SafetyScript safetyScript = new SafetyScript();
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SulphurNaguaOverlay sulphurNaguaOverlay;
    @Inject
    SulphurNaguaScript sulphurNaguaScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(sulphurNaguaOverlay);
        }
        SulphurNaguaScript.state = State.CHAMBER;
        sulphurNaguaScript.run(config);
        Microbot.pauseAllScripts = false;
        cooldown = 0;

        attackNpc.run((AIOFighterConfig) config);
        //combatPotion.run(config);

        //prayerPotionScript.run(config);


        //antiPoisonScript.run(config);

        attackStyleScript.run((AIOFighterConfig) config);

        prayerScript.run((AIOFighterConfig) config);

        potionManagerScript.run((AIOFighterConfig) config);

        Microbot.getSpecialAttackConfigs()
                .setSpecialAttack(true);
    }

    protected void shutDown() {
        sulphurNaguaScript.shutdown();
        overlayManager.remove(sulphurNaguaOverlay);
    }
}
