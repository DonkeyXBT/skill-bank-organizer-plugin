package com.skillbankorganizer.data;

import com.skillbankorganizer.data.SkillCatalog.Matcher;
import com.skillbankorganizer.data.SkillCatalog.MethodDef;
import com.skillbankorganizer.data.SkillCatalog.NeedDef;
import com.skillbankorganizer.data.SkillCatalog.SkillDef;
import com.skillbankorganizer.data.SkillCatalog.TabDef;
import com.skillbankorganizer.data.SkillCatalog.ToolDef;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Organizer
{
	private Organizer()
	{
	}

	public static List<SkillPage> build(SkillCatalog catalog, List<BankStack> stacks)
	{
		List<BankStack> safe = stacks == null ? new ArrayList<>() : stacks;
		Set<Integer> claimed = new HashSet<>();
		List<SkillPage> pages = new ArrayList<>();
		SkillDef other = null;

		for (SkillDef skill : catalog.skillsOrEmpty())
		{
			if ("OTHER".equals(skill.id))
			{
				other = skill;
				continue;
			}
			pages.add(pageFor(catalog, skill, safe, claimed, false));
		}

		if (other != null)
		{
			List<BankStack> leftovers = new ArrayList<>();
			for (BankStack stack : safe)
			{
				if (!claimed.contains(stack.id))
				{
					leftovers.add(stack);
				}
			}
			pages.add(pageFor(catalog, other, leftovers, claimed, true));
		}
		return pages;
	}

	public static List<SkillDef> skillsFor(SkillCatalog catalog, BankStack stack)
	{
		List<SkillDef> hit = new ArrayList<>();
		for (SkillDef skill : catalog.skillsOrEmpty())
		{
			if ("OTHER".equals(skill.id))
			{
				continue;
			}
			if (matchesSkill(skill, stack))
			{
				hit.add(skill);
			}
		}
		return hit;
	}

	public static String tagName(SkillDef skill)
	{
		if (skill == null || skill.name == null)
		{
			return "other";
		}
		return skill.name.toLowerCase().replace(' ', '-');
	}

	private static SkillPage pageFor(SkillCatalog catalog, SkillDef skill, List<BankStack> stacks,
		Set<Integer> claimed, boolean takeAll)
	{
		List<BankStack> owned = new ArrayList<>();
		for (BankStack stack : stacks)
		{
			if (takeAll || matchesSkill(skill, stack))
			{
				owned.add(stack);
				claimed.add(stack.id);
			}
		}

		List<ToolStatus> tools = new ArrayList<>();
		if (skill.tools != null)
		{
			for (ToolDef tool : skill.tools)
			{
				BankStack hit = find(owned, tool.id, tool.name);
				if (hit == null)
				{
					hit = find(stacks, tool.id, tool.name);
				}
				tools.add(new ToolStatus(tool, hit != null, hit == null ? 0 : hit.qty, false));
			}
		}
		for (int i = tools.size() - 1; i >= 0; i--)
		{
			if (tools.get(i).owned)
			{
				tools.get(i).bestOwned = true;
				break;
			}
		}

		List<MethodStatus> methods = new ArrayList<>();
		int missing = 0;
		if (skill.methods != null)
		{
			for (MethodDef method : skill.methods)
			{
				List<NeedStatus> needs = new ArrayList<>();
				boolean ready = true;
				if (method.items != null)
				{
					for (NeedDef need : method.items)
					{
						int id = need.id == null ? -1 : need.id;
						BankStack hit = find(stacks, id, need.name);
						boolean has = hit != null;
						needs.add(new NeedStatus(need, has, hit == null ? 0 : hit.qty));
						if (!need.isOptional() && !has)
						{
							ready = false;
							missing++;
						}
					}
				}
				methods.add(new MethodStatus(method, needs, ready));
			}
		}

		Set<Integer> toolIds = new HashSet<>();
		if (skill.tools != null)
		{
			for (ToolDef tool : skill.tools)
			{
				toolIds.add(tool.id);
			}
		}
		List<BankStack> rebuild = new ArrayList<>(owned);
		rebuild.sort(Comparator
			.comparingInt((BankStack s) -> toolIds.contains(s.id) ? 0 : 1)
			.thenComparingLong(BankStack::geValue).reversed());

		TabDef tab = catalog.tabFor(skill.id);
		long ge = 0;
		int qty = 0;
		for (BankStack stack : owned)
		{
			ge += stack.geValue();
			qty += stack.qty;
		}

		return new SkillPage(skill, tab, owned, tools, methods, rebuild, missing, ge, qty);
	}

	static boolean matchesSkill(SkillDef skill, BankStack stack)
	{
		if (skill == null || skill.matchAny == null || "OTHER".equals(skill.id))
		{
			return false;
		}
		for (Matcher matcher : skill.matchAny)
		{
			if (matches(stack, matcher))
			{
				return true;
			}
		}
		return false;
	}

	static boolean matches(BankStack stack, Matcher matcher)
	{
		if (matcher == null)
		{
			return false;
		}
		String name = stack.name.toLowerCase();
		boolean idHit = matcher.ids != null && matcher.ids.contains(stack.id);
		boolean containsHit = false;
		if (matcher.nameContains != null)
		{
			for (String token : matcher.nameContains)
			{
				if (token != null && name.contains(token.toLowerCase()))
				{
					containsHit = true;
					break;
				}
			}
		}
		if (!idHit && !containsHit)
		{
			return false;
		}
		if (matcher.nameExclude != null)
		{
			for (String token : matcher.nameExclude)
			{
				if (token != null && name.contains(token.toLowerCase()))
				{
					return false;
				}
			}
		}
		return true;
	}

	static BankStack find(List<BankStack> stacks, int id, String name)
	{
		if (stacks == null)
		{
			return null;
		}
		if (id > 0)
		{
			for (BankStack stack : stacks)
			{
				if (stack.id == id)
				{
					return stack;
				}
			}
		}
		if (name == null || name.isEmpty())
		{
			return null;
		}
		String needle = name.toLowerCase();
		for (BankStack stack : stacks)
		{
			String n = stack.name.toLowerCase();
			if (n.equals(needle) || n.contains(needle))
			{
				return stack;
			}
		}
		return null;
	}

	public static final class ToolStatus
	{
		public final ToolDef tool;
		public final boolean owned;
		public final int qty;
		public boolean bestOwned;

		ToolStatus(ToolDef tool, boolean owned, int qty, boolean bestOwned)
		{
			this.tool = tool;
			this.owned = owned;
			this.qty = qty;
			this.bestOwned = bestOwned;
		}
	}

	public static final class NeedStatus
	{
		public final NeedDef need;
		public final boolean owned;
		public final int qty;

		NeedStatus(NeedDef need, boolean owned, int qty)
		{
			this.need = need;
			this.owned = owned;
			this.qty = qty;
		}
	}

	public static final class MethodStatus
	{
		public final MethodDef method;
		public final List<NeedStatus> items;
		public final boolean ready;

		MethodStatus(MethodDef method, List<NeedStatus> items, boolean ready)
		{
			this.method = method;
			this.items = items;
			this.ready = ready;
		}
	}

	public static final class SkillPage
	{
		public final SkillDef skill;
		public final TabDef tab;
		public final List<BankStack> stacks;
		public final List<ToolStatus> tools;
		public final List<MethodStatus> methods;
		public final List<BankStack> rebuild;
		public final int missingCount;
		public final long geValue;
		public final int totalQty;

		SkillPage(SkillDef skill, TabDef tab, List<BankStack> stacks, List<ToolStatus> tools,
			List<MethodStatus> methods, List<BankStack> rebuild, int missingCount, long geValue, int totalQty)
		{
			this.skill = skill;
			this.tab = tab;
			this.stacks = stacks;
			this.tools = tools;
			this.methods = methods;
			this.rebuild = rebuild;
			this.missingCount = missingCount;
			this.geValue = geValue;
			this.totalQty = totalQty;
		}

		public int stackCount()
		{
			return stacks.size();
		}
	}
}
