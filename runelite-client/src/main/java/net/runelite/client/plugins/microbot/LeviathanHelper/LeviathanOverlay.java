package net.runelite.client.plugins.microbot.LeviathanHelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class LeviathanOverlay extends OverlayPanel {

    @Inject
    LeviathanOverlay(LeviathanHelperPlugin plugin)
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
                    .left("Opposite axe = " + LeviathanHelperPlugin.oppositeAxe)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Opposite axe counter = " + LeviathanHelperPlugin.oppositeAxeCounter)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current ticks = " + LeviathanHelperPlugin.currentRunningTicks)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Is projectile active = " + LeviathanHelperScript.isProjectileActive)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In fight = " + LeviathanHelperScript.inFight)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In instance = " + LeviathanHelperScript.inInstance)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State = " + LeviathanHelperScript.state)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bank State = " + LeviathanHelperScript.bankState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("POH State = " + LeviathanHelperScript.POHState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Prayer = " + LeviathanHelperPlugin.currentPrayer)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Axe on safe tile = " + LeviathanHelperPlugin.axeOnSafeTile)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
