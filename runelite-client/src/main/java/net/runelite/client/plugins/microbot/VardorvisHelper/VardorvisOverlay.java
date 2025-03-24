package net.runelite.client.plugins.microbot.VardorvisHelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class VardorvisOverlay extends OverlayPanel {

    @Inject
    VardorvisOverlay(VardorvisHelperPlugin plugin)
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
                    .left("Opposite axe = " + VardorvisHelperPlugin.oppositeAxe)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Opposite axe counter = " + VardorvisHelperPlugin.oppositeAxeCounter)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current ticks = " + VardorvisHelperPlugin.currentRunningTicks)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Is projectile active = " + VardorvisHelperScript.isProjectileActive)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In fight = " + VardorvisHelperScript.inFight)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In instance = " + VardorvisHelperScript.inInstance)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State = " + VardorvisHelperScript.state)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bank State = " + VardorvisHelperScript.bankState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("POH State = " + VardorvisHelperScript.POHState)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Prayer = " + VardorvisHelperPlugin.currentPrayer)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Axe on safe tile = " + VardorvisHelperPlugin.axeOnSafeTile)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
