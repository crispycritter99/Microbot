/*
 * Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.devtools;

import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Singleton
class DevToolsOverlay extends Overlay {
    private static final Font FONT = FontManager.getRunescapeFont().deriveFont(Font.BOLD, 16);
    private static final Color RED = new Color(221, 44, 0);
    private static final Color GREEN = new Color(0, 200, 83);
    private static final Color ORANGE = new Color(255, 109, 0);
    private static final Color YELLOW = new Color(255, 214, 0);
    private static final Color CYAN = new Color(0, 184, 212);
    private static final Color BLUE = new Color(41, 98, 255);
    private static final Color DEEP_PURPLE = new Color(98, 0, 234);
    private static final Color PURPLE = new Color(170, 0, 255);
    private static final Color GRAY = new Color(158, 158, 158);

    private static final int MAX_DISTANCE = 2400;

    private final Client client;
    private final DevToolsPlugin plugin;
    private final TooltipManager toolTipManager;

    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private DevToolsOverlay(Client client, DevToolsPlugin plugin, TooltipManager toolTipManager, ModelOutlineRenderer modelOutlineRenderer) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(PRIORITY_HIGHEST);
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.client = client;
        this.plugin = plugin;
        this.toolTipManager = toolTipManager;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setFont(FONT);

        if (plugin.getPlayers().isActive()) {
            renderPlayers(graphics);
        }

        if (plugin.getNpcs().isActive()) {
            renderNpcs(graphics);
        }

        if (plugin.getInventory().isActive()) {
            renderInventory(graphics);
        }

        if (plugin.getMemoryInspector().isActive()) {
            renderMemory(graphics);
        }

        if (plugin.getGroundItems().isActive() || plugin.getGroundObjects().isActive() || plugin.getTileObjects().isActive()
            || plugin.getGameObjects().isActive() || plugin.getWalls().isActive() || plugin.getDecorations().isActive()
            || plugin.getTileLocation().isActive() || plugin.getMovementFlags().isActive()) {
            renderTileObjects(graphics);
        }

        if (plugin.getProjectiles().isActive()) {
            renderProjectiles(graphics);
        }

        if (plugin.getGraphicsObjects().isActive()) {
            renderGraphicsObjects(graphics);
        }

        if (plugin.getTileFlags().isActive()) {
            renderTileFlags(graphics);
        }

        return null;
    }

    private void renderTileFlags(Graphics2D graphics) {
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();
        byte[][][] settings = client.getTileSettings();
        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }

                boolean isbridge = (settings[1][x][y] & Constants.TILE_FLAG_BRIDGE) != 0;
                int flag = settings[z][x][y];
                boolean isvisbelow = (flag & Constants.TILE_FLAG_VIS_BELOW) != 0;
                boolean hasroof = (flag & Constants.TILE_FLAG_UNDER_ROOF) != 0;
                if (!isbridge && !isvisbelow && !hasroof) {
                    continue;
                }

                String s = "";
                if (isbridge) {
                    s += "B";
                }
                if (isvisbelow) {
                    s += "V";
                }
                if (hasroof) {
                    s += "R";
                }

                Point loc = Perspective.getCanvasTextLocation(client, graphics, tile.getLocalLocation(), s, z);
                if (loc == null) {
                    continue;
                }

                OverlayUtil.renderTextLocation(graphics, loc, s, Color.RED);
            }
        }
    }

    private void renderPlayers(Graphics2D graphics) {
        List<Player> players = client.getPlayers();
        Player local = client.getLocalPlayer();

        for (Player p : players) {
            if (p != local) {
                String text = p.getName() + " (A: " + p.getAnimation() + ") (P: " + p.getPoseAnimation() + ") (G: " + p.getGraphic() + ")";
                OverlayUtil.renderActorOverlay(graphics, p, text, BLUE);
            }
        }

        String text = local.getName() + " (A: " + local.getAnimation() + ") (P: " + local.getPoseAnimation() + ") (G: " + local.getGraphic() + ")";
        OverlayUtil.renderActorOverlay(graphics, local, text, CYAN);
    }

    private void renderNpcs(Graphics2D graphics) {
        List<NPC> npcs = client.getNpcs();
        for (NPC npc : npcs) {
            NPCComposition composition = npc.getComposition();
            Color color = composition.getCombatLevel() > 1 ? YELLOW : ORANGE;
            if (composition.getConfigs() != null) {
                NPCComposition transformedComposition = composition.transform();
                if (transformedComposition == null) {
                    color = GRAY;
                } else {
                    composition = transformedComposition;
                }
            }

            String text = composition.getName() + " (ID:" + composition.getId() + ")" +
                    " (A: " + npc.getAnimation() + ") (P: " + npc.getPoseAnimation() + ") (G: " + npc.getGraphic() + ")";
            if (npc.getModelOverrides() != null) {
                var mo = npc.getModelOverrides();
                if (mo.getModelIds() != null) {
                    text += " (M: " + Arrays.toString(mo.getModelIds()) + ")";
                }
                if (mo.getColorToReplaceWith() != null) {
                    text += " (C: " + Arrays.toString(mo.getColorToReplaceWith()) + ")";
                }
                if (mo.getTextureToReplaceWith() != null) {
                    text += " (T: " + Arrays.toString(mo.getTextureToReplaceWith()) + ")";
                }
                if (mo.useLocalPlayer()) {
                    text += " (LocalPlayer)";
                }
            }
            OverlayUtil.renderActorOverlay(graphics, npc, text, color);
        }
    }

    private void renderInventory(Graphics2D graphics) {
        if (Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY) return;
        for (Widget inventoryWidget : Rs2Widget.getWidget(ComponentID.INVENTORY_CONTAINER).getChildren()) {
            if (inventoryWidget == null || inventoryWidget.getItemId() == 6512) continue;
            Point canvasLocation = inventoryWidget.getCanvasLocation();
            OverlayUtil.renderTextLocation(graphics, new Point(canvasLocation.getX(), canvasLocation.getY() + inventoryWidget.getHeight()), String.valueOf(inventoryWidget.getItemId()), Color.GREEN, 12);
        }
    }

    /**
     * Renders the amount of memory the Microbot Client is using
     * @param graphics
     */
    private void renderMemory(Graphics2D graphics) {
        // Retrieve memory usage metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.maxMemory() / (1024 * 1024); // Convert bytes to megabytes
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // Used memory in MB
        int usedPercent = (int) ((memoryUsed * 100) / totalMemory); // Calculate used memory percentage

        // Format the memory usage text
        String memoryText = String.format("%d / %d MB (%d%%)", memoryUsed, totalMemory, usedPercent);

        // Determine the position for rendering the text
        int width = (int) client.getRealDimensions().getWidth();
        int xPosition = width - 107;
        int yPosition = 30;

        // Render the memory usage text on the screen
        OverlayUtil.renderTextLocation(graphics, new Point(xPosition, yPosition), memoryText, Color.YELLOW, 12);
    }

    private void renderTileObjects(Graphics2D graphics) {
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }

                Player player = client.getLocalPlayer();
                if (player == null) {
                    continue;
                }

                if (plugin.getGroundItems().isActive()) {
                    renderGroundItems(graphics, tile, player);
                }

                if (plugin.getTileObjects().isActive())
                {
                    renderGameObjects(graphics, tile, player);
                    renderTileObject(graphics, tile.getWallObject(), player, Color.GRAY);
                    renderTileObject(graphics, tile.getDecorativeObject(), player, Color.LIGHT_GRAY);
                    renderTileObject(graphics, tile.getGroundObject(), player, Color.PINK);
                }
                else
                {
                    if (plugin.getGameObjects().isActive())
                    {
                        renderGameObjects(graphics, tile, player);
                    }
                    if (plugin.getWalls().isActive())
                    {
                        renderTileObject(graphics, tile.getWallObject(), player, Color.GRAY);
                    }
                    if (plugin.getDecorations().isActive())
                    {
                        renderTileObject(graphics, tile.getDecorativeObject(), player, Color.LIGHT_GRAY);
                    }
                    if (plugin.getGroundObjects().isActive())
                    {
                        renderTileObject(graphics, tile.getGroundObject(), player, Color.PINK);
                    }
                }

                if (plugin.getTileLocation().isActive()) {
                    renderTileTooltip(graphics, tile);
                }

                if (plugin.getMovementFlags().isActive()) {
                    renderMovementInfo(graphics, tile);
                }
            }
        }
    }

	private void renderTileTooltip(Graphics2D graphics, Tile tile)
	{
		final LocalPoint tileLocalLocation = tile.getLocalLocation();
		Polygon poly = Perspective.getCanvasTilePoly(client, tileLocalLocation);
		if (poly != null && poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
		{
			WorldPoint worldLocation = WorldPoint.fromLocalInstance(client, tileLocalLocation);
			byte flags = client.getTileSettings()[tile.getRenderLevel()][tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];
			String tooltip = String.format("World location: %d, %d, %d<br>" +
					"Region ID: %d location: %d, %d<br>" +
					"Flags: %d",
				worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(),
				worldLocation.getRegionID(), worldLocation.getRegionX(), worldLocation.getRegionY(),
				flags);
			toolTipManager.add(new Tooltip(tooltip));
			OverlayUtil.renderPolygon(graphics, poly, GREEN);
		}
	}

    private void renderMovementInfo(Graphics2D graphics, Tile tile) {
        Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());

        if (poly == null || !poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
            return;
        }

        if (client.getCollisionMaps() != null) {
            int[][] flags = client.getCollisionMaps()[client.getPlane()].getFlags();
            int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

            Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

            if (movementFlags.isEmpty()) {
                toolTipManager.add(new Tooltip("No movement flags"));
            } else {
                movementFlags.forEach(flag -> toolTipManager.add(new Tooltip(flag.toString())));
            }

            OverlayUtil.renderPolygon(graphics, poly, GREEN);
        }
    }

    private void renderGroundItems(Graphics2D graphics, Tile tile, Player player) {
        ItemLayer itemLayer = tile.getItemLayer();
        if (itemLayer != null) {
            if (player.getLocalLocation().distanceTo(itemLayer.getLocalLocation()) <= MAX_DISTANCE) {
                Node current = itemLayer.getTop();
                while (current instanceof TileItem) {
                    TileItem item = (TileItem) current;
                    OverlayUtil.renderTileOverlay(graphics, itemLayer, "ID: " + item.getId() + " Qty:" + item.getQuantity(), RED);
                    current = current.getNext();
                }
            }
        }
    }

    private void renderGameObjects(Graphics2D graphics, Tile tile, Player player) {
        GameObject[] gameObjects = tile.getGameObjects();
        if (gameObjects != null) {
            for (GameObject gameObject : gameObjects) {
                if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation())) {
                    ObjectComposition objComposition = client.getObjectDefinition(gameObject.getId());
                    if (objComposition == null )
                    {
                        return;
                    }
                    if (player.getLocalLocation().distanceTo(gameObject.getLocalLocation()) <= MAX_DISTANCE) {
                        modelOutlineRenderer.drawOutline(gameObject, 1, Color.RED, 50);
                        MenuEntry[] menuEntries = client.getMenuEntries();
                        MenuEntry hovered = Arrays.stream(menuEntries).filter(e -> e.getType().equals(MenuAction.GAME_OBJECT_FIRST_OPTION) ||
                                e.getType().equals(MenuAction.GAME_OBJECT_SECOND_OPTION) ||
                                e.getType().equals(MenuAction.GAME_OBJECT_THIRD_OPTION) ||
                                e.getType().equals(MenuAction.GAME_OBJECT_FOURTH_OPTION) ||
                                e.getType().equals(MenuAction.GAME_OBJECT_FIFTH_OPTION) ||
                                e.getType().equals(MenuAction.EXAMINE_OBJECT) ||
                                e.getType().equals(MenuAction.WIDGET_TARGET_ON_GAME_OBJECT))
                                .findFirst().orElse(null);
                        MenuAction action = hovered != null ? hovered.getType() : MenuAction.CANCEL;

                        switch (action)
                        {
                            case WIDGET_TARGET_ON_GAME_OBJECT:
                            case GAME_OBJECT_FIRST_OPTION:
                            case GAME_OBJECT_SECOND_OPTION:
                            case GAME_OBJECT_THIRD_OPTION:
                            case GAME_OBJECT_FOURTH_OPTION:
                            case GAME_OBJECT_FIFTH_OPTION:
                            case EXAMINE_OBJECT:
                            {
                                int x = hovered.getParam0();
                                int y = hovered.getParam1();
                                int id = hovered.getIdentifier();

                                TileObject hoveredObject = plugin.findTileObject(x, y, id);
                                if (hoveredObject instanceof GameObject && gameObject.equals(hoveredObject))
                                {
                                    modelOutlineRenderer.drawOutline(gameObject, 5, new Color(0,255,0,150), 5);
                                    String objectType = "Unknown";
                                    if (gameObject instanceof WallObject)
                                    {
                                        objectType = "Wall";
                                    }
                                    else if (gameObject instanceof DecorativeObject)
                                    {
                                        objectType = "Decorative";
                                    }
                                    else if (gameObject instanceof GroundObject)
                                    {
                                        objectType = "Ground";
                                    }
                                    else {
                                        objectType = "Game";
                                    }

                                    Point textLocation = gameObject.getCanvasTextLocation(graphics,
                                            String.format("ID: %d", gameObject.getId()), 0);

                                    if (textLocation != null)
                                    {
                                        WorldPoint worldLocation = gameObject.getWorldLocation();
                                        String text = String.format("%s (ID: %d X: %d Y: %d)",
                                                objComposition.getName(), gameObject.getId(), worldLocation.getX(), worldLocation.getY());
                                        String typeText = "Type: " + objectType;

                                        OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.GREEN);
                                        Point typeTextLocation = new Point(textLocation.getX(), textLocation.getY() + 15);
                                        OverlayUtil.renderTextLocation(graphics, typeTextLocation, typeText, Color.GREEN);
                                    }
                                }

                                break;
                            }
                            default:
                                break;
                        }


                    }
                }
            }
        }
    }

    private void renderTileObject(Graphics2D graphics, TileObject tileObject, Player player, Color color) {
        if (tileObject != null) {
            if (player.getLocalLocation().distanceTo(tileObject.getLocalLocation()) <= MAX_DISTANCE) {
                OverlayUtil.renderTileOverlay(graphics, tileObject, "ID: " + tileObject.getId(), color);
            }
        }
    }

    private void renderProjectiles(Graphics2D graphics) {
        for (Projectile projectile : client.getProjectiles()) {
            int projectileId = projectile.getId();
            String text = "(ID: " + projectileId + ")";
            int x = (int) projectile.getX();
            int y = (int) projectile.getY();
            LocalPoint projectilePoint = new LocalPoint(x, y);
            Point textLocation = Perspective.getCanvasTextLocation(client, graphics, projectilePoint, text, 0);
            if (textLocation != null) {
                OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.RED);
            }
        }
    }

    private void renderGraphicsObjects(Graphics2D graphics) {
        for (GraphicsObject graphicsObject : client.getGraphicsObjects()) {
            LocalPoint lp = graphicsObject.getLocation();
            Polygon poly = Perspective.getCanvasTilePoly(client, lp);

            if (poly != null) {
                OverlayUtil.renderPolygon(graphics, poly, Color.MAGENTA);
            }

            String infoString = "(ID: " + graphicsObject.getId() + ")";
            Point textLocation = Perspective.getCanvasTextLocation(
                    client, graphics, lp, infoString, 0);
            if (textLocation != null) {
                OverlayUtil.renderTextLocation(graphics, textLocation, infoString, Color.WHITE);
            }
        }
    }
}
