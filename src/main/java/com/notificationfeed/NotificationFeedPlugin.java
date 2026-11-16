package com.notificationfeed;

import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

@Slf4j
@PluginDescriptor(
	name = "Notification Feed",
	description = "Shows a live, movable feed of RuneLite notifications inside the game window",
	tags = {"notification", "notifications", "feed", "log", "history", "overlay", "toast", "alert"}
)
public class NotificationFeedPlugin extends Plugin
{
	private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();

	@Inject
	private NotificationFeedConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NotificationFeedOverlay overlay;

	@Inject
	private NotificationFeed feed;

	private volatile List<String> ignoredPatterns = Collections.emptyList();

	@Provides
	NotificationFeedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NotificationFeedConfig.class);
	}

	@Override
	protected void startUp()
	{
		loadIgnoredPatterns();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		feed.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (NotificationFeedConfig.GROUP.equals(event.getGroup()) && "ignoredNotifications".equals(event.getKey()))
		{
			loadIgnoredPatterns();
		}
	}

	// Fired by RuneLite's Notifier for every enabled notification, whether or not the client is
	// focused, and by plugins such as Watchdog that post the event directly. May be on any thread.
	@Subscribe
	public void onNotificationFired(NotificationFired event)
	{
		String message = clean(event.getMessage());
		if (message.isEmpty() || isIgnored(message))
		{
			return;
		}

		log.debug("Notification: {}", message);
		feed.add(message, System.currentTimeMillis(), config.collapseDuplicates(),
			config.collapseWindow() * 60_000L, config.maxEntries());
	}

	static String clean(String message)
	{
		if (message == null)
		{
			return "";
		}
		return Text.removeTags(message).replaceAll("\\s+", " ").trim();
	}

	private boolean isIgnored(String message)
	{
		for (String pattern : ignoredPatterns)
		{
			if (WildcardMatcher.matches(pattern, message))
			{
				return true;
			}
		}
		return false;
	}

	private void loadIgnoredPatterns()
	{
		ignoredPatterns = LINE_SPLITTER.splitToList(config.ignoredNotifications());
	}
}
