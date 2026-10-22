package com.microbot.hwid;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.logging.Logger;

public final class HwidAgent
{
	private static final Logger log = Logger.getLogger(HwidAgent.class.getName());

	public static final String TOGGLE_PROPERTY = "microbot.hwid.spoof";

	private static volatile boolean deviceHookInstalled;
	private static volatile byte[] injectedRandomDat;
	private static volatile byte[] injectedSnapshot;

	private HwidAgent()
	{
	}

	public static void premain(String args, Instrumentation inst)
	{
		try
		{
			inst.addTransformer(new PlatformInfoTransformer());
			inst.addTransformer(new RandomDatTransformer());
		}
		catch (Throwable t)
		{
			log.severe("hwid-agent failed to register transformer: " + t);
		}
	}

	static void markDeviceHookInstalled()
	{
		deviceHookInstalled = true;
	}

	public static boolean isDeviceHookInstalled()
	{
		return deviceHookInstalled;
	}

	public static String deviceId(int osType)
	{
		try
		{
			if (!spoofEnabled())
			{
				return VanillaDeviceId.getDeviceId(osType);
			}

			return DeviceIdStore.getOrCreate(currentIdentifier());
		}
		catch (Throwable t)
		{
			log.warning("hwid-agent device id fallback: " + t);
			return VanillaDeviceId.getDeviceId(osType);
		}
	}

	public static boolean spoofEnabled()
	{
		return Boolean.parseBoolean(System.getProperty(TOGGLE_PROPERTY, "true"));
	}

	public static byte[] randomDatField(byte[] current)
	{
		try
		{
			if (current != null && (current != injectedRandomDat || !Arrays.equals(current, injectedSnapshot)))
			{
				return current;
			}
			if (!spoofEnabled())
			{
				injectedRandomDat = null;
				injectedSnapshot = null;
				return null;
			}
			byte[] data = RandomDatStore.getOrCreate(currentIdentifier());
			if (current != null && Arrays.equals(current, data))
			{
				return current;
			}
			byte[] injected = data.clone();
			injectedSnapshot = data.clone();
			injectedRandomDat = injected;
			return injected;
		}
		catch (Throwable t)
		{
			log.warning("hwid-agent random.dat fallback: " + t);
			return current;
		}
	}

	static String currentIdentifier()
	{
		String username = vanillaUsername();
		if (username != null && !username.isBlank())
		{
			return "user/" + username.trim();
		}

		String jxCharacterId = System.getenv("JX_CHARACTER_ID");
		if (jxCharacterId != null && !jxCharacterId.isBlank())
		{
			return "jx/" + jxCharacterId.trim();
		}

		return "default";
	}

	private static String vanillaUsername()
	{
		try
		{
			Class<?> api = Class.forName("net.runelite.api.Client", false, HwidAgent.class.getClassLoader());
			Class<?> gameclient = Class.forName("client", false, HwidAgent.class.getClassLoader());

			for (java.lang.reflect.Field field : gameclient.getDeclaredFields())
			{
				int modifiers = field.getModifiers();
				if (!java.lang.reflect.Modifier.isStatic(modifiers))
				{
					continue;
				}
				if (java.lang.reflect.Modifier.isFinal(modifiers) || field.getType().isPrimitive())
				{
					continue;
				}
				field.setAccessible(true);
				Object value = field.get(null);
				if (value == null || !api.isInstance(value))
				{
					continue;
				}
				Object username = api.getMethod("getUsername").invoke(value);
				return username instanceof String ? (String) username : null;
			}
		}
		catch (Throwable ignored)
		{
		}
		return null;
	}
}
