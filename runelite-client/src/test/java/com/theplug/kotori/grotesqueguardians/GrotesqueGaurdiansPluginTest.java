package com.theplug.kotori.grotesqueguardians;

import net.runelite.client.plugins.kotori.grotesqueguardians.GrotesqueGuardiansPlugin;
import net.runelite.client.plugins.kotori.kotoriutils.KotoriUtils;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GrotesqueGaurdiansPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GrotesqueGuardiansPlugin.class, KotoriUtils.class);
		RuneLite.main(args);
	}
}