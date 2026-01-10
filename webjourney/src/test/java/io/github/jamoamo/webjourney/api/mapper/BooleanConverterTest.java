/*
 * The MIT License
 *
 * Copyright 2025 James Amoore.
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
package io.github.jamoamo.webjourney.api.mapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the BooleanConverter class.
 *
 * @author James Amoore
 */
public class BooleanConverterTest
{
	
	@Test
	public void testMapValue_trueValues_returnsTrue()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test all the defined true values
		assertTrue(instance.mapValue("1"));
		assertTrue(instance.mapValue("true"));
		assertTrue(instance.mapValue("yes"));
	}
	
	@Test
	public void testMapValue_trueValuesCaseInsensitive_returnsTrue()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test case variations of true values
		assertTrue(instance.mapValue("TRUE"));
		assertTrue(instance.mapValue("True"));
		assertTrue(instance.mapValue("tRuE"));
		assertTrue(instance.mapValue("YES"));
		assertTrue(instance.mapValue("Yes"));
		assertTrue(instance.mapValue("yEs"));
	}
	
	@Test
	public void testMapValue_falseValues_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test various false values
		assertFalse(instance.mapValue("0"));
		assertFalse(instance.mapValue("false"));
		assertFalse(instance.mapValue("no"));
		assertFalse(instance.mapValue("maybe"));
		assertFalse(instance.mapValue("random"));
		assertFalse(instance.mapValue(""));
		assertFalse(instance.mapValue(" "));
	}
	
	@Test
	public void testMapValue_null_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		Boolean result = instance.mapValue(null);
		assertFalse(result);
	}
	
	@Test
	public void testMapValue_emptyString_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		Boolean result = instance.mapValue("");
		assertFalse(result);
	}
	
	@Test
	public void testMapValue_whitespaceOnly_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		Boolean result = instance.mapValue("   ");
		assertFalse(result);
	}
	
	@Test
	public void testMapValue_numbersOtherThanOne_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test various numbers that should be false
		assertFalse(instance.mapValue("0"));
		assertFalse(instance.mapValue("2"));
		assertFalse(instance.mapValue("10"));
		assertFalse(instance.mapValue("-1"));
		assertFalse(instance.mapValue("1.5"));
	}
	
	@Test
	public void testMapValue_specialCharacters_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test special characters and symbols
		assertFalse(instance.mapValue("!"));
		assertFalse(instance.mapValue("@"));
		assertFalse(instance.mapValue("#"));
		assertFalse(instance.mapValue("$"));
		assertFalse(instance.mapValue("%"));
		assertFalse(instance.mapValue("^"));
		assertFalse(instance.mapValue("&"));
		assertFalse(instance.mapValue("*"));
	}
	
	@Test
	public void testMapValue_mixedCaseFalseValues_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test case variations of false values
		assertFalse(instance.mapValue("FALSE"));
		assertFalse(instance.mapValue("False"));
		assertFalse(instance.mapValue("fAlSe"));
		assertFalse(instance.mapValue("NO"));
		assertFalse(instance.mapValue("No"));
		assertFalse(instance.mapValue("nO"));
	}
	
	@Test
	public void testMapValue_partialMatches_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test partial matches that should not be true
		assertFalse(instance.mapValue("tru"));
		assertFalse(instance.mapValue("truer"));
		assertFalse(instance.mapValue("ye"));
		assertFalse(instance.mapValue("yeses"));
		assertFalse(instance.mapValue("11"));
		assertFalse(instance.mapValue("01"));
	}
	
	@Test
	public void testMapValue_unicodeAndSpecialStrings_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test unicode and special string cases
		assertFalse(instance.mapValue("t\u0072ue")); // 't' + unicode 'r' + 'ue'
		assertFalse(instance.mapValue("t\u0000rue")); // 't' + null char + 'rue'
		assertFalse(instance.mapValue("true\u0000")); // 'true' + null char
		assertFalse(instance.mapValue("\u0000true")); // null char + 'true'
	}
	
	@Test
	public void testMapValue_veryLongStrings_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test very long strings
		String longString = "true" + "x".repeat(1000);
		assertFalse(instance.mapValue(longString));
		
		String longFalseString = "false" + "x".repeat(1000);
		assertFalse(instance.mapValue(longFalseString));
	}
	
	@Test
	public void testMapValue_edgeCaseStrings_returnsFalse()
		 throws Exception
	{
		BooleanConverter instance = new BooleanConverter();
		
		// Test edge case strings
		assertFalse(instance.mapValue("true ")); // true with trailing space
		assertFalse(instance.mapValue(" true")); // true with leading space
		assertFalse(instance.mapValue(" true ")); // true with both spaces
		assertFalse(instance.mapValue("\ttrue")); // true with tab
		assertFalse(instance.mapValue("true\n")); // true with newline
		assertFalse(instance.mapValue("true\r")); // true with carriage return
	}
}
