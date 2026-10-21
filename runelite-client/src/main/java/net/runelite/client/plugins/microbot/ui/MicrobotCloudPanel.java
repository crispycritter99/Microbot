package net.runelite.client.plugins.microbot.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.microbot.MicrobotConfig;
import net.runelite.client.plugins.microbot.alerts.BotAlertStatus;
import net.runelite.client.plugins.microbot.alerts.DeviceLinkFlow;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

@Singleton
public class MicrobotCloudPanel extends JPanel
{
	static final String KEY_COLLAPSED = "cloudPanelCollapsed";
	private static final String DASHBOARD = "https://dashboard.microbot.cloud";
	private static final ImageIcon EXPAND_ICON;
	private static final ImageIcon RETRACT_ICON;

	static
	{
		BufferedImage arrow = ImageUtil.luminanceOffset(ImageUtil.loadImageResource(MicrobotConfigPanel.class, "/util/arrow_right.png"), -121);
		EXPAND_ICON = new ImageIcon(arrow);
		RETRACT_ICON = new ImageIcon(ImageUtil.rotateImage(arrow, Math.PI / 2));
	}

	private final DeviceLinkFlow deviceLinkFlow;
	private final ConfigManager configManager;
	private final JButton toggle = new JButton();
	private final JPanel body = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel alertStatus = new JLabel();
	private final JButton alertAction = new JButton();
	private final Timer refresher = new Timer(1000, e -> refresh());
	private BotAlertStatus shown;

	@Inject
	MicrobotCloudPanel(DeviceLinkFlow deviceLinkFlow, ConfigManager configManager)
	{
		this.deviceLinkFlow = deviceLinkFlow;
		this.configManager = configManager;

		setLayout(new BorderLayout());
		setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(4, 10, 4, 10)));

		JPanel header = new JPanel(new BorderLayout());
		header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		toggle.setBorder(new EmptyBorder(0, 0, 0, 5));
		SwingUtil.removeButtonDecorations(toggle);
		toggle.addActionListener(e -> setCollapsed(body.isVisible()));
		header.add(toggle, BorderLayout.WEST);
		JLabel title = new JLabel("Microbot Cloud");
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setToolTipText("Features that use your microbot.cloud account");
		header.add(title, BorderLayout.CENTER);
		MouseAdapter onHeader = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				setCollapsed(body.isVisible());
			}
		};
		header.addMouseListener(onHeader);
		title.addMouseListener(onHeader);
		add(header, BorderLayout.NORTH);

		body.setBorder(new EmptyBorder(4, 0, 0, 0));
		JLabel alertsName = new JLabel("Bot alerts");
		alertsName.setFont(FontManager.getRunescapeSmallFont());
		alertsName.setToolTipText("<html>Get a Discord or email alert when this client stops responding,<br>a script stops on an error or the game logs out</html>");
		JPanel alertsRow = new JPanel(new BorderLayout(6, 0));
		alertsRow.add(alertsName, BorderLayout.WEST);
		alertStatus.setFont(FontManager.getRunescapeSmallFont());
		alertStatus.setHorizontalAlignment(JLabel.RIGHT);
		alertsRow.add(alertStatus, BorderLayout.CENTER);
		body.add(alertsRow);

		alertAction.setFont(FontManager.getRunescapeSmallFont());
		alertAction.setFocusable(false);
		alertAction.addActionListener(e ->
		{
			deviceLinkFlow.start();
			refresh();
		});
		body.add(alertAction);

		JButton dashboard = new JButton("Open dashboard");
		dashboard.setFont(FontManager.getRunescapeSmallFont());
		dashboard.setFocusable(false);
		dashboard.setToolTipText(DASHBOARD);
		dashboard.addActionListener(e -> LinkBrowser.browse(DASHBOARD));
		body.add(dashboard);
		add(body, BorderLayout.CENTER);

		applyCollapsed(Boolean.TRUE.equals(configManager.getConfiguration(MicrobotConfig.configGroup, KEY_COLLAPSED, Boolean.class)));
		refresh();
	}

	public void setActive(boolean active)
	{
		if (active)
		{
			refresh();
			refresher.start();
		}
		else
		{
			refresher.stop();
		}
	}

	private void setCollapsed(boolean collapsed)
	{
		applyCollapsed(collapsed);
		if (collapsed)
		{
			configManager.setConfiguration(MicrobotConfig.configGroup, KEY_COLLAPSED, true);
		}
		else
		{
			configManager.unsetConfiguration(MicrobotConfig.configGroup, KEY_COLLAPSED);
		}
	}

	private void applyCollapsed(boolean collapsed)
	{
		body.setVisible(!collapsed);
		toggle.setIcon(collapsed ? EXPAND_ICON : RETRACT_ICON);
		toggle.setToolTipText(collapsed ? "Expand" : "Minimize");
		revalidate();
	}

	private void refresh()
	{
		BotAlertStatus status = deviceLinkFlow.status();
		if (status == shown)
		{
			return;
		}
		shown = status;
		alertStatus.setText(status.getText());
		alertStatus.setForeground(status.isOn() ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
		alertAction.setVisible(status.getAction() != null);
		if (status.getAction() != null)
		{
			alertAction.setText(status.getAction());
		}
	}
}
