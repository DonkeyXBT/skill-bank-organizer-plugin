package com.skillbankorganizer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SkillBankOrganizerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SkillBankOrganizerPlugin.class);
		RuneLite.main(args);
	}
}
