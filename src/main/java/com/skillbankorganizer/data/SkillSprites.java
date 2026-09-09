package com.skillbankorganizer.data;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.gameval.SpriteID;

/**
 * Maps a catalog skill id onto the game sprite the client already ships for it, so every icon in
 * the panel comes from the cache rather than from bundled artwork.
 *
 * <p>The stats tab splits its icons across two sprite sheets, and the two pages that are not real
 * skills borrow an interface icon each: Raids uses the raiding party side icon, and Uncategorized
 * uses the bank's "all items" tab icon.
 */
public final class SkillSprites
{
	/** Returned when a skill has no sprite, so callers can skip the icon instead of drawing a gap. */
	public static final int NONE = -1;

	private static final ImmutableMap<String, Integer> SPRITES = ImmutableMap.<String, Integer>builder()
		.put("ATTACK", SpriteID.Staticons.ATTACK)
		.put("STRENGTH", SpriteID.Staticons.STRENGTH)
		.put("DEFENCE", SpriteID.Staticons.DEFENCE)
		.put("RANGED", SpriteID.Staticons.RANGED)
		.put("PRAYER", SpriteID.Staticons.PRAYER)
		.put("MAGIC", SpriteID.Staticons.MAGIC)
		.put("HITPOINTS", SpriteID.Staticons.HITPOINTS)
		.put("AGILITY", SpriteID.Staticons.AGILITY)
		.put("HERBLORE", SpriteID.Staticons.HERBLORE)
		.put("THIEVING", SpriteID.Staticons.THIEVING)
		.put("CRAFTING", SpriteID.Staticons.CRAFTING)
		.put("FLETCHING", SpriteID.Staticons.FLETCHING)
		.put("MINING", SpriteID.Staticons.MINING)
		.put("SMITHING", SpriteID.Staticons.SMITHING)
		.put("FISHING", SpriteID.Staticons.FISHING)
		.put("COOKING", SpriteID.Staticons.COOKING)
		.put("FIREMAKING", SpriteID.Staticons.FIREMAKING)
		.put("WOODCUTTING", SpriteID.Staticons.WOODCUTTING)
		.put("RUNECRAFT", SpriteID.Staticons2.RUNECRAFT)
		.put("SLAYER", SpriteID.Staticons2.SLAYER)
		.put("FARMING", SpriteID.Staticons2.FARMING)
		.put("HUNTER", SpriteID.Staticons2.HUNTER)
		.put("CONSTRUCTION", SpriteID.Staticons2.CONSTRUCTION)
		.put("RAIDS", SpriteID.SideiconsInterface.RAIDING_PARTY)
		.put("OTHER", SpriteID.BanktabIcons.ALL_ITEMS)
		.build();

	private SkillSprites()
	{
	}

	/**
	 * @return the sprite id for that catalog skill, or {@link #NONE} if the catalog has grown a
	 * skill this map has not been taught yet.
	 */
	public static int spriteFor(String skillId)
	{
		if (skillId == null)
		{
			return NONE;
		}
		return SPRITES.getOrDefault(skillId, NONE);
	}
}
