package com.skillbankorganizer;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.skillbankorganizer.data.BankStack;
import com.skillbankorganizer.data.Organizer;
import com.skillbankorganizer.data.SkillCatalog;
import com.skillbankorganizer.data.SkillCatalog.SkillDef;
import com.skillbankorganizer.ui.SkillBankItemOverlay;
import com.skillbankorganizer.ui.SkillBankPanel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Skill Bank Organizer",
	description = "Scan your bank into a page per skill. Right-click Send-to-page tags the item; it never drags it for you.",
	tags = {"bank", "skills", "organizer", "skilling", "raids", "woodcutting", "magic"}
)
public class SkillBankOrganizerPlugin extends Plugin
{
	private static final String BANK_TAGS_GROUP = "banktags";
	private static final String ITEM_TAG_PREFIX = "item_";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SkillBankOrganizerConfig config;

	@Inject
	private Gson gson;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	private SkillCatalog catalog;
	private SkillBankPanel panel;
	private NavigationButton navButton;
	private SkillBankItemOverlay overlay;
	private List<BankStack> lastScan = new ArrayList<>();
	private boolean hasScan;
	private String highlightedSkillId;

	@Override
	protected void startUp()
	{
		catalog = SkillCatalog.load(gson);
		panel = new SkillBankPanel(this, catalog, itemManager);
		overlay = new SkillBankItemOverlay(this);
		overlayManager.add(overlay);
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/com/skillbankorganizer/icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Skill Bank Organizer")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		log.debug("Skill Bank Organizer started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
		lastScan = new ArrayList<>();
		hasScan = false;
		highlightedSkillId = null;
		navButton = null;
		panel = null;
		overlay = null;
		log.debug("Skill Bank Organizer stopped");
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!config.autoScan())
		{
			return;
		}
		if (event.getContainerId() != InventoryID.BANK)
		{
			return;
		}
		scanContainer(event.getItemContainer());
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.rightClickSend())
		{
			return;
		}
		String option = event.getOption();
		if (option == null || (!option.startsWith("Withdraw-") && !option.startsWith("Deposit-")))
		{
			return;
		}
		int rawId = event.getItemId();
		if (rawId <= 0)
		{
			return;
		}
		int itemId = itemManager.canonicalize(rawId);
		ItemComposition composition = itemManager.getItemComposition(itemId);
		String name = composition.getName();
		if (name == null || name.equals("null"))
		{
			return;
		}
		BankStack stack = new BankStack(itemId, name, 1, 0);
		List<SkillDef> skills = Organizer.skillsFor(catalog, stack);
		if (skills.isEmpty())
		{
			SkillDef other = otherSkill();
			if (other != null)
			{
				skills = new ArrayList<>();
				skills.add(other);
			}
		}
		int shown = 0;
		for (SkillDef skill : skills)
		{
			if (shown >= 3)
			{
				break;
			}
			client.getMenu().createMenuEntry(-1)
				.setOption("Send to " + skill.name)
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e -> sendToSkill(skill, itemId, name));
			shown++;
		}
	}

	public void requestScan()
	{
		clientThread.invoke(() -> scanContainer(client.getItemContainer(InventoryID.BANK)));
	}

	public void requestTagAll()
	{
		int n = 0;
		for (BankStack stack : lastScan)
		{
			for (SkillDef skill : Organizer.skillsFor(catalog, stack))
			{
				tagItem(stack.id, skill);
				n++;
			}
		}
		final int tagged = n;
		if (config.showChatScan())
		{
			clientThread.invoke(() -> client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"Skill Bank Organizer: tagged " + tagged + " item-skill pairs. Search #woodcutting etc. in the bank.",
				null));
		}
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setStatus("Tagged " + tagged + " entries. Search #raids, #woodcutting, …");
			}
		});
	}

	public boolean hasScan()
	{
		return hasScan;
	}

	public List<BankStack> lastScan()
	{
		return lastScan;
	}

	public SkillBankOrganizerConfig config()
	{
		return config;
	}

	public void setHighlightedSkill(String skillId)
	{
		highlightedSkillId = skillId;
	}

	public boolean shouldHighlight(int itemId)
	{
		if (!config.highlightPage() || highlightedSkillId == null || catalog == null)
		{
			return false;
		}
		int canonical = itemManager.canonicalize(itemId);
		ItemComposition composition = itemManager.getItemComposition(canonical);
		String name = composition.getName();
		if (name == null)
		{
			return false;
		}
		BankStack stack = new BankStack(canonical, name, 1, 0);
		List<SkillDef> skills = Organizer.skillsFor(catalog, stack);
		if ("OTHER".equals(highlightedSkillId))
		{
			return skills.isEmpty();
		}
		for (SkillDef skill : skills)
		{
			if (highlightedSkillId.equals(skill.id))
			{
				return true;
			}
		}
		return false;
	}

	private void sendToSkill(SkillDef skill, int itemId, String name)
	{
		if (config.tagOnSend())
		{
			tagItem(itemId, skill);
		}
		highlightedSkillId = skill.id;
		String tab = "";
		if (catalog.tabFor(skill.id) != null)
		{
			tab = " · tab " + catalog.tabFor(skill.id).id + " " + catalog.tabFor(skill.id).name;
		}
		final String message = name + " → " + skill.name + tab + ". Tagged. Drag it yourself — plugins cannot move bank items.";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.openSkill(skill.id);
			}
		});
		log.debug("Send to {} item {}", skill.id, itemId);
	}

	private void tagItem(int itemId, SkillDef skill)
	{
		String tag = Organizer.tagName(skill);
		String key = ITEM_TAG_PREFIX + itemId;
		String current = configManager.getConfiguration(BANK_TAGS_GROUP, key);
		Set<String> tags = new LinkedHashSet<>();
		if (current != null && !current.isEmpty())
		{
			for (String part : current.split(","))
			{
				String trimmed = part.trim();
				if (!trimmed.isEmpty())
				{
					tags.add(trimmed);
				}
			}
		}
		tags.add(tag);
		configManager.setConfiguration(BANK_TAGS_GROUP, key, String.join(",", tags));
	}

	private SkillDef otherSkill()
	{
		for (SkillDef skill : catalog.skillsOrEmpty())
		{
			if ("OTHER".equals(skill.id))
			{
				return skill;
			}
		}
		return null;
	}

	private void scanContainer(ItemContainer container)
	{
		if (container == null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.showEmpty("Open your bank, then press Scan.");
				}
			});
			return;
		}

		List<BankStack> stacks = new ArrayList<>();
		for (Item item : container.getItems())
		{
			if (item == null || item.getId() <= 0)
			{
				continue;
			}
			int canonical = itemManager.canonicalize(item.getId());
			ItemComposition composition = itemManager.getItemComposition(canonical);
			String name = composition.getName();
			if (name == null || name.equals("null") || name.equals("Bank filler"))
			{
				continue;
			}
			int qty = item.getQuantity();
			if (qty <= 0 && !config.includePlaceholders())
			{
				continue;
			}
			int price = itemManager.getItemPrice(canonical);
			stacks.add(new BankStack(canonical, name, Math.max(qty, 0), price));
		}

		lastScan = stacks;
		hasScan = true;
		List<Organizer.SkillPage> pages = Organizer.build(catalog, stacks);
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.showPages(pages, stacks.size());
			}
		});

		if (config.showChatScan())
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"Skill Bank Organizer: " + stacks.size() + " stacks sorted into skill pages.",
				null);
		}
		log.debug("Scanned {} bank stacks", stacks.size());
	}

	@Provides
	SkillBankOrganizerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkillBankOrganizerConfig.class);
	}
}
