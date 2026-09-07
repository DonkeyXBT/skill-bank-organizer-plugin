package com.skillbankorganizer.ui;

import com.skillbankorganizer.SkillBankOrganizerPlugin;
import com.skillbankorganizer.data.BankStack;
import com.skillbankorganizer.data.Organizer;
import com.skillbankorganizer.data.Organizer.MethodStatus;
import com.skillbankorganizer.data.Organizer.NeedStatus;
import com.skillbankorganizer.data.Organizer.SkillPage;
import com.skillbankorganizer.data.Organizer.ToolStatus;
import com.skillbankorganizer.data.SkillCatalog;
import com.skillbankorganizer.data.SkillCatalog.TabDef;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.QuantityFormatter;

public class SkillBankPanel extends PluginPanel
{
	private static final Color OK = new Color(120, 160, 110);
	private static final Color MISS = new Color(180, 100, 85);
	private static final Color GOLD = new Color(196, 165, 116);

	private final SkillBankOrganizerPlugin plugin;
	private final SkillCatalog catalog;
	private final ItemManager itemManager;
	private final SkillIconManager skillIconManager;

	private final JPanel cards = new JPanel();
	private final JLabel statusLabel = new JLabel("Open your bank to scan.");
	private final JTextField search = new JTextField();
	private final JPanel homeList = new JPanel();
	private List<SkillPage> pages = new ArrayList<>();

	public SkillBankPanel(SkillBankOrganizerPlugin plugin, SkillCatalog catalog, ItemManager itemManager)
	{
		super(false);
		this.plugin = plugin;
		this.catalog = catalog;
		this.itemManager = itemManager;
		this.skillIconManager = new SkillIconManager();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setBorder(new EmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Skill Bank");
		title.setForeground(GOLD);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);

		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		statusLabel.setFont(FontManager.getRunescapeSmallFont());
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(statusLabel);
		header.add(Box.createVerticalStrut(6));

		JButton scan = new JButton("Scan bank");
		scan.setAlignmentX(Component.LEFT_ALIGNMENT);
		scan.setFocusPainted(false);
		scan.addActionListener(e -> plugin.requestScan());
		header.add(scan);
		header.add(Box.createVerticalStrut(6));

		JButton tagAll = new JButton("Tag bank for skill tabs");
		tagAll.setAlignmentX(Component.LEFT_ALIGNMENT);
		tagAll.setFocusPainted(false);
		tagAll.addActionListener(e -> plugin.requestTagAll());
		header.add(tagAll);
		header.add(Box.createVerticalStrut(6));

		search.setAlignmentX(Component.LEFT_ALIGNMENT);
		search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		search.putClientProperty("JTextField.placeholderText", "Filter skills…");
		search.addActionListener(e -> rebuildHome());
		search.getDocument().addDocumentListener(new SimpleDoc(this::rebuildHome));
		header.add(search);

		add(header, BorderLayout.NORTH);

		cards.setLayout(new BorderLayout());
		cards.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane scroll = new JScrollPane(cards);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		add(scroll, BorderLayout.CENTER);

		homeList.setLayout(new BoxLayout(homeList, BoxLayout.Y_AXIS));
		homeList.setBackground(ColorScheme.DARK_GRAY_COLOR);
		showEmpty("Open your bank, then press Scan.");
	}

	public void showEmpty(String message)
	{
		statusLabel.setText(message);
		pages = new ArrayList<>();
		rebuildHome();
	}

	public void showPages(List<SkillPage> next, int stackCount)
	{
		pages = next == null ? new ArrayList<>() : next;
		statusLabel.setText(stackCount + " stacks · " + pages.size() + " pages");
		rebuildHome();
	}

	public void setStatus(String message)
	{
		statusLabel.setText(message);
	}

	public void openSkill(String skillId)
	{
		if (pages.isEmpty())
		{
			pages = Organizer.build(catalog, plugin.lastScan());
		}
		for (SkillPage page : pages)
		{
			if (page.skill.id.equals(skillId))
			{
				showSkill(page);
				return;
			}
		}
		rebuildHome();
	}

	private void rebuildHome()
	{
		plugin.setHighlightedSkill(null);
		homeList.removeAll();
		String filter = search.getText() == null ? "" : search.getText().trim().toLowerCase();

		homeList.add(section("Rebuild tabs"));
		if (catalog.tabs != null)
		{
			for (TabDef tab : catalog.tabs)
			{
				JLabel row = new JLabel("Tab " + tab.id + "  " + tab.name);
				row.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				row.setFont(FontManager.getRunescapeSmallFont());
				row.setBorder(new EmptyBorder(2, 8, 2, 8));
				row.setAlignmentX(Component.LEFT_ALIGNMENT);
				homeList.add(row);
				JLabel skills = new JLabel(String.join(" · ", tab.skills).toLowerCase());
				skills.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
				skills.setFont(FontManager.getRunescapeSmallFont());
				skills.setBorder(new EmptyBorder(0, 8, 6, 8));
				skills.setAlignmentX(Component.LEFT_ALIGNMENT);
				homeList.add(skills);
			}
		}

		homeList.add(section("Skill pages"));
		boolean any = false;
		for (SkillPage page : pages)
		{
			if (plugin.config().hideEmptySkills() && page.stackCount() == 0 && !"OTHER".equals(page.skill.id))
			{
				continue;
			}
			if (!filter.isEmpty() && !page.skill.name.toLowerCase().contains(filter))
			{
				continue;
			}
			any = true;
			homeList.add(skillRow(page));
		}
		if (!any)
		{
			JLabel empty = new JLabel(pages.isEmpty()
				? "Nothing scanned yet."
				: "No skills match that filter.");
			empty.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			empty.setBorder(new EmptyBorder(8, 8, 8, 8));
			empty.setAlignmentX(Component.LEFT_ALIGNMENT);
			homeList.add(empty);
		}

		homeList.add(Box.createVerticalStrut(12));
		JLabel hint = new JLabel("<html>Right-click a bank item → Send to page. Plugins cannot drag items. That option tags it and opens the page so you can move it yourself.</html>");
		hint.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		hint.setFont(FontManager.getRunescapeSmallFont());
		hint.setBorder(new EmptyBorder(4, 8, 12, 8));
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);
		homeList.add(hint);

		showCard(homeList);
	}

	private JPanel skillRow(SkillPage page)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(6, 8, 6, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel icon = new JLabel(skillIcon(page.skill.rsSkill));
		row.add(icon, BorderLayout.WEST);

		JLabel name = new JLabel(page.skill.name);
		name.setForeground(Color.WHITE);
		name.setFont(FontManager.getRunescapeSmallFont());

		JLabel meta = new JLabel(page.stackCount() + "  ·  " + formatGp(page.geValue));
		meta.setForeground(page.missingCount > 0 ? MISS : OK);
		meta.setFont(FontManager.getRunescapeSmallFont());

		JPanel text = new JPanel(new GridLayout(2, 1));
		text.setOpaque(false);
		text.add(name);
		text.add(meta);
		row.add(text, BorderLayout.CENTER);

		row.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		row.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				showSkill(page);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				row.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});
		return row;
	}

	private void showSkill(SkillPage page)
	{
		plugin.setHighlightedSkill(page.skill.id);
		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		root.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton back = new JButton("← Skills");
		back.setAlignmentX(Component.LEFT_ALIGNMENT);
		back.setFocusPainted(false);
		back.addActionListener(e -> rebuildHome());
		root.add(pad(back));

		JLabel title = new JLabel(skillIcon(page.skill.rsSkill));
		title.setText("  " + page.skill.name);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(GOLD);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		root.add(pad(title));

		String tab = page.tab == null ? "any tab" : "tab " + page.tab.id + " · " + page.tab.name;
		JLabel meta = new JLabel(page.stackCount() + " stacks · " + formatGp(page.geValue) + " · " + tab);
		meta.setFont(FontManager.getRunescapeSmallFont());
		meta.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		meta.setAlignmentX(Component.LEFT_ALIGNMENT);
		root.add(pad(meta));

		JLabel blurb = new JLabel("<html>" + page.skill.blurb + "</html>");
		blurb.setFont(FontManager.getRunescapeSmallFont());
		blurb.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		blurb.setAlignmentX(Component.LEFT_ALIGNMENT);
		root.add(pad(blurb));

		if (!page.tools.isEmpty())
		{
			root.add(section("Tools — keep the best, drop the rest"));
			for (ToolStatus tool : page.tools)
			{
				root.add(itemLine(tool.tool.id, tool.tool.name, tool.qty,
					tool.owned ? (tool.bestOwned ? "best owned" : "owned") : "missing",
					tool.owned));
			}
		}

		if (!page.methods.isEmpty())
		{
			root.add(section("What you need to train"));
			for (MethodStatus method : page.methods)
			{
				JLabel head = new JLabel(method.method.name + "  (" + method.method.level + ")");
				head.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.PLAIN, 12f));
				head.setForeground(method.ready ? OK : GOLD);
				head.setAlignmentX(Component.LEFT_ALIGNMENT);
				root.add(pad(head));
				JLabel why = new JLabel("<html>" + method.method.why + "</html>");
				why.setFont(FontManager.getRunescapeSmallFont());
				why.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
				why.setAlignmentX(Component.LEFT_ALIGNMENT);
				root.add(pad(why));
				for (NeedStatus need : method.items)
				{
					String extra = need.need.isOptional() ? "optional" : (need.owned ? "have" : "need");
					if (need.need.hint != null && !need.owned)
					{
						extra = extra + " · " + need.need.hint;
					}
					int id = need.need.id == null ? -1 : need.need.id;
					root.add(itemLine(id, need.need.name, need.qty, extra, need.owned));
				}
			}
		}

		root.add(section("In your bank"));
		if (page.stacks.isEmpty())
		{
			JLabel none = new JLabel("Nothing for this skill yet.");
			none.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			none.setFont(FontManager.getRunescapeSmallFont());
			none.setAlignmentX(Component.LEFT_ALIGNMENT);
			root.add(pad(none));
		}
		else
		{
			for (BankStack stack : page.rebuild)
			{
				root.add(itemLine(stack.id, stack.name, stack.qty, formatGp(stack.geValue()), true));
			}
		}

		root.add(section("Rebuild order (top to bottom)"));
		JLabel how = new JLabel("<html>Drag these into " + tab + " in this order. Leave placeholders so the section stays put.</html>");
		how.setFont(FontManager.getRunescapeSmallFont());
		how.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		how.setAlignmentX(Component.LEFT_ALIGNMENT);
		root.add(pad(how));
		int n = 1;
		for (BankStack stack : page.rebuild)
		{
			JLabel step = new JLabel(n + ". " + stack.name + " × " + QuantityFormatter.quantityToStackSize(stack.qty));
			step.setFont(FontManager.getRunescapeSmallFont());
			step.setForeground(Color.WHITE);
			step.setAlignmentX(Component.LEFT_ALIGNMENT);
			root.add(pad(step));
			n++;
		}
		root.add(Box.createVerticalStrut(16));
		showCard(root);
	}

	private JPanel itemLine(int itemId, String name, int qty, String extra, boolean owned)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(4, 8, 4, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(24, 24));
		if (itemId > 0)
		{
			itemManager.getImage(itemId, Math.max(qty, 1), false).addTo(icon);
		}
		row.add(icon, BorderLayout.WEST);

		JLabel label = new JLabel(name);
		label.setForeground(owned ? Color.WHITE : MISS);
		label.setFont(FontManager.getRunescapeSmallFont());

		String right = extra;
		if (qty > 0)
		{
			right = QuantityFormatter.quantityToStackSize(qty) + " · " + extra;
		}
		JLabel meta = new JLabel(right);
		meta.setForeground(owned ? OK : MISS);
		meta.setFont(FontManager.getRunescapeSmallFont());
		meta.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel text = new JPanel(new GridLayout(2, 1));
		text.setOpaque(false);
		text.add(label);
		text.add(meta);
		row.add(text, BorderLayout.CENTER);
		return row;
	}

	private JLabel section(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(GOLD);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setBorder(new EmptyBorder(12, 8, 4, 8));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JPanel pad(Component child)
	{
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.setBorder(new EmptyBorder(2, 8, 2, 8));
		wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrap.add(child, BorderLayout.CENTER);
		wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, child.getPreferredSize().height + 8));
		return wrap;
	}

	private void showCard(JPanel content)
	{
		cards.removeAll();
		cards.add(content, BorderLayout.NORTH);
		cards.revalidate();
		cards.repaint();
	}

	private ImageIcon skillIcon(String rsSkill)
	{
		if (rsSkill == null)
		{
			return new ImageIcon();
		}
		try
		{
			Skill skill = Skill.valueOf(rsSkill);
			BufferedImage image = skillIconManager.getSkillImage(skill, true);
			return new ImageIcon(image);
		}
		catch (Exception ex)
		{
			return new ImageIcon();
		}
	}

	private static String formatGp(long value)
	{
		if (value >= 10_000_000L)
		{
			return String.format("%.1fm", value / 1_000_000d);
		}
		if (value >= 1_000_000L)
		{
			return String.format("%.2fm", value / 1_000_000d);
		}
		if (value >= 10_000L)
		{
			return (value / 1000L) + "k";
		}
		return Long.toString(value);
	}

	private static final class SimpleDoc implements javax.swing.event.DocumentListener
	{
		private final Runnable run;

		private SimpleDoc(Runnable run)
		{
			this.run = run;
		}

		@Override
		public void insertUpdate(javax.swing.event.DocumentEvent e)
		{
			run.run();
		}

		@Override
		public void removeUpdate(javax.swing.event.DocumentEvent e)
		{
			run.run();
		}

		@Override
		public void changedUpdate(javax.swing.event.DocumentEvent e)
		{
			run.run();
		}
	}
}
