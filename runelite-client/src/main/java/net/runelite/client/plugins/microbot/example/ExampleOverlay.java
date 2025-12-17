package net.runelite.client.plugins.microbot.example;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;

import static net.runelite.client.plugins.microbot.salvaging.SalvagingPlugin.wrecks;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.findReachableObject;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.getAll;

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

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("" + Microbot.status)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());
            WorldPoint worldPoint =  Microbot.getClient().getLocalPlayer().getWorldLocation();

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("" + wrecks.size())
                        .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
