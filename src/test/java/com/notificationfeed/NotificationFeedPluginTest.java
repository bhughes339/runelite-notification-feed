package com.notificationfeed;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NotificationFeedPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NotificationFeedPlugin.class);
		RuneLite.main(args);
	}
}
