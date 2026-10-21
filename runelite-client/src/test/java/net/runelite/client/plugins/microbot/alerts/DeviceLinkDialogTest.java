package net.runelite.client.plugins.microbot.alerts;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DeviceLinkDialogTest
{
	private static final String URL = "https://dashboard.example.test/link?code=ABCD-EFGH&x=1";

	@Test
	public void turnsTheVerificationUrlIntoALink()
	{
		String html = DeviceLinkDialog.toHtml("Code: ABCD-EFGH\n\nIf no page opened, go to " + URL, URL);

		assertEquals("<html>Code: ABCD-EFGH<br><br>If no page opened, go to "
			+ "<a href=\"https://dashboard.example.test/link?code=ABCD-EFGH&amp;x=1\">"
			+ "https://dashboard.example.test/link?code=ABCD-EFGH&amp;x=1</a></html>", html);
	}

	@Test
	public void escapesTheMessage()
	{
		assertEquals("<html>&lt;b&gt;hi&lt;/b&gt;</html>", DeviceLinkDialog.toHtml("<b>hi</b>", null));
	}
}
