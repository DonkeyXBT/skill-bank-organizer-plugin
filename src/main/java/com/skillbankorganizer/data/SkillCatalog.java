package com.skillbankorganizer.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SkillCatalog
{
	public int version;
	public List<TabDef> tabs = new ArrayList<>();
	public List<SkillDef> skills = new ArrayList<>();

	public static SkillCatalog load(Gson gson)
	{
		InputStream in = SkillCatalog.class.getResourceAsStream("/com/skillbankorganizer/skill-catalog.json");
		if (in == null)
		{
			throw new IllegalStateException("skill-catalog.json missing from plugin resources");
		}
		try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8))
		{
			SkillCatalog catalog = gson.fromJson(reader, SkillCatalog.class);
			if (catalog == null || catalog.skills == null || catalog.skills.isEmpty())
			{
				throw new IllegalStateException("skill-catalog.json did not parse");
			}
			if (catalog.tabs == null)
			{
				catalog.tabs = new ArrayList<>();
			}
			return catalog;
		}
		catch (Exception ex)
		{
			if (ex instanceof IllegalStateException)
			{
				throw (IllegalStateException) ex;
			}
			throw new IllegalStateException("Failed to read skill-catalog.json", ex);
		}
	}

	public static final class TabDef
	{
		public int id;
		public String name;
		public List<String> skills = new ArrayList<>();
	}

	public static final class SkillDef
	{
		public String id;
		public String name;
		public String rsSkill;
		public String blurb;
		public List<ToolDef> tools = new ArrayList<>();
		public List<MethodDef> methods = new ArrayList<>();
		public List<Matcher> matchAny = new ArrayList<>();
	}

	public static final class ToolDef
	{
		public int id;
		public String name;
		public int level;
	}

	public static final class MethodDef
	{
		public String name;
		public String level;
		public String why;
		public List<NeedDef> items = new ArrayList<>();
	}

	public static final class NeedDef
	{
		public Integer id;
		public String name;
		public Boolean optional;
		public String hint;

		public boolean isOptional()
		{
			return Boolean.TRUE.equals(optional);
		}
	}

	public static final class Matcher
	{
		public List<Integer> ids;
		@SerializedName("nameContains")
		public List<String> nameContains;
		@SerializedName("nameExclude")
		public List<String> nameExclude;
	}

	public TabDef tabFor(String skillId)
	{
		if (skillId == null)
		{
			return null;
		}
		for (TabDef tab : tabs)
		{
			if (tab.skills != null && tab.skills.contains(skillId))
			{
				return tab;
			}
		}
		return null;
	}

	public List<SkillDef> skillsOrEmpty()
	{
		return skills == null ? Collections.emptyList() : skills;
	}
}
