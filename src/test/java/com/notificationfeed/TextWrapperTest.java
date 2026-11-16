package com.notificationfeed;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TextWrapperTest
{
	// Monospaced so widths are predictable: every char is the same width
	private static final FontMetrics FM = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
		.createGraphics()
		.getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, 12));
	private static final int CHAR = FM.charWidth('a');

	@Test
	public void shortTextIsOneLine()
	{
		assertEquals(Collections.singletonList("hello world"), TextWrapper.wrap("hello world", FM, 20 * CHAR, 20 * CHAR));
	}

	@Test
	public void wrapsAtWordBoundaries()
	{
		assertEquals(Arrays.asList("aaa bbb", "ccc"), TextWrapper.wrap("aaa bbb ccc", FM, 8 * CHAR, 8 * CHAR));
	}

	@Test
	public void breaksWordsLongerThanLine()
	{
		assertEquals(Arrays.asList("abcd", "efgh", "ij"), TextWrapper.wrap("abcdefghij", FM, 4 * CHAR, 4 * CHAR));
	}

	@Test
	public void narrowFirstLine()
	{
		assertEquals(Arrays.asList("aa", "bbb ccc"), TextWrapper.wrap("aa bbb ccc", FM, 3 * CHAR, 8 * CHAR));
	}

	@Test
	public void firstLineLeftEmptyWhenFirstWordOnlyFitsFullWidth()
	{
		assertEquals(Arrays.asList("", "abcdef"), TextWrapper.wrap("abcdef", FM, 3 * CHAR, 8 * CHAR));
	}

	@Test
	public void collapsesRepeatedSpaces()
	{
		assertEquals(Collections.singletonList("a b"), TextWrapper.wrap("a   b", FM, 20 * CHAR, 20 * CHAR));
	}

	@Test
	public void emptyTextIsOneEmptyLine()
	{
		assertEquals(Collections.singletonList(""), TextWrapper.wrap("", FM, 20 * CHAR, 20 * CHAR));
	}

	@Test
	public void truncateLeavesShortTextAlone()
	{
		assertEquals("hello", TextWrapper.truncate("hello", FM, 10 * CHAR));
	}

	@Test
	public void truncateAddsEllipsis()
	{
		String result = TextWrapper.truncate("hello world", FM, 8 * CHAR);
		assertEquals("hello...", result);
		assertTrue(FM.stringWidth(result) <= 8 * CHAR);
	}

	@Test
	public void wrappedLinesFitWidth()
	{
		List<String> lines = TextWrapper.wrap("Your character is now idle and should probably do something", FM, 12 * CHAR, 15 * CHAR);
		assertTrue(FM.stringWidth(lines.get(0)) <= 12 * CHAR);
		for (String line : lines.subList(1, lines.size()))
		{
			assertTrue(line, FM.stringWidth(line) <= 15 * CHAR);
		}
	}
}
