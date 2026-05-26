package net.runelite.client.plugins.microbot.caviarmixer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.giantsfoundry.GiantsFoundryState;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CaviarMixerOverlay extends OverlayPanel {

    @Inject
    CaviarMixerOverlay(caviarMixerPlugin plugin)
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
                    .text("Micro Example V1.0.0")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("" + Microbot.status)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("" + GiantsFoundryState.getCurrentHeat())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());
//            WorldPoint worldPoint =  Microbot.getClientThread().invoke(() -> Microbot.getClient().getLocalPlayer().getWorldLocation());
                int deayaltFragments=Rs2Inventory.itemQuantity("Daeyalt shard");
                long shardsGained=deayaltFragments- caviarMixerPlugin.startingDaeyaltShard;
                long timeElapsed= caviarMixerPlugin.initialTime-System.currentTimeMillis();
                int shardsPerHour = Math.toIntExact( shardsGained / timeElapsed);
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("" )
                        .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
