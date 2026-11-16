package com.notificationfeed;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NotificationFeedTest
{
	private final NotificationFeed feed = new NotificationFeed();

	@Test
	public void keepsEntriesOldestFirst()
	{
		feed.add("a", 0, true, 0, 5);
		feed.add("b", 1, true, 0, 5);

		List<FeedEntry> entries = feed.snapshot(2, 0, 5);
		assertEquals(2, entries.size());
		assertEquals("a", entries.get(0).getMessage());
		assertEquals("b", entries.get(1).getMessage());
	}

	@Test
	public void collapsesConsecutiveDuplicates()
	{
		feed.add("a", 0, true, 0, 5);
		feed.add("a", 10, true, 0, 5);
		feed.add("b", 20, true, 0, 5);
		feed.add("a", 30, true, 0, 5);

		List<FeedEntry> entries = feed.snapshot(30, 0, 5);
		assertEquals(3, entries.size());
		assertEquals(new FeedEntry("a", 10, 2), entries.get(0));
		assertEquals(new FeedEntry("a", 30, 1), entries.get(2));
	}

	@Test
	public void duplicateWithinWindowCollapses()
	{
		feed.add("a", 0, true, 1000, 5);
		feed.add("a", 999, true, 1000, 5);

		List<FeedEntry> entries = feed.snapshot(999, 0, 5);
		assertEquals(1, entries.size());
		assertEquals(2, entries.get(0).getCount());
	}

	@Test
	public void duplicateAfterWindowStartsNewEntry()
	{
		feed.add("a", 0, true, 1000, 5);
		feed.add("a", 1000, true, 1000, 5);

		List<FeedEntry> entries = feed.snapshot(1000, 0, 5);
		assertEquals(2, entries.size());
		assertEquals(new FeedEntry("a", 1000, 1), entries.get(1));
	}

	@Test
	public void windowIsMeasuredFromLatestOccurrence()
	{
		// Each repeat is within the window of the previous one, so they keep collapsing
		// even though the last is well past the window from the first
		feed.add("a", 0, true, 1000, 5);
		feed.add("a", 800, true, 1000, 5);
		feed.add("a", 1600, true, 1000, 5);

		List<FeedEntry> entries = feed.snapshot(1600, 0, 5);
		assertEquals(1, entries.size());
		assertEquals(3, entries.get(0).getCount());
	}

	@Test
	public void keepsDuplicatesWhenNotCollapsing()
	{
		feed.add("a", 0, false, 0, 5);
		feed.add("a", 10, false, 0, 5);

		assertEquals(2, feed.snapshot(10, 0, 5).size());
	}

	@Test
	public void dropsOldestBeyondMax()
	{
		for (int i = 0; i < 5; i++)
		{
			feed.add("m" + i, i, true, 0, 3);
		}

		List<FeedEntry> entries = feed.snapshot(5, 0, 3);
		assertEquals(3, entries.size());
		assertEquals("m2", entries.get(0).getMessage());

		// Lowering the max later trims on the next snapshot
		assertEquals("m4", feed.snapshot(5, 0, 1).get(0).getMessage());
	}

	@Test
	public void expiresEntriesAfterDuration()
	{
		feed.add("old", 0, true, 0, 5);
		feed.add("new", 900, true, 0, 5);

		List<FeedEntry> entries = feed.snapshot(1000, 1000, 5);
		assertEquals(1, entries.size());
		assertEquals("new", entries.get(0).getMessage());
	}

	@Test
	public void collapsedDuplicateRestartsExpiry()
	{
		feed.add("a", 0, true, 0, 5);
		feed.add("a", 900, true, 0, 5);

		assertEquals(1, feed.snapshot(1500, 1000, 5).size());
	}

	@Test
	public void zeroDurationNeverExpires()
	{
		feed.add("a", 0, true, 0, 5);
		assertEquals(1, feed.snapshot(Long.MAX_VALUE, 0, 5).size());
	}

	@Test
	public void clearRemovesEverything()
	{
		feed.add("a", 0, true, 0, 5);
		feed.clear();
		assertTrue(feed.snapshot(0, 0, 5).isEmpty());
	}
}
