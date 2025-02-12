package net.runelite.client.plugins.microbot.tickbarb;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;



public class ThreeTickBarbOverlay extends OverlayPanel {

    @Inject
    ThreeTickBarbOverlay(ThreeTickBarb threeTickBarb) {
        super(threeTickBarb);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return super.render(graphics);
    }
}
