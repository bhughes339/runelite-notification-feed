package com.notificationfeed;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.FontType;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.ui.overlay.components.ComponentConstants;

@ConfigGroup(NotificationFeedConfig.GROUP)
public interface NotificationFeedConfig extends Config
{
	String GROUP = "notificationfeed";

	@ConfigSection(
		name = "Feed",
		description = "Which notifications are shown and for how long",
		position = 0
	)
	String feedSection = "feed";

	@ConfigSection(
		name = "Appearance",
		description = "How the feed looks",
		position = 1
	)
	String appearanceSection = "appearance";

	// Feed

	@Range(min = 1, max = 50)
	@ConfigItem(
		keyName = "maxEntries",
		name = "Max notifications",
		description = "Maximum number of notifications shown at once. Older ones are removed first.",
		position = 0,
		section = feedSection
	)
	default int maxEntries()
	{
		return 5;
	}

	@Units(Units.SECONDS)
	@Range(min = 0, max = 3600)
	@ConfigItem(
		keyName = "duration",
		name = "Display duration",
		description = "How long each notification stays in the feed. 0 keeps notifications until they are pushed out or cleared.",
		position = 1,
		section = feedSection
	)
	default int duration()
	{
		return 15;
	}

	@ConfigItem(
		keyName = "fadeOut",
		name = "Fade out",
		description = "Fade notifications out during their last second instead of removing them abruptly",
		position = 2,
		section = feedSection
	)
	default boolean fadeOut()
	{
		return true;
	}

	@ConfigItem(
		keyName = "newestFirst",
		name = "Newest on top",
		description = "Show the newest notification at the top of the feed instead of the bottom",
		position = 3,
		section = feedSection
	)
	default boolean newestFirst()
	{
		return true;
	}

	@ConfigItem(
		keyName = "collapseDuplicates",
		name = "Collapse duplicates",
		description = "Merge a notification with the previous one if the text is identical, showing a count instead",
		position = 4,
		section = feedSection
	)
	default boolean collapseDuplicates()
	{
		return true;
	}

	@Units(Units.MINUTES)
	@Range(min = 0, max = 1440)
	@ConfigItem(
		keyName = "collapseWindow",
		name = "Duplicate time limit",
		description = "When collapsing duplicates, a repeat that arrives at least this many minutes after the"
			+ " previous one starts a new line instead. 0 always collapses.",
		position = 5,
		section = feedSection
	)
	default int collapseWindow()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "showWhenEmpty",
		name = "Show when empty",
		description = "Keep the feed visible with a placeholder when there are no notifications."
			+ " The placeholder is always shown while holding Alt so the feed can be moved.",
		position = 6,
		section = feedSection
	)
	default boolean showWhenEmpty()
	{
		return false;
	}

	@ConfigItem(
		keyName = "ignoredNotifications",
		name = "Ignored notifications",
		description = "Notifications matching any of these lines are not shown. One per line; supports * wildcards"
			+ " and is case-insensitive. e.g. *is now idle*",
		position = 7,
		section = feedSection
	)
	default String ignoredNotifications()
	{
		return "";
	}

	// Appearance

	@ConfigItem(
		keyName = "style",
		name = "Style",
		description = "Show each notification in its own box, or all of them in one panel",
		position = 0,
		section = appearanceSection
	)
	default FeedStyle style()
	{
		return FeedStyle.CARDS;
	}

	@ConfigItem(
		keyName = "timestampStyle",
		name = "Timestamp",
		description = "Show when each notification was received",
		position = 1,
		section = appearanceSection
	)
	default TimestampStyle timestampStyle()
	{
		return TimestampStyle.TIME_24H;
	}

	@ConfigItem(
		keyName = "wrapText",
		name = "Wrap long text",
		description = "Wrap long notifications onto multiple lines instead of cutting them off."
			+ " Resize the feed by Alt-dragging its edge.",
		position = 2,
		section = appearanceSection
	)
	default boolean wrapText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "font",
		name = "Font",
		description = "Font used for notification text",
		position = 3,
		section = appearanceSection
	)
	default FontType font()
	{
		return FontType.REGULAR;
	}

	@ConfigItem(
		keyName = "textColor",
		name = "Text color",
		description = "Color of notification text",
		position = 4,
		section = appearanceSection
	)
	default Color textColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "timestampColor",
		name = "Timestamp color",
		description = "Color of timestamps and duplicate counts",
		position = 5,
		section = appearanceSection
	)
	default Color timestampColor()
	{
		return new Color(0xFF, 0x98, 0x1F);
	}

	@Alpha
	@ConfigItem(
		keyName = "backgroundColor",
		name = "Background color",
		description = "Background color of the feed",
		position = 6,
		section = appearanceSection
	)
	default Color backgroundColor()
	{
		return ComponentConstants.STANDARD_BACKGROUND_COLOR;
	}
}
