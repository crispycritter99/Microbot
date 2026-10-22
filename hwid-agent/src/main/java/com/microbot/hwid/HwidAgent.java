package com.microbot.hwid;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

public final class HwidAgent
{
	private static final Logger log = Logger.getLogger(HwidAgent.class.getName());

	public static final String TOGGLE_PROPERTY = "microbot.hwid.spoof";

	private static volatile boolean deviceHookInstalled;

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

	public static byte[] randomData(byte flag)
	{
		try
		{
			if (!spoofEnabled())
			{
				return vanillaRandomDat(flag);
			}
			byte[] data = RandomDatStore.getOrCreate(currentIdentifier());
			return data.clone();
		}
		catch (Throwable t)
		{
			log.warning("hwid-agent random.dat fallback: " + t);
			return vanillaRandomDat(flag);
		}
	}

	private static byte[] vanillaRandomDat(byte flag)
	{
		String owner = RandomDatTransformer.providerOwner();
		String name = RandomDatTransformer.providerName();
		if (owner == null || name == null)
		{
			return RandomDatStore.fallbackBytes();
		}
		try
		{
			String dotted = owner.replace('/', '.');
			Class<?> provider = Class.forName(dotted, false, HwidAgent.class.getClassLoader());
			Object result = provider.getMethod(name, byte.class).invoke(null, flag);
			return result instanceof byte[] ? (byte[]) result : RandomDatStore.fallbackBytes();
		}
		catch (Throwable t)
		{
			log.warning("hwid-agent random.dat passthrough fallback: " + t);
			return RandomDatStore.fallbackBytes();
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
