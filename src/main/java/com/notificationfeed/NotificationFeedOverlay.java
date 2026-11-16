package com.notificationfeed;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

class NotificationFeedOverlay extends Overlay
{
	private static final int DEFAULT_WIDTH = 225;
	private static final int MIN_WIDTH = 80;
	private static final int PADDING = 4;
	/**
	 * Vertical gap between entries: between boxes in card style, between text blocks in panel style.
	 */
	private static final int ENTRY_GAP = 2;
	private static final long FADE_MS = 1000;
	private static final String EMPTY_TEXT = "No notifications";
	private static final String TARGET = "Notification feed";
	private static final Font COUNT_FONT = FontManager.getRunescapeSmallFont();

	private final Client client;
	private final NotificationFeed feed;
	private final NotificationFeedConfig config;
	private final BackgroundComponent background = new BackgroundComponent();
	private CustomTimestampFormat customTimestampFormat = CustomTimestampFormat.compile(null);

	@Inject
	NotificationFeedOverlay(NotificationFeedPlugin plugin, Client client, NotificationFeed feed, NotificationFeedConfig config)
	{
		super(plugin);
		this.client = client;
		this.feed = feed;
		this.config = config;

		setPosition(OverlayPosition.TOP_LEFT);
		setResizable(true);
		setMinimumSize(MIN_WIDTH);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Clear", TARGET, e -> feed.clear());
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, TARGET);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		long now = System.currentTimeMillis();
		long durationMs = config.duration() * 1000L;
		List<FeedEntry> entries = feed.snapshot(now, durationMs, config.maxEntries());

		g.setFont(config.font().getFont());
		FontMetrics fm = g.getFontMetrics();
		int width = getPreferredSize() != null ? Math.max(getPreferredSize().width, MIN_WIDTH) : DEFAULT_WIDTH;

		if (entries.isEmpty())
		{
			// Holding Alt puts overlays in move mode; an overlay that renders nothing can't be grabbed
			if (!config.showWhenEmpty() && !client.isKeyPressed(KeyCode.KC_ALT))
			{
				return null;
			}
			return renderEmpty(g, fm, width);
		}

		if (config.newestFirst())
		{
			Collections.reverse(entries);
		}

		List<Block> blocks = layout(entries, fm, g.getFontMetrics(COUNT_FONT), width - 2 * PADDING, now, durationMs);
		int lineHeight = fm.getHeight();

		if (config.style() == FeedStyle.PANEL)
		{
			int height = 2 * PADDING + (blocks.size() - 1) * ENTRY_GAP;
			for (Block block : blocks)
			{
				height += block.lines.size() * lineHeight;
			}
			drawBackground(g, 0, width, height);

			int y = PADDING;
			for (Block block : blocks)
			{
				int top = y;
				withAlpha(g, block.alpha, () -> drawBlock(g, fm, block, PADDING, top));
				y += block.lines.size() * lineHeight + ENTRY_GAP;
			}
			return new Dimension(width, height);
		}

		int y = 0;
		for (Block block : blocks)
		{
			int height = block.lines.size() * lineHeight + 2 * PADDING;
			int top = y;
			withAlpha(g, block.alpha, () ->
			{
				drawBackground(g, top, width, height);
				drawBlock(g, fm, block, PADDING, top + PADDING);
			});
			y += height + ENTRY_GAP;
		}
		return new Dimension(width, y - ENTRY_GAP);
	}

	private Dimension renderEmpty(Graphics2D g, FontMetrics fm, int width)
	{
		int height = fm.getHeight() + 2 * PADDING;
		drawBackground(g, 0, width, height);
		String text = TextWrapper.truncate(EMPTY_TEXT, fm, width - 2 * PADDING);
		drawText(g, text, PADDING, PADDING + fm.getAscent(), Color.GRAY);
		return new Dimension(width, height);
	}

	private List<Block> layout(List<FeedEntry> entries, FontMetrics fm, FontMetrics countFm, int textWidth,
		long now, long durationMs)
	{
		TimestampStyle timestampStyle = config.timestampStyle();
		String customPattern = config.customTimestampFormat();
		if (!Objects.equals(customPattern, customTimestampFormat.getPattern()))
		{
			customTimestampFormat = CustomTimestampFormat.compile(customPattern);
		}
		int spaceWidth = fm.stringWidth(" ");
		List<Block> blocks = new ArrayList<>(entries.size());

		for (FeedEntry entry : entries)
		{
			String timestamp = timestampStyle.format(entry.getTimestamp(), now, customTimestampFormat);
			String prefix = timestamp != null ? timestamp : "";
			int prefixWidth = prefix.isEmpty() ? 0 : fm.stringWidth(prefix) + spaceWidth;

			String count = entry.getCount() > 1 ? "x" + entry.getCount() : "";
			int countWidth = count.isEmpty() ? 0 : spaceWidth + countFm.stringWidth(count);

			List<String> lines;
			if (config.wrapText())
			{
				lines = new ArrayList<>(TextWrapper.wrap(entry.getMessage(), fm, textWidth - prefixWidth, textWidth));
				// Push the count onto its own line if it doesn't fit after the last line of text
				int last = lines.size() - 1;
				int lineStart = last == 0 ? prefixWidth : 0;
				if (countWidth > 0 && lineStart + fm.stringWidth(lines.get(last)) + countWidth > textWidth)
				{
					lines.add("");
				}
			}
			else
			{
				lines = Collections.singletonList(
					TextWrapper.truncate(entry.getMessage(), fm, textWidth - prefixWidth - countWidth));
			}

			int last = lines.size() - 1;
			String lastLine = lines.get(last);
			int countX = (last == 0 ? prefixWidth : 0)
				+ (lastLine.isEmpty() ? 0 : fm.stringWidth(lastLine) + spaceWidth);

			blocks.add(new Block(prefix, prefixWidth, lines, count, countX, alpha(entry, now, durationMs)));
		}
		return blocks;
	}

	private float alpha(FeedEntry entry, long now, long durationMs)
	{
		if (!config.fadeOut() || durationMs <= 0)
		{
			return 1f;
		}
		long remaining = durationMs - (now - entry.getTimestamp());
		return Math.max(0f, Math.min(1f, remaining / (float) Math.min(FADE_MS, durationMs)));
	}

	private void drawBlock(Graphics2D g, FontMetrics fm, Block block, int x, int top)
	{
		int baseline = top + fm.getAscent();
		if (!block.prefix.isEmpty())
		{
			drawText(g, block.prefix, x, baseline, config.timestampColor());
		}

		Color textColor = config.textColor();
		for (int i = 0; i < block.lines.size(); i++)
		{
			int lineX = i == 0 ? x + block.prefixWidth : x;
			drawText(g, block.lines.get(i), lineX, baseline + i * fm.getHeight(), textColor);
		}

		if (!block.count.isEmpty())
		{
			Font font = g.getFont();
			g.setFont(COUNT_FONT);
			int countBaseline = baseline + (block.lines.size() - 1) * fm.getHeight();
			drawText(g, block.count, x + block.countX, countBaseline, config.timestampColor());
			g.setFont(font);
		}
	}

	private void drawBackground(Graphics2D g, int y, int width, int height)
	{
		background.setBackgroundColor(config.backgroundColor());
		background.setRectangle(new Rectangle(0, y, width, height));
		background.render(g);
	}

	private static void drawText(Graphics2D g, String text, int x, int y, Color color)
	{
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + 1);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	private static void withAlpha(Graphics2D g, float alpha, Runnable draw)
	{
		if (alpha >= 1f)
		{
			draw.run();
			return;
		}

		Composite original = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		try
		{
			draw.run();
		}
		finally
		{
			g.setComposite(original);
		}
	}

	@Value
	private static class Block
	{
		String prefix;
		int prefixWidth;
		List<String> lines;
		/**
		 * Duplicate count such as "x3", drawn in a smaller font after the last line; empty if none.
		 */
		String count;
		/**
		 * X offset of the count from the start of the last line's row.
		 */
		int countX;
		float alpha;
	}
}
