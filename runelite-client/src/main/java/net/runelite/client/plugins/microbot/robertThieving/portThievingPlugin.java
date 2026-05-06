package net.runelite.client.plugins.microbot.robertThieving;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Port Roberts Thieving",
        description = "Automates Port Roberts stall thieving with guard-awareness",
        tags = {"thieving", "microbot", "port roberts"},
        enabledByDefault = false
)
@Slf4j
public class portThievingPlugin extends Plugin
{
    // -------------------------------------------------------------------------
    // Stall registry
    // -------------------------------------------------------------------------
    public enum StallTypes { FUR, SILK, GEM, CANNON, FISH, ORE, SPICE, VEG, SILVER }

    /** Single source of truth for all stall state — no parallel maps */
    public static class StallState
    {
        public final int objectId;
        public final WorldPoint position;
        public final List<WorldPoint> watchPoints;

        // runtime
        public boolean isWatched          = false;
        public int watchedTicksRemaining  = 0;
        public int safeTicksRemaining     = 0; // ticks the stall stays safe after guard leaves

        public StallState(int objectId, WorldPoint position, List<WorldPoint> watchPoints)
        {
            this.objectId    = objectId;
            this.position    = position;
            this.watchPoints = watchPoints;
        }

        /** True when it is safe to steal right now */
        public boolean isSafe()
        {
            if (!isWatched) return safeTicksRemaining > 0;
            // Guard is leaving in ≤1 tick — safe to click
            return watchedTicksRemaining <= 1;
        }

        public void reset()
        {
            isWatched = false;
            watchedTicksRemaining = 0;
            safeTicksRemaining = 0;
        }
    }

    // Ordered priority: script will pick the first safe stall in this list
    public static final StallTypes[] STALL_PRIORITY = {
            StallTypes.GEM,
            StallTypes.SILVER,
            StallTypes.CANNON,
            StallTypes.SPICE,
            StallTypes.ORE,
            StallTypes.FISH,
            StallTypes.FUR,
            StallTypes.SILK,
            StallTypes.VEG
    };

    // Map from object ID → stall type for O(1) guard-zone → stall lookup
    public final Map<StallTypes, StallState> stalls = new EnumMap<>(StallTypes.class);
    private final Map<Integer, StallTypes> idToType = new HashMap<>();

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final String GUARD_NAME          = "Market Guard";
    private static final Set<Integer> GUARD_IDS     = Set.of(14881, 14882, 14883);
    private static final int GUARD_WATCH_DURATION   = 10; // ticks guard watches after arriving
    private static final int SAFE_WINDOW_TICKS      = 5;  // ticks the stall is safe after guard leaves

    // Centre of the market — script returns if player is outside this radius
    public static final WorldPoint MARKET_CENTRE = new WorldPoint(1866, 3293, 0);
    public static final int MARKET_RADIUS = 10;

    // -------------------------------------------------------------------------
    // Injected fields
    // -------------------------------------------------------------------------
    @Inject private Client client;
    @Inject private portThievingConfig config;
    @Inject private OverlayManager overlayManager;
    @Inject private portThievingOverlay overlay;
    @Inject private portThievingScript script;

    // Instance-scoped — no static leaks across restarts
    private final List<NPC> guards = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    @Override
    protected void startUp() throws AWTException
    {
        buildRegistry();
        overlayManager.add(overlay);
        script.run(config);
    }

    @Override
    protected void shutDown()
    {
        script.shutdown();
        overlayManager.remove(overlay);
        guards.clear();
        stalls.values().forEach(StallState::reset);
    }

    private void buildRegistry()
    {
        stalls.clear();
        idToType.clear();

        reg(StallTypes.FUR,    58102, wp(1870,3292), wp(1869,3292), wp(1869,3293), wp(1869,3294));
        reg(StallTypes.SILK,   58101, wp(1870,3295), wp(1869,3295), wp(1868,3295));
        reg(StallTypes.GEM,    58106, wp(1869,3289), wp(1869,3290), wp(1869,3291));
        reg(StallTypes.CANNON, 58108, wp(1867,3296), wp(1867,3295), wp(1866,3295));
        reg(StallTypes.FISH,   58103, wp(1861,3292), wp(1863,3292), wp(1863,3291));
        reg(StallTypes.ORE,    58107, wp(1861,3295), wp(1863,3294), wp(1863,3293));
        reg(StallTypes.SPICE,  58105, wp(1863,3289), wp(1864,3290), wp(1865,3290));
        reg(StallTypes.VEG,    58100, wp(1864,3296), wp(1865,3295), wp(1864,3295));
        reg(StallTypes.SILVER, 58104, wp(1866,3289), wp(1866,3290), wp(1867,3290), wp(1868,3290));
    }

    private void reg(StallTypes type, int objId, WorldPoint pos, WorldPoint... watches)
    {
        StallState s = new StallState(objId, pos, Arrays.asList(watches));
        stalls.put(type, s);
        idToType.put(objId, type);
    }

    private static WorldPoint wp(int x, int y) { return new WorldPoint(x, y, 0); }

    // -------------------------------------------------------------------------
    // NPC tracking
    // -------------------------------------------------------------------------
    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        NPC npc = event.getNpc();
        if (isMarketGuard(npc) && !guards.contains(npc))
            guards.add(npc);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        NPC npc = event.getNpc();
        if (isMarketGuard(npc))
            guards.remove(npc);
    }

    // -------------------------------------------------------------------------
    // Game tick — update all stall states
    // -------------------------------------------------------------------------
    @Subscribe
    public void onGameTick(GameTick event)
    {
        for (StallTypes type : StallTypes.values())
        {
            StallState s        = stalls.get(type);
            boolean wasWatched  = s.isWatched;
            boolean nowWatched  = isAnyGuardWatching(s.watchPoints);
            s.isWatched         = nowWatched;

            if (nowWatched && !wasWatched)
            {
                // Guard just arrived
                s.watchedTicksRemaining = GUARD_WATCH_DURATION;
                s.safeTicksRemaining    = 0;
            }
            else if (nowWatched)
            {
                // Guard still here
                if (s.watchedTicksRemaining > 0) s.watchedTicksRemaining--;
                // Pre-arm the safe window 1 tick before guard leaves
                if (s.watchedTicksRemaining == 1) s.safeTicksRemaining = SAFE_WINDOW_TICKS;
            }
            else
            {
                // No guard — count down safe window
                s.watchedTicksRemaining = 0;
                if (s.safeTicksRemaining > 0) s.safeTicksRemaining--;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Public API for script
    // -------------------------------------------------------------------------

    /**
     * Returns the highest-priority stall that is currently safe to steal from,
     * or null if none are safe.
     */
    public StallTypes getBestSafeStall()
    {
        for (StallTypes type : STALL_PRIORITY)
        {
            if (stalls.get(type).isSafe()) return type;
        }
        return null;
    }

    public boolean isStallSafe(StallTypes type)
    {
        return type != null && stalls.get(type).isSafe();
    }

    public int getObjectId(StallTypes type)
    {
        return stalls.get(type).objectId;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private boolean isMarketGuard(NPC npc)
    {
        return npc != null
                && GUARD_NAME.equals(npc.getName())
                && GUARD_IDS.contains(npc.getId());
    }

    private boolean isAnyGuardWatching(List<WorldPoint> points)
    {
        for (NPC guard : guards)
        {
            WorldPoint loc = guard.getWorldLocation();
            for (WorldPoint wp : points)
            {
                if (loc.getX() == wp.getX() && loc.getY() == wp.getY())
                    return true;
            }
        }
        return false;
    }

    @Provides
    portThievingConfig provideConfig(ConfigManager cm)
    {
        return cm.getConfig(portThievingConfig.class);
    }
}