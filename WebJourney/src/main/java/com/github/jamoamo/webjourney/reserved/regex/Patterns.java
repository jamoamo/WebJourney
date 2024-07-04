/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.jamoamo.webjourney.reserved.regex;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author James Amoore
 */
public final class Patterns
{
	private static final HashMap<String, Pattern> PATTERN_MAP = new HashMap<>();

	private Patterns(){}
	
	/**
	 * Get a cached pattern.
	 * @param patternString The pattern string to get a Pattern for.
	 * @return a cached pattern.
	 * @throws com.github.jamoamo.webjourney.reserved.regex.XRegexException if invalid regex pattern syntax
	 */
	public static Pattern getPattern(String patternString) throws XRegexException
	{
		try
		{
			if(!PATTERN_MAP.containsKey(patternString))
			{
				PATTERN_MAP.put(patternString, Pattern.compile(patternString));
			}
			return PATTERN_MAP.get(patternString);
		}
		catch(PatternSyntaxException ex)
		{
			throw new XRegexException(ex);
		}
	}
}
