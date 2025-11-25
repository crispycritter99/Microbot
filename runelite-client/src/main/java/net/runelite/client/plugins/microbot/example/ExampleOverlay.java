package net.runelite.client.plugins.microbot.example;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;

public class ExampleOverlay extends OverlayPanel {

    @Inject
    ExampleOverlay(ExamplePlugin plugin)
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

            panelComponent.getChildren().add(LineComponent.builder().build());
            WorldPoint worldPoint =  WorldPoint.fromRegion(7222,
                    29,
                    42,
                    Microbot.getClient().getPlane());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Rs2WorldPoint.convertInstancedWorldPoint(worldPoint) +"")
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Rs2WorldPoint.toLocalInstance(worldPoint) +"")
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(""+Rs2GameObject.getAll(o -> o.getWorldLocation().equals(Rs2WorldPoint.convertInstancedWorldPoint(worldPoint))))
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
