package net.runelite.client.plugins.microbot.bloodmoons;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class MoonsOverlay extends OverlayPanel {
    @Inject
    MoonsOverlay(MoonsPlugin plugin)
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
                    .text("Neon Moon V1.0.0")
                    .color(Color.GREEN)
                    .build());
            long elapsed= System.currentTimeMillis() - MoonsScript.start_time;
            double hours = elapsed / 3600000.0;
            double killsPerHour = MoonsScript.loots / hours;
            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(MoonsScript.state.toString())
                    .build());
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(String.format("Moon chests: " + MoonsScript.loots + " [%.1f kph]",killsPerHour))
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .right(MoonsPlugin.ticks+"")
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
