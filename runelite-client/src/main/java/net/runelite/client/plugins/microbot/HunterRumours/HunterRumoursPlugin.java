package net.runelite.client.plugins.microbot.HunterRumours;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.falconry.FalconryConfig;
import net.runelite.client.plugins.microbot.falconry.FalconryScript;
import net.runelite.client.plugins.microbot.microhunter.AutoHunterConfig;
import net.runelite.client.plugins.microbot.microhunter.scripts.AutoChinScript;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalConfig;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalScript;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
@PluginDescriptor(
        name = PluginDescriptor.TaFCat + "Hunter Rumours",
        description = "Automatically runs the correct hunter script for your current rumour.",
        tags = {"hunter", "rumours", "falconry", "salamander", "chinchompa", "microbot"},
        version = "1.0.0",
        minClientVersion = "2.0.13",
        cardUrl = "",
        iconUrl = "",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL
)
public class HunterRumoursPlugin extends Plugin
{
    @Inject
    private ConfigManager configManager;

    // Hunter Rumours config (this plugin)
    @Inject
    private HunterRumoursConfig hunterRumoursConfig;

    @Provides
    HunterRumoursConfig provideHunterRumoursConfig(ConfigManager configManager)
    {
        return configManager.getConfig(HunterRumoursConfig.class);
    }

    // Falconry
    @Inject
    private FalconryConfig falconryConfig;

    @Provides
    FalconryConfig provideFalconryConfig(ConfigManager configManager)
    {
        return configManager.getConfig(FalconryConfig.class);
    }

    @Inject
    private FalconryScript falconryScript;

    // AutoHunter / Chins
    @Inject
    private AutoHunterConfig autoHunterConfig;

    @Provides
    AutoHunterConfig provideAutoHunterConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AutoHunterConfig.class);
    }

    @Inject
    private AutoChinScript autoChinScript;

    // Salamanders
    @Inject
    private SalamanderLocalConfig salamanderConfig;

    @Provides
    SalamanderLocalConfig provideSalamanderConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SalamanderLocalConfig.class);
    }

    // Salamander script (uses stub plugin internally in HunterRumoursScript)
    @Inject
    private SalamanderLocalScript salamanderScript;

    private HunterRumoursScript hunterRumoursScript;

    @Override
    protected void startUp() throws AWTException
    {
        log.info("Hunter Rumours plugin started");

        // NOTE: do NOT call any of the individual plugins' startUp() here
        // (FalconryPlugin, AutoHunterLocalPlugin, SalamanderLocalPlugin),
        // or you will end up double-running scripts.

        hunterRumoursScript = new HunterRumoursScript(
                hunterRumoursConfig,
                falconryScript,
                falconryConfig,
                autoChinScript,
                autoHunterConfig,
                salamanderScript,
                salamanderConfig
        );
        hunterRumoursScript.run();
    }

    @Override
    protected void shutDown()
    {
        log.info("Hunter Rumours plugin stopped");

        if (hunterRumoursScript != null)
        {
            hunterRumoursScript.shutdown();
            hunterRumoursScript = null;
        }
    }
}
