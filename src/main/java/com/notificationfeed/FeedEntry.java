package com.notificationfeed;

import lombok.Value;
import lombok.With;

/**
 * An immutable entry in the notification feed. Collapsed duplicates produce a new
 * entry with a bumped count and timestamp rather than mutating this one, so the
 * overlay can safely render a snapshot while notifications arrive on other threads.
 */
@Value
@With
public class FeedEntry
{
	String message;
	/**
	 * Epoch millis of the most recent occurrence of this message.
	 */
	long timestamp;
	int count;
}
