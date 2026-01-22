package net.runelite.client.plugins.datbear;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.api.Perspective;

import net.runelite.client.plugins.datbear.overlay.WorldPerspective;

public class ObstacleOutlineOverlay extends Overlay {

    private Client client;
    private BearycudaTrialsPlugin plugin;
    private BearycudaTrialsConfig config;

    @Inject
    public ObstacleOutlineOverlay(Client client, BearycudaTrialsPlugin plugin, BearycudaTrialsConfig config) {
        super();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        highlightObstacleTiles(graphics);

        return null;
    }

    private void highlightObstacleTiles(Graphics2D graphics) {
        if (!config.showObstacleOutlines() || plugin.getIsInTrial() == 0) {
            return;
        }

        var obstacleWorldPoints = plugin.getObstacleWorldPoints();
        if (obstacleWorldPoints == null || obstacleWorldPoints.isEmpty()) {
            return;
        }

        var obstacleColor = config.obstacleOutlineColor();

        for (var obstacleWorldPoint : obstacleWorldPoints) {
            if (obstacleWorldPoint == null) {
                continue;
            }

            var localPoints = WorldPerspective.getInstanceLocalPointFromReal(client, obstacleWorldPoint);
            if (localPoints == null || localPoints.isEmpty()) {
                continue;
            }

            for (LocalPoint localPoint : localPoints) {
                if (localPoint == null) {
                    continue;
                }

                var polygon = Perspective.getCanvasTilePoly(client, localPoint);
                if (polygon != null) {
                    OverlayUtil.renderPolygon(graphics, polygon, obstacleColor);
                }
            }
        }
    }

}
