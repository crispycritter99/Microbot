package net.runelite.client.plugins.microbot.mongooseamethystminer;

import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.mongooseamethystminer.MongooseAmethystMiningScript.status;

public class MongooseAmethystMiningOverlay extends OverlayPanel {
    @Inject
    MongooseAmethystMiningOverlay(MongooseAmethystMiningPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredLocation(new Point(80, 8));
            panelComponent.setPreferredSize(new Dimension(275, 700));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("\uD83E\uDD86 Mongoose Amethyst Miner \uD83E\uDD86")
                    .color(Color.ORANGE)
                    .build());

            addEmptyLine();

            if(Rs2AntibanSettings.devDebug)
                Rs2Antiban.renderAntibanOverlayComponents(panelComponent);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Gem bag inv count: " + MongooseAmethystMiningScript.inventoryCountSinceLastGemBagCheck)
                    .build());
            addEmptyLine();
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Items to keep: " + MongooseAmethystMiningScript.itemsToKeep.toString())
                    .build());
            addEmptyLine();

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(status.toString())
                    .right("Version: " + MongooseAmethystMiningPlugin.version)
                    .build());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }

    private void addEmptyLine() {
        panelComponent.getChildren().add(LineComponent.builder().build());
    }
}

