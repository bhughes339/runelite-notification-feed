package com.notificationfeed;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

final class TextWrapper
{
	private static final String ELLIPSIS = "...";

	private TextWrapper()
	{
	}

	/**
	 * Word-wraps text to fit within maxWidth pixels. Words wider than maxWidth are
	 * broken across lines. Always returns at least one line.
	 *
	 * @param firstLineWidth width available on the first line, which may be narrower than
	 *                       maxWidth to leave room for a prefix such as a timestamp. If the
	 *                       first word doesn't fit there but would fit on a full line, the
	 *                       first line is left empty.
	 */
	static List<String> wrap(String text, FontMetrics fm, int firstLineWidth, int maxWidth)
	{
		List<String> lines = new ArrayList<>();
		String line = "";

		for (String word : text.split(" "))
		{
			if (word.isEmpty())
			{
				continue;
			}

			String candidate = line.isEmpty() ? word : line + " " + word;
			if (fm.stringWidth(candidate) <= limit(lines, firstLineWidth, maxWidth))
			{
				line = candidate;
				continue;
			}

			if (!line.isEmpty())
			{
				lines.add(line);
				line = "";
			}

			if (fm.stringWidth(word) <= maxWidth)
			{
				if (fm.stringWidth(word) > limit(lines, firstLineWidth, maxWidth))
				{
					// Only possible on a narrowed first line
					lines.add("");
				}
				line = word;
				continue;
			}

			// Break words that are too long to fit on a line of their own
			while (word.length() > 1 && fm.stringWidth(word) > limit(lines, firstLineWidth, maxWidth))
			{
				int fit = fitChars(word, fm, limit(lines, firstLineWidth, maxWidth));
				lines.add(word.substring(0, fit));
				word = word.substring(fit);
			}
			line = word;
		}

		if (!line.isEmpty() || lines.isEmpty())
		{
			lines.add(line);
		}
		return lines;
	}

	private static int limit(List<String> lines, int firstLineWidth, int maxWidth)
	{
		return lines.isEmpty() ? firstLineWidth : maxWidth;
	}

	/**
	 * Truncates text with an ellipsis so that it fits within maxWidth pixels.
	 */
	static String truncate(String text, FontMetrics fm, int maxWidth)
	{
		if (fm.stringWidth(text) <= maxWidth)
		{
			return text;
		}

		int available = maxWidth - fm.stringWidth(ELLIPSIS);
		int end = text.length();
		while (end > 0 && fm.stringWidth(text.substring(0, end)) > available)
		{
			end--;
		}
		return text.substring(0, end).trim() + ELLIPSIS;
	}

	/**
	 * Number of leading chars of word that fit within maxWidth, minimum 1.
	 */
	private static int fitChars(String word, FontMetrics fm, int maxWidth)
	{
		int n = 1;
		while (n < word.length() && fm.stringWidth(word.substring(0, n + 1)) <= maxWidth)
		{
			n++;
		}
		return n;
	}
}
