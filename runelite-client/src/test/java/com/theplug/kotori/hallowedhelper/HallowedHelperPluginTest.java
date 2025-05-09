package com.theplug.kotori.hallowedhelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.kotori.hallowedhelper.HallowedHelperPlugin;

public class HallowedHelperPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HallowedHelperPlugin.class);
		RuneLite.main(args);
	}
}