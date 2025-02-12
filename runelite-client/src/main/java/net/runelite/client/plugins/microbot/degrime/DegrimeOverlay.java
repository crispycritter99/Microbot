package net.runelite.client.plugins.microbot.degrime;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class DegrimeOverlay extends OverlayPanel {
    private final DegrimeConfig config;
    @Inject
    DegrimeOverlay(DegrimePlugin plugin, DegrimeConfig config) {
        super(plugin);
        this.config=config;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(400, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Bank's BankStander V" + DegrimeScript.version)
                    .color(Color.GREEN)
                    .build());
            ///* Added by Storm
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("New features added by eXioStorm")
                    .leftColor(PluginDescriptor.stormColor)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Items processed : " + DegrimeScript.itemsProcessed)
                    .leftColor(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
