package net.runelite.client.plugins.microbot.crabhunting;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CrabHuntingOverlay extends OverlayPanel {

    @Inject
    CrabHuntingOverlay(CrabHuntingPlugin plugin)
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
            panelComponent.getChildren().add(LineComponent.builder().build());
//            WorldPoint worldPoint =  Microbot.getClientThread().invoke(() -> Microbot.getClient().getLocalPlayer().getWorldLocation());
                int deayaltFragments=Rs2Inventory.itemQuantity("Daeyalt shard");
                long shardsGained=deayaltFragments- CrabHuntingPlugin.startingDaeyaltShard;
                long timeElapsed= CrabHuntingPlugin.initialTime-System.currentTimeMillis();
                int shardsPerHour = Math.toIntExact( shardsGained / timeElapsed);
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("" + deayaltFragments)
                        .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
