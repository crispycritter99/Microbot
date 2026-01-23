package net.runelite.client.plugins.datbear;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class BoostOverlay extends Overlay {

    private BearycudaTrialsPlugin plugin;
    private BearycudaTrialsConfig config;

    @Inject
    public BoostOverlay(Client client, BearycudaTrialsPlugin plugin, BearycudaTrialsConfig config) {
        super();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        highlightBoosts(graphics);
        return null;
    }

    private void highlightBoosts(Graphics2D graphics) {
        if (!config.showBoostHighlights()) {
            return;
        }
        var boosts = plugin.getTrialBoostsById();
        var boostColor = config.boostHighlightColor();
        for (var boostList : boosts.values()) {
            for (var boost : boostList) {
                var poly = boost.getCanvasTilePoly();
                if (poly != null) {
                    OverlayUtil.renderPolygon(graphics, poly, boostColor);
                }
            }
        }
    }
}
