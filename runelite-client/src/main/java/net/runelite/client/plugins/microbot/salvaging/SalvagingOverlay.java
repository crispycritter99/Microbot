package net.runelite.client.plugins.microbot.salvaging;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.findObjectById;

public class SalvagingOverlay extends OverlayPanel {
public static boolean isClose = false;
    @Inject
    SalvagingOverlay(SalvagingPlugin plugin)
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
//            List objectIds = (List) Rs2GameObject.getAll()
//                    .stream()
//                    .map(obj -> obj.getId())
//                    .collect(Collectors.toList());
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(""+ SalvagingPlugin.shouldloot)
                    .build());



        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
