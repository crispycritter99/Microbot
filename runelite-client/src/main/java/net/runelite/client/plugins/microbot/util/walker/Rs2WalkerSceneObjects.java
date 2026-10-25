package net.runelite.client.plugins.microbot.util.walker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectQueryable;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.api.tileobject.models.TileObjectType;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public final class Rs2WalkerSceneObjects {

    private Rs2WalkerSceneObjects() {
    }

    public static <T extends TileObject> List<T> near(Class<T> kind, WorldPoint anchor, int radiusTiles) {
        if (anchor == null) {
            return Collections.emptyList();
        }
        List<T> found = Microbot.getClientThread().invoke(() -> {
            Player player = Microbot.getClient().getLocalPlayer();
            WorldView worldView = player != null ? player.getWorldView() : null;
            if (worldView == null) {
                return Collections.<T>emptyList();
            }
            LocalPoint anchorLocal = LocalPoint.fromWorld(worldView, anchor);
            if (anchorLocal == null) {
                anchorLocal = player.getLocalLocation();
            }
            if (anchorLocal == null) {
                return Collections.<T>emptyList();
            }
            LocalPoint origin = anchorLocal;
            int maxDistance = Math.min(radiusTiles, Constants.SCENE_SIZE) * Perspective.LOCAL_TILE_SIZE;
            Rs2TileObjectQueryable query = Microbot.getRs2TileObjectCache().query().fromWorldView();
            TileObjectType type = typeOf(kind);
            if (type != null) {
                query = query.ofType(type);
            }
            return query.toList().stream()
                    .map(Rs2TileObjectModel::getTileObject)
                    .filter(kind::isInstance)
                    .map(kind::cast)
                    .filter(o -> o.getLocalLocation() != null && o.getLocalLocation().distanceTo(origin) <= maxDistance)
                    .sorted(Comparator.comparingInt(o -> o.getLocalLocation().distanceTo(origin)))
                    .collect(Collectors.toList());
        });
        return found != null ? found : Collections.emptyList();
    }

    public static <T extends TileObject> List<T> near(Class<T> kind, Predicate<? super T> predicate, WorldPoint anchor, int radiusTiles) {
        return near(kind, anchor, radiusTiles).stream().filter(predicate).collect(Collectors.toList());
    }

    public static <T extends TileObject> T first(Class<T> kind, Predicate<? super T> predicate, WorldPoint anchor, int radiusTiles) {
        return near(kind, anchor, radiusTiles).stream().filter(predicate).findFirst().orElse(null);
    }

    public static List<TileObject> all() {
        return near(TileObject.class, Rs2Player.getWorldLocation(), Constants.SCENE_SIZE);
    }

    public static boolean interact(TileObject object, String action) {
        if (object == null) {
            return false;
        }
        Rs2TileObjectModel model = new Rs2TileObjectModel(object);
        if (action == null || action.isEmpty() || !model.hasAction(action)) {
            return model.click();
        }
        return model.click(action);
    }

    private static TileObjectType typeOf(Class<? extends TileObject> kind) {
        if (kind == WallObject.class) {
            return TileObjectType.WALL;
        }
        if (kind == GameObject.class) {
            return TileObjectType.GAME;
        }
        if (kind == GroundObject.class) {
            return TileObjectType.GROUND;
        }
        if (kind == DecorativeObject.class) {
            return TileObjectType.DECORATIVE;
        }
        return null;
    }
}
