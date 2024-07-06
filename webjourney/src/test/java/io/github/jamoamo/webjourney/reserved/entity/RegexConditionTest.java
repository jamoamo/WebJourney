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
package io.github.jamoamo.webjourney.reserved.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class RegexConditionTest
{
	
	public RegexConditionTest()
	{
	}

	/**
	 * Test of evaluate method, of class RegexCondition.
	 */
	@Test
	public void testEvaluate_match() throws Exception
	{
		IExtractor<String> extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(any())).thenReturn("String value");
		
		RegexCondition condition = new RegexCondition(extractor, "value");
		
		boolean evaluate = condition.evaluate(null);
		assertTrue(evaluate);
	}
	
	@Test
	public void testEvaluate_nomatch() throws Exception
	{
		IExtractor<String> extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(any())).thenReturn("String value");
		
		RegexCondition condition = new RegexCondition(extractor, "values");
		
		boolean evaluate = condition.evaluate(null);
		assertFalse(evaluate);
	}
	
	@Test
	public void testEvaluate_null() throws Exception
	{
		IExtractor<String> extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(any())).thenReturn(null);
		
		RegexCondition condition = new RegexCondition(extractor, "values");
		
		boolean evaluate = condition.evaluate(null);
		assertFalse(evaluate);
	}
	
}
