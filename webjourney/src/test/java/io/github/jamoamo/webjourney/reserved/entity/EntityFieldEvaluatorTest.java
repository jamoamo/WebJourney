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

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EntityFieldEvaluatorTest
{
	public static class BooleanCondition implements ICondition
	{
		private final boolean value;

		public BooleanCondition(
			boolean value)
		{
			this.value = value;
		}

		@Override
		public boolean evaluate(IValueReader reader, EntityCreationContext entityCreationContext)
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
		Mockito.when(extractor.extractRawValue(any(), any())).thenReturn("Value");
		Mockito.when(extractor.getCondition())
			.thenReturn(new BooleanCondition(
				true));

		ITransformer transformer = Mockito.mock(ITransformer.class);
		Mockito.when(transformer.transformValue(Mockito.any())).thenReturn("TheValue");

		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any(), any(), any())).thenReturn("SomeValue");

		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(
			Collections.singletonList(extractor),
			transformer,
			converter);
		Object evaluate = evaluator.evaluate(browser, new ArrayList<>(), null);

		assertEquals("SomeValue", evaluate);
	}

	@Test
	public void testEvaluate_nullTransformer() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);

		IExtractor extractor = Mockito.mock(IExtractor.class);
		Mockito.when(extractor.extractRawValue(any(), any())).thenReturn("Value");
		Mockito.when(extractor.getCondition())
			.thenReturn(new BooleanCondition(
				true));

		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any(), any(), any())).thenReturn("SomeValue");

		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(
			Collections.singletonList(extractor),
			null,
			converter);
		Object evaluate = evaluator.evaluate(browser, new ArrayList<>(), null);

		assertEquals("SomeValue", evaluate);
	}

	@Test
	public void testEvaluate_multipleExtractors_oneConditionMatch() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);

		IExtractor extractor1 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor1.extractRawValue(any(), any())).thenReturn("Value");
		Mockito.when(extractor1.getCondition())
			.thenReturn(new BooleanCondition(
				true));

		IExtractor extractor2 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor2.extractRawValue(any(), any())).thenReturn("Value2");
		Mockito.when(extractor2.getCondition())
			.thenReturn(new BooleanCondition(
				false));

		ArrayList<IExtractor> list = new ArrayList<>();
		list.add(extractor1);
		list.add(extractor2);

		ITransformer transformer = Mockito.mock(ITransformer.class);
		Mockito.when(transformer.transformValue("Value")).thenReturn("TheValue");

		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any(), any(), any())).thenReturn("SomeValue");

		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(
			list,
			transformer,
			converter);
		Object evaluate = evaluator.evaluate(browser, new ArrayList<>(), null);

		assertEquals("SomeValue", evaluate);
	}

	@Test
	public void testEvaluate_multipleExtractors_multipleConditionMatches() throws Exception
	{
		IValueReader browser = Mockito.mock(IValueReader.class);

		IExtractor extractor1 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor1.extractRawValue(any(), any())).thenReturn("Value");
		Mockito.when(extractor1.getCondition())
			.thenReturn(new BooleanCondition(
				true));
		Mockito.when(extractor1.describe()).thenReturn("extractor1-description");

		IExtractor extractor2 = Mockito.mock(IExtractor.class);
		Mockito.when(extractor2.extractRawValue(any(), any())).thenReturn("Value2");
		Mockito.when(extractor2.getCondition())
			.thenReturn(new BooleanCondition(
				true));
		Mockito.when(extractor2.describe()).thenReturn("extractor2-description");

		ArrayList<IExtractor> list = new ArrayList<>();
		list.add(extractor1);
		list.add(extractor2);

		ITransformer transformer = Mockito.mock(ITransformer.class);
		Mockito.when(transformer.transformValue("Value")).thenReturn("TheValue");

		IConverter converter = Mockito.mock(IConverter.class);
		Mockito.when(converter.convertValue(any(), any(), any(), any())).thenReturn("SomeValue");

		EntityFieldEvaluator evaluator = new EntityFieldEvaluator(
			list,
			transformer,
			converter);
		XEntityEvaluationException exception =
			assertThrows(XEntityEvaluationException.class, () -> evaluator.evaluate(browser, new ArrayList<>(), null));
		assertEquals("More than one Extractor applies. Extractors: extractor1-description, extractor2-description",
			exception.getMessage());
	}

}
