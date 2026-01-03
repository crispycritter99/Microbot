package net.runelite.client.plugins.microbot.salvaging;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.GameObject;
import net.runelite.api.gameval.ObjectID;
import com.google.common.collect.ImmutableMap;
import net.runelite.client.util.Text;

import java.awt.Color;
import java.util.*;
import javax.inject.Inject;
import java.awt.*;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft+ "Salvaging",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class SalvagingPlugin extends Plugin {
    @Inject
    private SalvageConfig config;
    @Provides
    SalvageConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SalvageConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SalvagingOverlay salvagingOverlay;

    @Inject
    SalvagingScript salvagingScript;
    private static final int SIZE_SALVAGEABLE_AREA = 15;
    public static boolean shouldloot = false;
    public static final Map<Integer, Integer> SALVAGE_LEVEL_REQ = ImmutableMap.<Integer, Integer>builder()
            .put(ObjectID.SAILING_SMALL_SHIPWRECK, 15)
            .put(ObjectID.SAILING_FISHERMAN_SHIPWRECK, 26)
            .put(ObjectID.SAILING_BARRACUDA_SHIPWRECK, 35)
            .put(ObjectID.SAILING_LARGE_SHIPWRECK, 53)
            .put(ObjectID.SAILING_PIRATE_SHIPWRECK, 64)
            .put(ObjectID.SAILING_MERCENARY_SHIPWRECK, 73)
            .put(ObjectID.SAILING_FREMENNIK_SHIPWRECK, 80)
            .put(ObjectID.SAILING_MERCHANT_SHIPWRECK, 87)
            .build();

    private static final Map<Integer, Integer> STUMP_LEVEL_REQ = ImmutableMap.<Integer, Integer>builder()
            .put(ObjectID.SAILING_SMALL_SHIPWRECK_STUMP, 15)
            .put(ObjectID.SAILING_FISHERMAN_SHIPWRECK_STUMP, 26)
            .put(ObjectID.SAILING_BARRACUDA_SHIPWRECK_STUMP, 35)
            .put(ObjectID.SAILING_LARGE_SHIPWRECK_STUMP, 53)
            .put(ObjectID.SAILING_PIRATE_SHIPWRECK_STUMP, 64)
            .put(ObjectID.SAILING_MERCENARY_SHIPWRECK_STUMP, 73)
            .put(ObjectID.SAILING_FREMENNIK_SHIPWRECK_STUMP, 80)
            .put(ObjectID.SAILING_MERCHANT_SHIPWRECK_STUMP, 87)
            .build();

    public static Set<GameObject> wrecks = new HashSet<>();
    private final Set<GameObject> stumps = new HashSet<>();

    private boolean activeWrecks;
    private Color activeColour;
    private boolean inactiveWrecks;
    private Color inactiveColour;
    private boolean highLevelWrecks;
    private Color highLevelColour;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(salvagingOverlay);
        }
        salvagingScript.run(config);

//        wrecks.clear();
//        stumps.clear();
    }

    protected void shutDown() {
        salvagingScript.shutdown();
        overlayManager.remove(salvagingOverlay);
        wrecks.clear();
        stumps.clear();
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
    public void onChatMessage(ChatMessage chatMessage) {
//        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
//            return;
//        }

        String message = chatMessage.getMessage();

        // Lock the plugin when the boss fight begins
        if (message.contains("cannot salvage")) {
            shouldloot=true;
            SalvagingScript.chestiterate=5;
        }

    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned e)
    {
        if (SALVAGE_LEVEL_REQ.containsKey(e.getGameObject().getId()))
        {
            wrecks.add(e.getGameObject());
        }
        else if (STUMP_LEVEL_REQ.containsKey(e.getGameObject().getId()))
        {
            stumps.add(e.getGameObject());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned e)
    {
        wrecks.remove(e.getGameObject());
        stumps.remove(e.getGameObject());
    }

    @Subscribe
    public void onWorldViewUnloaded(WorldViewUnloaded e)
    {
        if (e.getWorldView().isTopLevel())
        {
            wrecks.clear();
            stumps.clear();
        }
    }
    public static Set<String> getHighAlchList() {
        String stored = Microbot.getConfigManager().getConfiguration(
                "salvage",
                "listOfItemsToalch",
                String.class
        );

        LinkedHashSet<String> normalized = normalizeCsvEntries(stored);
        if (normalized.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(normalized);
    }
    public static Set<String> getDropList() {
        String stored = Microbot.getConfigManager().getConfiguration(
                "salvage",
                "listOfItemsToDrop",
                String.class
        );

        LinkedHashSet<String> normalized = normalizeCsvEntries(stored);
        if (normalized.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(normalized);
    }
    public static boolean isHighAlch(String itemName) {
        if (itemName == null) {
            return false;
        }

        String normalizedItemName = Text.standardize(itemName);
        if (normalizedItemName.isEmpty()) {
            return false;
        }

        Set<String> blacklist = getHighAlchList();
        if (blacklist.isEmpty()) {
            return false;
        }

        for (String pattern : blacklist) {
            if (matchesWildcard(pattern, normalizedItemName)) {
                return true;
            }
        }

        return false;
    }
    public static boolean isDrop(String itemName) {
        if (itemName == null) {
            return false;
        }

        String normalizedItemName = Text.standardize(itemName);
        if (normalizedItemName.isEmpty()) {
            return false;
        }

        Set<String> blacklist = getDropList();
        if (blacklist.isEmpty()) {
            return false;
        }

        for (String pattern : blacklist) {
            if (matchesWildcard(pattern, normalizedItemName)) {
                return true;
            }
        }

        return false;
    }
    private static boolean matchesWildcard(String pattern, String candidate) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        if (!pattern.contains("*")) {
            return candidate.equals(pattern);
        }

        StringBuilder regex = new StringBuilder();
        regex.append('^');
        for (char ch : pattern.toCharArray()) {
            if (ch == '*') {
                regex.append(".*");
            } else {
                if ("\\.^$|?+()[]{}".indexOf(ch) >= 0) {
                    regex.append('\\');
                }
                regex.append(ch);
            }
        }
        regex.append('$');

        return candidate.matches(regex.toString());
    }
    private static LinkedHashSet<String> normalizeCsvEntries(String rawCsv) {
        String source = rawCsv == null ? "" : rawCsv;
        return Arrays.stream(source.split(","))
                .map(Text::standardize)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
