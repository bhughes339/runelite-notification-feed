package com.notificationfeed;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MessageCleanTest
{
	@Test
	public void stripsTagsAndCollapsesWhitespace()
	{
		assertEquals("You are now idle!", NotificationFeedPlugin.clean("<col=ff0000>You are</col>  now\nidle!"));
	}

	@Test
	public void nullIsEmpty()
	{
		assertEquals("", NotificationFeedPlugin.clean(null));
	}
}
