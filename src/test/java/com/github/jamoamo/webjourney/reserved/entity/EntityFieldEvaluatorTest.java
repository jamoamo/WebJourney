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

import java.util.ArrayList;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class EntityFieldEvaluatorTest
{
	public static class BooleanCondition implements ICondition
	{
		private final boolean value;
		public BooleanCondition(boolean value)
		{
			this.value = value;
		}

		@Override
		public boolean evaluate(IValueReader reader)
		{
			return value;
		}
	}

	/**
	 * Test of evaluate method, of class EntityFieldEvaluator.
	 */
	@Test
	public void testEvaluate() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);
		
		IExtractor extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(browser)).thenReturn("Value");
		Mockito.when(extractor.getCondition()).thenReturn(new BooleanCondition(true));
		
		ITransformer transformer = Mockito.mock(ITransformer.class);
		Mockito.when(transformer.transformValue(Mockito.any())).thenReturn("TheValue");
		
		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any())).thenReturn("SomeValue");
		
		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(Collections.singletonList(extractor), transformer, converter);
		Object evaluate = evaluator.evaluate(browser);
		
		assertEquals("SomeValue", evaluate);
	}
	
	@Test
	public void testEvaluate_nullTransformer() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);
		
		IExtractor extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(browser)).thenReturn("Value");
		Mockito.when(extractor.getCondition()).thenReturn(new BooleanCondition(true));
		
		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any())).thenReturn("SomeValue");
		
		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(Collections.singletonList(extractor), null, converter);
		Object evaluate = evaluator.evaluate(browser);
		
		assertEquals("SomeValue", evaluate);
	}
	
	@Test
	public void testEvaluate_multipleExtractors_oneConditionMatch() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);
		
		IExtractor extractor1 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor1.extractRawValue(browser)).thenReturn("Value");
		Mockito.when(extractor1.getCondition()).thenReturn(new BooleanCondition(true));
		
		IExtractor extractor2 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor2.extractRawValue(browser)).thenReturn("Value2");
		Mockito.when(extractor2.getCondition()).thenReturn(new BooleanCondition(false));
		
		ArrayList<IExtractor> list = new ArrayList<>();
		list.add(extractor1);
		list.add(extractor2);
		
		ITransformer transformer = Mockito.mock(ITransformer.class);
		Mockito.when(transformer.transformValue("Value")).thenReturn("TheValue");
		
		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any())).thenReturn("SomeValue");
		
		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(list, transformer, converter);
		Object evaluate = evaluator.evaluate(browser);
		
		assertEquals("SomeValue", evaluate);
	}
}
