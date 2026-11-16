package com.notificationfeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Test;

public class TimestampStyleTest
{
	private static final long TIME = LocalDateTime.of(2026, 11, 16, 13, 5, 9)
		.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	@Test
	public void relative()
	{
		assertEquals("now", TimestampStyle.formatRelative(-50));
		assertEquals("now", TimestampStyle.formatRelative(4_999));
		assertEquals("5s ago", TimestampStyle.formatRelative(5_000));
		assertEquals("1m ago", TimestampStyle.formatRelative(60_000));
		assertEquals("2h ago", TimestampStyle.formatRelative(2 * 3_600_000));
		assertEquals("3d ago", TimestampStyle.formatRelative(3 * 86_400_000L));
	}

	@Test
	public void noneFormatsToNull()
	{
		assertNull(TimestampStyle.NONE.format(0, 0, CustomTimestampFormat.compile("HH:mm")));
	}

	@Test
	public void customPattern()
	{
		assertEquals("13:05:09", custom("HH:mm:ss", TIME));
		assertEquals("1:05 PM 16/11", custom("h:mm a dd/MM", TIME));
		assertEquals("13:05 at Mon", custom("HH:mm 'at' EEE", TIME));
	}

	@Test
	public void customRelative()
	{
		long now = TIME + 120_000;
		assertEquals("2m ago", custom("{relative}", now));
		assertEquals("13:05 (2m ago)", custom("HH:mm ({relative})", now));
		assertEquals("2m ago, 13:05, 2m ago", custom("{relative}, HH:mm, {relative}", now));
	}

	@Test
	public void customBlankShowsNothing()
	{
		assertNull(custom("", TIME));
		assertNull(custom("  ", TIME));
		assertNull(custom(null, TIME));
	}

	@Test
	public void customInvalid()
	{
		assertEquals(CustomTimestampFormat.INVALID, custom("HH:mm 'unterminated", TIME));
		assertEquals(CustomTimestampFormat.INVALID, custom("ZZZZZZ", TIME));
	}

	private static String custom(String pattern, long now)
	{
		return TimestampStyle.CUSTOM.format(TIME, now, CustomTimestampFormat.compile(pattern));
	}
}
