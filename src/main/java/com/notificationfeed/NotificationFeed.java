package com.notificationfeed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import javax.inject.Singleton;

/**
 * Thread-safe store of recent notifications, oldest first. Notifications may be fired
 * from any thread, while the overlay reads from the client thread.
 */
@Singleton
public class NotificationFeed
{
	private final Deque<FeedEntry> entries = new ArrayDeque<>();

	/**
	 * Adds a notification to the feed.
	 *
	 * @param collapseDuplicates if true and the newest entry has the same message, that
	 *                           entry's count and timestamp are bumped instead of adding a new one
	 * @param collapseWindowMs   a duplicate arriving this long or longer after the newest entry's
	 *                           last occurrence starts a new entry instead; 0 or less means no limit
	 * @param maxEntries         oldest entries beyond this count are dropped
	 */
	public synchronized void add(String message, long now, boolean collapseDuplicates, long collapseWindowMs, int maxEntries)
	{
		FeedEntry last = entries.peekLast();
		if (collapseDuplicates && last != null && last.getMessage().equals(message)
			&& (collapseWindowMs <= 0 || now - last.getTimestamp() < collapseWindowMs))
		{
			entries.pollLast();
			entries.addLast(last.withCount(last.getCount() + 1).withTimestamp(now));
		}
		else
		{
			entries.addLast(new FeedEntry(message, now, 1));
		}

		trim(maxEntries);
	}

	/**
	 * Removes expired entries and returns the remaining ones, oldest first.
	 *
	 * @param durationMs how long entries stay in the feed; 0 or less means forever
	 */
	public synchronized List<FeedEntry> snapshot(long now, long durationMs, int maxEntries)
	{
		if (durationMs > 0)
		{
			for (Iterator<FeedEntry> it = entries.iterator(); it.hasNext(); )
			{
				if (now - it.next().getTimestamp() >= durationMs)
				{
					it.remove();
				}
			}
		}

		trim(maxEntries);
		return new ArrayList<>(entries);
	}

	public synchronized void clear()
	{
		entries.clear();
	}

	private void trim(int maxEntries)
	{
		while (entries.size() > Math.max(maxEntries, 0))
		{
			entries.pollFirst();
		}
	}
}
