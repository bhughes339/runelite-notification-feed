package com.notificationfeed;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FeedStyle
{
	CARDS("Separate boxes"),
	PANEL("Single panel");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
