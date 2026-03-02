package net.runelite.client.plugins.microbot.gotr;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static net.runelite.api.ItemID.GUARDIAN_FRAGMENTS;


public class GotrOverlay extends OverlayPanel {

    private final GotrLocalPlugin plugin;
    public static Color PUBLIC_TIMER_COLOR = Color.YELLOW;
    public static int TIMER_OVERLAY_DIAMETER = 20;
    private final ProgressPieComponent progressPieComponent = new ProgressPieComponent();

    int sleepingCounter;

    @Inject
    GotrOverlay(GotrLocalPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            int timeToStart = 0;
            if (GotrScript.nextGameStart.isPresent()) {
                timeToStart = ((int) ChronoUnit.SECONDS.between(Instant.now(), GotrScript.nextGameStart.get()));
            }
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro Guardians of the rift V" + GotrLocalPlugin.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("STATE: " + GotrScript.state)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Large Mine: " + GotrScript.isInLargeMine()+" "+GotrScript.getStartTimer()+ " "+
                            (!GotrScript.isInLargeMine() && (!Rs2Inventory.hasItem(GUARDIAN_FRAGMENTS) || GotrScript.getStartTimer() != -1||timeToStart>1)))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Elemental points: " + GotrScript.elementalRewardPoints)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Catalytic points: " + GotrScript.catalyticRewardPoints)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time since portal: " + GotrScript.getTimeSincePortal())
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Total time script loop: " + GotrScript.totalTime + "ms")
                    .build());

        } catch (Exception ex) {
            Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
        }
        return super.render(graphics);
    }
}
