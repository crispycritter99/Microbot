package net.runelite.client.plugins.microbot.bloodwoodChopper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class BloodwoodChopperOverlay extends OverlayPanel {

    @Inject
    BloodwoodChopperOverlay(BloodwoodChopperPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Bloodwood Chopper")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("" + Microbot.status)
                    .build());
            if (Rs2Player.isInteracting()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("" + Rs2Player.getInteracting().getWorldLocation())
                        .build());
            }
            panelComponent.getChildren().add(LineComponent.builder().build());
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
