package com.skillbankorganizer.ui;

import com.skillbankorganizer.SkillBankOrganizerPlugin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class SkillBankItemOverlay extends WidgetItemOverlay
{
	private static final Color GOLD = new Color(196, 165, 116, 220);

	private final SkillBankOrganizerPlugin plugin;

	public SkillBankItemOverlay(SkillBankOrganizerPlugin plugin)
	{
		this.plugin = plugin;
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!plugin.shouldHighlight(itemId))
		{
			return;
		}
		Rectangle bounds = widgetItem.getCanvasBounds();
		if (bounds == null)
		{
			return;
		}
		graphics.setColor(GOLD);
		graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
	}
}
