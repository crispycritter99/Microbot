package net.runelite.client.plugins.microbot.alerts;

import java.awt.KeyboardFocusManager;
import javax.inject.Singleton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.runelite.client.util.LinkBrowser;

@Singleton
public class DeviceLinkDialog
{
	private static final String TITLE = "Bot alerts";
	private static final String HIDE = "Hide";
	private static final String CANCEL = "Cancel linking";
	private static final String OK = "OK";

	private JDialog dialog;

	public void showWaiting(String message, Runnable onCancel)
	{
		SwingUtilities.invokeLater(() -> show(message, new Object[]{HIDE, CANCEL}, onCancel));
	}

	public void showMessage(String message)
	{
		SwingUtilities.invokeLater(() -> show(message, new Object[]{OK}, null));
	}

	public void close()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (dialog != null)
			{
				dialog.dispose();
				dialog = null;
			}
		});
	}

	public void browse(String url)
	{
		LinkBrowser.browse(url);
	}

	private void show(String message, Object[] options, Runnable onCancel)
	{
		if (dialog != null)
		{
			dialog.dispose();
		}
		JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		JDialog next = pane.createDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), TITLE);
		next.setModal(false);
		if (onCancel != null)
		{
			pane.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, event ->
			{
				if (CANCEL.equals(event.getNewValue()))
				{
					onCancel.run();
				}
			});
		}
		dialog = next;
		next.setVisible(true);
	}
}
