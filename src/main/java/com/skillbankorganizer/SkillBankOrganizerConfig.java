package com.skillbankorganizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("skillbankorganizer")
public interface SkillBankOrganizerConfig extends Config
{
	@ConfigItem(
		keyName = "autoScan",
		name = "Scan when bank opens",
		description = "Refresh the skill pages whenever your bank contents change.",
		position = 0
	)
	default boolean autoScan()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includePlaceholders",
		name = "Include placeholders",
		description = "Count placeholder slots as items you own (quantity 0).",
		position = 1
	)
	default boolean includePlaceholders()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideEmptySkills",
		name = "Hide empty skills",
		description = "Hide skill pages that currently have nothing in the bank.",
		position = 2
	)
	default boolean hideEmptySkills()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showChatScan",
		name = "Chat message on scan",
		description = "Write a game-message when a bank scan finishes.",
		position = 3
	)
	default boolean showChatScan()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rightClickSend",
		name = "Right-click: Send to page",
		description = "Add a Send-to-page option on bank items. Plugins cannot drag items; this opens the page and tags it.",
		position = 4
	)
	default boolean rightClickSend()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tagOnSend",
		name = "Tag item on Send",
		description = "Write a Bank Tags tag (e.g. #woodcutting) so you can filter that skill in the bank.",
		position = 5
	)
	default boolean tagOnSend()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightPage",
		name = "Highlight current page in bank",
		description = "Draw a border on bank items that belong to the skill page you have open.",
		position = 6
	)
	default boolean highlightPage()
	{
		return true;
	}
}
