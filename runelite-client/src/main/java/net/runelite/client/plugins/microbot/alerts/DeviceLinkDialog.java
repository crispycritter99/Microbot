package net.runelite.client.plugins.microbot.alerts;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import java.awt.KeyboardFocusManager;
import javax.inject.Singleton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.runelite.client.plugins.info.JRichTextPane;
import net.runelite.client.util.LinkBrowser;

@Singleton
public class DeviceLinkDialog
{
	private static final String TITLE = "Bot alerts";
	private static final String HIDE = "Hide";
	private static final String CANCEL = "Cancel linking";
	private static final String OK = "OK";

	private JDialog dialog;

	public void showWaiting(String message, String url, Runnable onCancel)
	{
		SwingUtilities.invokeLater(() -> show(withLink(message, url), new Object[]{HIDE, CANCEL}, onCancel));
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

	static String toHtml(String message, String url)
	{
		Escaper escaper = HtmlEscapers.htmlEscaper();
		String html = escaper.escape(message);
		if (!Strings.isNullOrEmpty(url))
		{
			String link = escaper.escape(url);
			html = html.replace(link, "<a href=\"" + link + "\">" + link + "</a>");
		}
		return "<html>" + html.replace("\n", "<br>") + "</html>";
	}

	private static JRichTextPane withLink(String message, String url)
	{
		JRichTextPane pane = new JRichTextPane();
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		pane.setFont(UIManager.getFont("OptionPane.messageFont"));
		pane.setContentType("text/html");
		pane.setText(toHtml(message, url));
		return pane;
	}

	private void show(Object message, Object[] options, Runnable onCancel)
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
