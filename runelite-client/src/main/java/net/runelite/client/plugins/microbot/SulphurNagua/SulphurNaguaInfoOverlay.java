package net.runelite.client.plugins.microbot.SulphurNagua;


import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SulphurNaguaInfoOverlay extends OverlayPanel {
    private final SulphurNaguaConfig config;

    @Inject
    SulphurNaguaInfoOverlay(SulphurNaguaPlugin plugin, SulphurNaguaConfig config) {
        super(plugin);
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(250, 400));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("\uD83E\uDD86 AIO Fighter \uD83E\uDD86")
                    .color(Color.ORANGE)
                    .build());


            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Play Style: " + config.playStyle() + "(" + config.playStyle().getPrimaryTickInterval() + "," + config.playStyle().getSecondaryTickInterval() + ")")
                    .right("Attack cooldown: " + SulphurNaguaPlugin.getCooldown())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .right("State:" + config.state())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .right(Microbot.pauseAllScripts+"")
                    .build());



        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
