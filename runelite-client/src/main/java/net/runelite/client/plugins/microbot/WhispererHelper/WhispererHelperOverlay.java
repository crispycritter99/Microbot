package net.runelite.client.plugins.microbot.WhispererHelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class WhispererHelperOverlay extends OverlayPanel {

    @Inject
    WhispererHelperOverlay(WhispererHelperPlugin plugin)
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
                    .text("Vardorvis SLAYER V1.0.0")
                    .color(Color.red.darker())
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Opposite axe = " + WhispererHelperPlugin.oppositeAxe)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Opposite axe counter = " + WhispererHelperPlugin.oppositeAxeCounter)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current ticks = " + WhispererHelperPlugin.currentRunningTicks)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Is projectile active = " + WhispererHelperScript.isProjectileActive)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In fight = " + WhispererHelperScript.inFight)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In instance = " + WhispererHelperScript.inInstance)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State = " + WhispererHelperScript.state)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bank State = " + WhispererHelperScript.bankState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("POH State = " + WhispererHelperScript.POHState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Prayer = " + WhispererHelperPlugin.currentPrayer)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Axe on safe tile = " + WhispererHelperPlugin.axeOnSafeTile)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
