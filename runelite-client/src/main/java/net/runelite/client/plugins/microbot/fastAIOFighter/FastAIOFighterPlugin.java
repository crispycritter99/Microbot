package net.runelite.client.plugins.microbot.fastAIOFighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MicrobotApi;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@PluginDescriptor(
        name = "<html>[<font color=#93652a>K</font>] FastAIOFighter",
        description = "Fast configurable AIO fighter",
        tags = {"combat", "fighter", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class FastAIOFighterPlugin extends Plugin {
    private static final String SLAYER_RESTRICTION_MESSAGE = "wants you to stick to your slayer assignments";
    private static final String ATTACK = "Attack";
    private static final String ADD_TO_ATTACK_LIST = "Add to attack list";
    private static final String REMOVE_FROM_ATTACK_LIST = "Remove from attack list";
    @Inject
    private FastAIOFighterConfig config;
    @Inject
    private ConfigManager configManager;
    @Provides
    FastAIOFighterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FastAIOFighterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FastAIOFighterOverlay exampleOverlay;

    @Inject
    FastAIOFighterScript exampleScript;
public static int startingDaeyaltShard = 0;
public static long initialTime=System.currentTimeMillis();
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        startingDaeyaltShard= Rs2Inventory.itemQuantity("Daeyalt shard");

        exampleScript.updateAttackableNpcs(config.attackableNpcs());
        exampleScript.run(config);
    }

    protected void shutDown() {
        exampleScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (FastAIOFighterConfig.GROUP.equals(event.getGroup())
                && FastAIOFighterConfig.ATTACKABLE_NPCS_KEY.equals(event.getKey())) {
            exampleScript.updateAttackableNpcs(event.getNewValue());
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!ATTACK.equals(event.getOption())) {
            return;
        }

        String npcName = getNpcNameFromMenuEntry(event.getTarget());
        if (npcName.isEmpty()) {
            return;
        }

        boolean listed = parseAttackableNpcs(config.attackableNpcs()).stream()
                .anyMatch(name -> name.equalsIgnoreCase(npcName));
        String option = listed ? REMOVE_FROM_ATTACK_LIST : ADD_TO_ATTACK_LIST;

        Microbot.getClient().createMenuEntry(1)
                .setOption(option)
                .setTarget(event.getTarget())
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(entry -> updateAttackList(entry, npcName));
    }

    private void updateAttackList(MenuEntry entry, String npcName) {
        List<String> names = parseAttackableNpcs(config.attackableNpcs());

        if (ADD_TO_ATTACK_LIST.equals(entry.getOption())) {
            if (names.stream().noneMatch(name -> name.equalsIgnoreCase(npcName))) {
                names.add(npcName);
            }
        } else if (REMOVE_FROM_ATTACK_LIST.equals(entry.getOption())) {
            names.removeIf(name -> name.equalsIgnoreCase(npcName));
        } else {
            return;
        }

        configManager.setConfiguration(
                FastAIOFighterConfig.GROUP,
                FastAIOFighterConfig.ATTACKABLE_NPCS_KEY,
                String.join(", ", names)
        );
    }

    static List<String> parseAttackableNpcs(String value) {
        List<String> names = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return names;
        }

        for (String entry : value.split(",")) {
            String name = entry.trim();
            if (!name.isEmpty() && names.stream().noneMatch(existing -> existing.equalsIgnoreCase(name))) {
                names.add(name);
            }
        }
        return names;
    }

    private static String getNpcNameFromMenuEntry(String menuTarget) {
        return Text.removeTags(menuTarget).replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        String message = event.getMessage();
        if (message != null && message.toLowerCase(Locale.ROOT).contains(SLAYER_RESTRICTION_MESSAGE)) {
            exampleScript.shutdown();
            Microbot.status = "Stopped: target requires a Slayer assignment";
        }
    }
}
