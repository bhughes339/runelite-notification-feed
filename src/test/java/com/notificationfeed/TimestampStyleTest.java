package com.notificationfeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class TimestampStyleTest
{
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
		assertNull(TimestampStyle.NONE.format(0, 0));
	}
}
