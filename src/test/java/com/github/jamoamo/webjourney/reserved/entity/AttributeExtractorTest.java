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
package com.github.jamoamo.webjourney.reserved.entity;

import com.github.jamoamo.webjourney.api.web.AElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class AttributeExtractorTest
{
	
	public AttributeExtractorTest()
	{
	}

	/**
	 * Test of extractRawValue method, of class AttributeExtractor.
	 */
	@Test
	public void testExtractRawValue()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getAttribute("attr")).thenReturn("Value");
		
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getElement("//div")).thenReturn(element);
		
		AttributeExtractor extractor = new AttributeExtractor("//div", "attr");
		String extractRawValue = extractor.extractRawValue(reader);
		assertEquals("Value", extractRawValue);
	}

	/**
	 * Test of getCondition method, of class AttributeExtractor.
	 */
	@Test
	public void testGetCondition()
	{
		AttributeExtractor extractor = new AttributeExtractor("//div", "attr");
		assertTrue(extractor.getCondition().evaluate(null));
	}
	
}
