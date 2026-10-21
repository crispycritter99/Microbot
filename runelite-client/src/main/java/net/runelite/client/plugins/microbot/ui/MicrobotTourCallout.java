package net.runelite.client.plugins.microbot.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

class MicrobotTourCallout extends JWindow
{
	private static final int BODY_WIDTH = 260;
	private static final int ARC = 10;

	private final JLabel title = new JLabel();
	private final JLabel newChip = MicrobotCloudPanel.newChip();
	private final JLabel body = new JLabel();
	private final JLabel counter = new JLabel();
	private final JButton skip = new JButton("Skip tour");
	private final JButton back = new JButton("Back");
	private final JButton next = new JButton("Next");

	MicrobotTourCallout(Window owner, Runnable onSkip, Runnable onBack, Runnable onNext)
	{
		super(owner);
		setType(Window.Type.POPUP);
		setFocusableWindowState(false);
		setAutoRequestFocus(false);

		JPanel card = new JPanel(new BorderLayout(0, 8));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new CompoundBorder(new LineBorder(ColorScheme.BRAND_ORANGE, 1), new EmptyBorder(10, 12, 10, 12)));

		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		header.setOpaque(false);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		header.add(title);
		header.add(Box.createHorizontalStrut(6));
		header.add(newChip);
		card.add(header, BorderLayout.NORTH);

		body.setFont(FontManager.getRunescapeSmallFont());
		body.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		body.setVerticalAlignment(JLabel.TOP);
		card.add(body, BorderLayout.CENTER);

		JPanel footer = new JPanel(new BorderLayout(8, 0));
		footer.setOpaque(false);
		counter.setFont(FontManager.getRunescapeSmallFont());
		counter.setForeground(ColorScheme.MEDIUM_GRAY_COLOR.brighter());
		footer.add(counter, BorderLayout.WEST);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		buttons.setOpaque(false);
		styleLink(skip);
		style(back);
		style(next);
		next.setBackground(ColorScheme.BRAND_ORANGE);
		next.setForeground(ColorScheme.DARKER_GRAY_COLOR);
		skip.addActionListener(e -> onSkip.run());
		back.addActionListener(e -> onBack.run());
		next.addActionListener(e -> onNext.run());
		buttons.add(skip);
		buttons.add(back);
		buttons.add(next);
		footer.add(buttons, BorderLayout.EAST);
		card.add(footer, BorderLayout.SOUTH);

		setContentPane(card);

		GraphicsDevice device = owner.getGraphicsConfiguration().getDevice();
		if (device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT))
		{
			addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentResized(ComponentEvent e)
				{
					setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ARC, ARC));
				}
			});
		}
	}

	void setStep(MicrobotTourStep step, int position, int total, boolean showBack, boolean last)
	{
		title.setText(step.getTitle());
		newChip.setVisible(step.isNew());
		body.setText("<html><body style='width:" + BODY_WIDTH + "px'>" + escape(step.getBody()) + "</body></html>");
		counter.setText("Step " + position + " of " + total);
		skip.setVisible(!last);
		back.setVisible(showBack);
		next.setText(last ? "Done" : "Next");
		pack();
	}

	static String escape(String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static void style(JButton button)
	{
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setFocusable(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	private static void styleLink(JButton button)
	{
		style(button);
		button.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	}
}
