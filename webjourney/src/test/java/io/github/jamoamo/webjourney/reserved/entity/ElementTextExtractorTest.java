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

import io.github.jamoamo.webjourney.reserved.entity.ElementTextExtractor;
import io.github.jamoamo.webjourney.reserved.entity.IValueReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ElementTextExtractorTest
{
	
	public ElementTextExtractorTest()
	{
	}

	/**
	 * Test of extractRawValue method, of class ElementTextExtractor.
	 */
	@Test
	public void testExtractRawValue() throws Exception
	{
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getElementText("//div", false)).thenReturn("Text");
		
		ElementTextExtractor extractor = new ElementTextExtractor("//div");
		String extractRawValue = extractor.extractRawValue(reader);
		assertEquals("Text", extractRawValue);
	}
}
