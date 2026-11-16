package com.notificationfeed;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * A user-defined timestamp pattern using {@link DateTimeFormatter} syntax (e.g. {@code HH:mm:ss}), with
 * {@value #RELATIVE_TOKEN} standing in for the relative time (e.g. "2m ago").
 */
final class CustomTimestampFormat
{
	static final String RELATIVE_TOKEN = "{relative}";
	static final String INVALID = "invalid format";

	@Getter
	private final String pattern;

	/**
	 * Date/time formatters for the text between relative tokens; one more entry than there are tokens.
	 * A null entry is an empty segment. The list itself is null if the pattern is invalid.
	 */
	private final List<DateTimeFormatter> segments;

	private CustomTimestampFormat(String pattern, List<DateTimeFormatter> segments)
	{
		this.pattern = pattern;
		this.segments = segments;
	}

	static CustomTimestampFormat compile(String pattern)
	{
		if (pattern == null || pattern.trim().isEmpty())
		{
			return new CustomTimestampFormat(pattern, null);
		}

		List<DateTimeFormatter> segments = new ArrayList<>();
		int start = 0;
		while (true)
		{
			int token = pattern.indexOf(RELATIVE_TOKEN, start);
			String segment = pattern.substring(start, token < 0 ? pattern.length() : token);
			try
			{
				segments.add(segment.isEmpty() ? null : DateTimeFormatter.ofPattern(segment));
			}
			catch (IllegalArgumentException e)
			{
				return new CustomTimestampFormat(pattern, null);
			}
			if (token < 0)
			{
				return new CustomTimestampFormat(pattern, segments);
			}
			start = token + RELATIVE_TOKEN.length();
		}
	}

	/**
	 * Formats an epoch-millis timestamp. Returns null for a blank pattern (no timestamp shown) and
	 * {@link #INVALID} if the pattern can't be used.
	 */
	String format(long timestamp, long now)
	{
		if (pattern == null || pattern.trim().isEmpty())
		{
			return null;
		}
		if (segments == null)
		{
			return INVALID;
		}

		ZonedDateTime time = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
		StringBuilder sb = new StringBuilder();
		try
		{
			for (int i = 0; i < segments.size(); i++)
			{
				if (i > 0)
				{
					sb.append(TimestampStyle.formatRelative(now - timestamp));
				}
				DateTimeFormatter segment = segments.get(i);
				if (segment != null)
				{
					segment.formatTo(time, sb);
				}
			}
		}
		catch (DateTimeException e)
		{
			return INVALID;
		}
		return sb.toString();
	}
}
