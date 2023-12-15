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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds a value from a regex group in multiple regexes.
 * @author James Amoore
 */
public class RegexGroup
{
	private final List<Pattern> patternList;
	private final String group;
	private final String defaultValue;
	/**
	 * Creates a new instance.
	 * @param patterns The patterns to evaluate
	 * @param group The group name
	 * @param defaultValue The default value
	 */
	public RegexGroup(String[] patterns, String group, String defaultValue)
	{
		this.patternList = Arrays.stream(patterns).map(regex -> Patterns.getPattern(regex)).toList();
		this.group = group;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Find the group value.
	 * @param value The value to evaluate
	 * @return the group value
	 */
	public String findGroupValue(String value)
	{
		for(Pattern pattern : this.patternList)
		{
			Matcher matcher = pattern.matcher(value);
			if(matcher.matches())
			{
				try
				{
					String groupValue = matcher.group(this.group);
					if(groupValue != null)
					{
						return groupValue;
					}
				}
				catch(IllegalArgumentException iae)
				{
					//ignore if group doesn't exist
				}
			}
		}
		return this.defaultValue;
	}
}