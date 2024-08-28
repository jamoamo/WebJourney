/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class CollectionIndexExtractorTest
{
	@Test
	public void testExtractRawValue_zeroBased()
		 throws Exception
	{
		CollectionIndexExtractor instance = new CollectionIndexExtractor(0);
		EntityCreationContext context = Mockito.mock(EntityCreationContext.class);
		Mockito.when(context.getExistingIndex()).thenReturn(4);
		String result = instance.extractRawValue(null, context);
		assertEquals("4", result);
	}
	
	@Test
	public void testExtractRawValue_oneBased()
		 throws Exception
	{
		CollectionIndexExtractor instance = new CollectionIndexExtractor(1);
		EntityCreationContext context = Mockito.mock(EntityCreationContext.class);
		Mockito.when(context.getExistingIndex()).thenReturn(4);
		String result = instance.extractRawValue(null, context);
		assertEquals("5", result);
	}
	
	@Test
	public void testExtractRawValue_noIndex()
		 throws Exception
	{
		CollectionIndexExtractor instance = new CollectionIndexExtractor(0);
		EntityCreationContext context = Mockito.mock(EntityCreationContext.class);
		Mockito.when(context.getExistingIndex()).thenReturn(null);
		XExtractionException assertThrows =
			 assertThrows(XExtractionException.class, () -> instance.extractRawValue(null, context));
		assertEquals("There was an error during Extraction. Can't use index when not part of a collection.", 
			 assertThrows.getMessage());
	}

	@Test
	public void testGetCondition()
	{
		CollectionIndexExtractor instance = new CollectionIndexExtractor(0);
		ICondition result = instance.getCondition();
		assertInstanceOf(AlwaysCondition.class, result);
	}
	
}
