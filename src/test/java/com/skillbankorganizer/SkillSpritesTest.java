package com.skillbankorganizer;

import com.google.gson.Gson;
import com.skillbankorganizer.data.SkillCatalog;
import com.skillbankorganizer.data.SkillCatalog.SkillDef;
import com.skillbankorganizer.data.SkillSprites;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SkillSpritesTest
{
	private final SkillCatalog catalog = SkillCatalog.load(new Gson());

	@Test
	public void catalogLoads()
	{
		assertFalse("catalog should ship with skills", catalog.skillsOrEmpty().isEmpty());
	}

	/**
	 * Every page drawn by the panel needs an icon, so a skill added to the catalog without a
	 * matching sprite should fail here rather than render as a blank gap in the sidebar.
	 */
	@Test
	public void everySkillHasASprite()
	{
		for (SkillDef skill : catalog.skillsOrEmpty())
		{
			assertNotEquals("no sprite mapped for skill id " + skill.id,
				SkillSprites.NONE, SkillSprites.spriteFor(skill.id));
		}
	}

	@Test
	public void spritesAreDistinct()
	{
		Set<Integer> seen = new HashSet<>();
		for (SkillDef skill : catalog.skillsOrEmpty())
		{
			int sprite = SkillSprites.spriteFor(skill.id);
			assertTrue("sprite " + sprite + " reused by " + skill.id, seen.add(sprite));
		}
		assertEquals(catalog.skillsOrEmpty().size(), seen.size());
	}

	@Test
	public void unknownSkillHasNoSprite()
	{
		assertEquals(SkillSprites.NONE, SkillSprites.spriteFor("NOT_A_SKILL"));
		assertEquals(SkillSprites.NONE, SkillSprites.spriteFor(null));
	}
}
