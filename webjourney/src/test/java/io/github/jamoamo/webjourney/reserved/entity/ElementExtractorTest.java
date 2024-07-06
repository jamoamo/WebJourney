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

import io.github.jamoamo.webjourney.reserved.entity.IValueReader;
import io.github.jamoamo.webjourney.reserved.entity.ElementExtractor;
import io.github.jamoamo.webjourney.api.web.AElement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ElementExtractorTest
{
	@Test
	public void testextractRawValue() throws Exception
	{
		AElement element = Mockito.mock(AElement.class);
		
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getElement("//div", false)).thenReturn(element);
		
		ElementExtractor extractor = new ElementExtractor("//div", false);
		AElement extractRawValue = extractor.extractRawValue(reader);
		assertSame(element, extractRawValue);
	}
	
}
