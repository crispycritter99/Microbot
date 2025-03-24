package net.runelite.client.plugins.microbot.vorkathmelee;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class VorkathMeleeOverlay extends OverlayPanel {
    private final VorkathMeleePlugin plugin;

    @Inject
    VorkathMeleeOverlay(VorkathMeleePlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(300, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro Vorkath V" + VorkathMeleeScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(plugin.vorkathMeleeScript.state.toString())
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Vorkath kills: " + plugin.vorkathMeleeScript.vorkathSessionKills)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Vorkath kills until selling: " + plugin.vorkathMeleeScript.tempVorkathKills)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
