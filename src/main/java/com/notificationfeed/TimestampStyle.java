package com.notificationfeed;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TimestampStyle
{
	NONE("None", null),
	TIME_24H("24-hour (13:05)", DateTimeFormatter.ofPattern("HH:mm")),
	TIME_24H_SECONDS("24-hour (13:05:09)", DateTimeFormatter.ofPattern("HH:mm:ss")),
	TIME_12H("12-hour (1:05 PM)", DateTimeFormatter.ofPattern("h:mm a")),
	TIME_12H_SECONDS("12-hour (1:05:09 PM)", DateTimeFormatter.ofPattern("h:mm:ss a")),
	RELATIVE("Relative (2m ago)", null);

	private final String name;
	private final DateTimeFormatter formatter;

	/**
	 * Formats an epoch-millis timestamp, or returns null for {@link #NONE}.
	 */
	String format(long timestamp, long now)
	{
		switch (this)
		{
			case NONE:
				return null;
			case RELATIVE:
				return formatRelative(now - timestamp);
			default:
				return formatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
		}
	}

	static String formatRelative(long elapsedMs)
	{
		long seconds = Math.max(elapsedMs, 0) / 1000;
		if (seconds < 5)
		{
			return "now";
		}
		if (seconds < 60)
		{
			return seconds + "s ago";
		}
		long minutes = seconds / 60;
		if (minutes < 60)
		{
			return minutes + "m ago";
		}
		long hours = minutes / 60;
		if (hours < 24)
		{
			return hours + "h ago";
		}
		return (hours / 24) + "d ago";
	}

	@Override
	public String toString()
	{
		return name;
	}
}
