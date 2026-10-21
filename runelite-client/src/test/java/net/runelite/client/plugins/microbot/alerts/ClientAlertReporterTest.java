package net.runelite.client.plugins.microbot.alerts;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.ClientSessionManager;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.MicrobotApi;
import net.runelite.client.plugins.microbot.externalplugins.MicrobotPluginManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientAlertReporterTest
{
	@PluginDescriptor(name = PluginDescriptor.Mocrosoft + "Fake Fisher", isExternal = true, version = "2.1.0")
	static class FakeScript extends Plugin
	{
	}

	@PluginDescriptor(name = "Break Handler")
	static class BuiltInPlugin extends Plugin
	{
	}

	private static final String TOKEN = ClientLinkTokenTest.CONFIG_TOKEN;
	private static final UUID SESSION = UUID.fromString("00000000-0000-0000-0000-000000000007");
	private static final String SCRIPT_LOGGER = FakeScript.class.getPackageName() + ".FishingScript";

	private final FakeScript script = new FakeScript();
	private MicrobotApi api;
	private ClientLinkToken token;
	private ClientSessionManager sessions;
	private PluginManager pluginManager;
	private ClientAlertReporter reporter;

	@Before
	public void setUp()
	{
		api = mock(MicrobotApi.class);
		token = mock(ClientLinkToken.class);
		when(token.get()).thenReturn(Optional.of(TOKEN));
		sessions = mock(ClientSessionManager.class);
		when(sessions.getMicrobotSessionId()).thenReturn(SESSION);
		pluginManager = mock(PluginManager.class);
		BuiltInPlugin builtIn = new BuiltInPlugin();
		when(pluginManager.getPlugins()).thenReturn(List.of(script, builtIn));
		when(pluginManager.isPluginActive(script)).thenReturn(true);
		when(pluginManager.isPluginActive(builtIn)).thenReturn(true);
		MicrobotPluginManager microbotPluginManager = mock(MicrobotPluginManager.class);
		when(microbotPluginManager.getInstalledPluginVersion(anyString())).thenReturn(Optional.empty());
		Client client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		doAnswer(invocation ->
		{
			invocation.<Runnable>getArgument(0).run();
			return null;
		}).when(executor).execute(any());
		reporter = new ClientAlertReporter(api, token, sessions, pluginManager, microbotPluginManager, client, executor);
	}

	private List<JsonObject> sent()
	{
		ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
		verify(api, atLeastOnce()).sendAlertEvent(any(), captor.capture(), any());
		return captor.getAllValues();
	}

	private List<JsonObject> sentOfType(String type)
	{
		return sent().stream().filter(json -> type.equals(json.get("type").getAsString())).collect(Collectors.toList());
	}

	@Test
	public void scriptStartReportsDisplayNameVersionAndRunningScripts()
	{
		reporter.onPluginChanged(new PluginChanged(script, true));

		JsonObject event = sentOfType(AlertEvent.SCRIPT_STARTED).get(0);
		assertEquals(SESSION.toString(), event.get("sessionId").getAsString());
		assertEquals("Fake Fisher", event.get("plugin").getAsString());
		assertEquals("2.1.0", event.get("pluginVersion").getAsString());
		assertEquals(1, event.getAsJsonArray("runningScripts").size());
		assertEquals("Fake Fisher", event.getAsJsonArray("runningScripts").get(0).getAsString());
		assertTrue(event.get("loggedIn").getAsBoolean());
		verify(api).sendAlertEvent(eq(TOKEN), any(), any());
	}

	@Test
	public void builtInPluginsAreNotScripts()
	{
		reporter.onPluginChanged(new PluginChanged(new BuiltInPlugin(), false));
		verify(api, never()).sendAlertEvent(any(), any(), any());
	}

	@Test
	public void stopRightAfterAnErrorFromTheScriptHasReasonError()
	{
		reporter.onScriptError(SCRIPT_LOGGER, List.of(), "Loop failed (IllegalStateException: bank closed)");
		reporter.onPluginChanged(new PluginChanged(script, false));

		JsonObject error = sentOfType(AlertEvent.SCRIPT_ERROR).get(0);
		assertEquals("Fake Fisher", error.get("plugin").getAsString());
		assertEquals("Loop failed (IllegalStateException: bank closed)", error.get("message").getAsString());

		JsonObject stopped = sentOfType(AlertEvent.SCRIPT_STOPPED).get(0);
		assertEquals(ScriptStopReason.ERROR, stopped.get("reason").getAsString());
		assertEquals("Loop failed (IllegalStateException: bank closed)", stopped.get("message").getAsString());
	}

	@Test
	public void errorAttributedThroughStackFrames()
	{
		reporter.onScriptError("net.runelite.client.plugins.microbot.Microbot",
			List.of("java.util.ArrayList.get:427", SCRIPT_LOGGER + ".run:88"), "FishingScript (IndexOutOfBoundsException)");

		assertEquals(1, sentOfType(AlertEvent.SCRIPT_ERROR).size());
	}

	@Test
	public void stopWithoutRecentErrorHasReasonUnknown()
	{
		reporter.onPluginChanged(new PluginChanged(script, false));

		JsonObject stopped = sentOfType(AlertEvent.SCRIPT_STOPPED).get(0);
		assertEquals(ScriptStopReason.UNKNOWN, stopped.get("reason").getAsString());
		assertTrue(stopped.get("message").isJsonNull());
	}

	@Test
	public void repeatedErrorsFromOneScriptAreSentOnce()
	{
		for (int i = 0; i < 5; i++)
		{
			reporter.onScriptError(SCRIPT_LOGGER, List.of(), "Loop failed");
		}
		verify(api, times(1)).sendAlertEvent(any(), any(), any());
	}

	@Test
	public void errorsOutsideMicrobotScriptsAreNotSent()
	{
		reporter.onScriptError("net.runelite.client.plugins.gpu.GpuPlugin", List.of("net.runelite.client.plugins.gpu.GpuPlugin.startUp:12"), "GL error");
		verify(api, never()).sendAlertEvent(any(), any(), any());
	}

	@Test
	public void logoutFromTheGameReportsLoggedOut()
	{
		reporter.onGameStateChanged(gameState(GameState.LOGGED_IN));
		reporter.onGameStateChanged(gameState(GameState.LOGIN_SCREEN));

		JsonObject event = sentOfType(AlertEvent.LOGGED_OUT).get(0);
		assertFalse(event.get("loggedIn").getAsBoolean());
		assertEquals(1, event.getAsJsonArray("runningScripts").size());
	}

	@Test
	public void onlyLeavingTheGameCountsAsLogout()
	{
		assertTrue(ClientAlertReporter.isLogout(GameState.LOGGED_IN, GameState.LOGIN_SCREEN));
		assertTrue(ClientAlertReporter.isLogout(GameState.CONNECTION_LOST, GameState.LOGIN_SCREEN));
		assertTrue(ClientAlertReporter.isLogout(GameState.HOPPING, GameState.LOGIN_SCREEN));
		assertFalse(ClientAlertReporter.isLogout(null, GameState.LOGIN_SCREEN));
		assertFalse(ClientAlertReporter.isLogout(GameState.LOGGING_IN, GameState.LOGIN_SCREEN));
		assertFalse(ClientAlertReporter.isLogout(GameState.LOGGED_IN, GameState.HOPPING));
		assertFalse(ClientAlertReporter.isLogout(GameState.LOGGED_IN, GameState.CONNECTION_LOST));
	}

	@Test
	public void nothingIsSentWithoutAToken()
	{
		when(token.get()).thenReturn(Optional.empty());
		reporter.onPluginChanged(new PluginChanged(script, true));
		reporter.onGameStateChanged(gameState(GameState.LOGGED_IN));
		reporter.onGameStateChanged(gameState(GameState.LOGIN_SCREEN));
		assertFalse(reporter.isActive());
		verify(api, never()).sendAlertEvent(any(), any(), any());
	}

	@Test
	public void nothingIsSentAfterClientShutdown()
	{
		reporter.onClientShutdown(new ClientShutdown());
		reporter.onPluginChanged(new PluginChanged(script, false));
		verify(api, never()).sendAlertEvent(any(), any(), any());
	}

	@Test
	public void unauthorizedResponseRejectsTheToken()
	{
		reporter.onPluginChanged(new PluginChanged(script, true));
		ArgumentCaptor<Runnable> onUnauthorized = ArgumentCaptor.forClass(Runnable.class);
		verify(api).sendAlertEvent(any(), any(), onUnauthorized.capture());

		onUnauthorized.getValue().run();
		verify(token).reject(TOKEN);
	}

	@Test
	public void scriptNamesDropAuthorPrefixAndTags()
	{
		assertEquals("Fake Fisher", ClientAlertReporter.scriptName(script));
		assertTrue(ClientAlertReporter.isScript(script));
		assertFalse(ClientAlertReporter.isScript(new BuiltInPlugin()));
		assertNull(ClientAlertReporter.attribute(
			new ClientAlertReporter.ScriptError(0, "other.Logger", List.of(), "x"), List.of(script)));
	}

	private static GameStateChanged gameState(GameState state)
	{
		GameStateChanged event = new GameStateChanged();
		event.setGameState(state);
		return event;
	}
}
