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
package io.github.jamoamo.webjourney.reserved.regex;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class RegexGroupTest
{
	
	public RegexGroupTest()
	{
	}

	/**
	 * Test of findGroupValue method, of class RegexGroup.
	 */
	@Test
	public void testFindGroupValue_singlePattern_match() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("17");
		assertEquals("17", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_singlePattern_nomatch() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("g17");
		assertEquals("n/a", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_singlePattern_nomatchinggroup() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"a(?<group>\\d+)?b"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("ab");
		assertEquals("n/a", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_singlePattern_nonexistentgroup() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"a(?<group>\\d+)?b"}, "group2", "n/a");
		String findGroupValue = group.findGroupValue("ab");
		assertEquals("n/a", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_multiplePattern_firstMatch() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"c(?<group>\\d+)","a(?<group>\\d+)","t(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("c17");
		assertEquals("17", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_multiplePattern_lastMatch() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)","a(?<group>\\d+)","t(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("t17");
		assertEquals("17", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_multiplePattern_middleMatch() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)","a(?<group>\\d+)","t(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("a17");
		assertEquals("17", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_multiplePattern_noMatch() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)","a(?<group>\\d+)","t(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue("g17");
		assertEquals("n/a", findGroupValue);
	}
	
	@Test
	public void testFindGroupValue_pattern_null() throws Exception
	{
		RegexGroup group = new RegexGroup(new String[]{"(?<group>\\d+)","a(?<group>\\d+)"}, "group", "n/a");
		String findGroupValue = group.findGroupValue(null);
		assertNull(findGroupValue);
	}
}
