/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class TextNodeCollectionExtractorTest
{
	@Test
	public void testExtractRawValue() throws Exception
	{
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getTextNodeValues("text()"))
			.thenReturn(List.of("0", "1lb", "5w"));

		TextNodeCollectionExtractor extractor = new TextNodeCollectionExtractor("text()", new AlwaysCondition());
		List<String> result = extractor.extractRawValue(reader, null);

		assertEquals(List.of("0", "1lb", "5w"), result);
	}

	@Test
	public void testExtractRawValue_empty() throws Exception
	{
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getTextNodeValues("text()"))
			.thenReturn(List.of());

		TextNodeCollectionExtractor extractor = new TextNodeCollectionExtractor("text()", new AlwaysCondition());
		List<String> result = extractor.extractRawValue(reader, null);

		assertEquals(List.of(), result);
	}

	@Test
	public void testExtractRawValue_wrapsReaderException()
	{
		IValueReader reader = Mockito.mock(IValueReader.class);

		try
		{
			Mockito.when(reader.getTextNodeValues("text()"))
				.thenThrow(new XValueReaderException(
					new io.github.jamoamo.webjourney.api.web.XElementDoesntExistException()));
		}
		catch (XValueReaderException ex)
		{
			throw new IllegalStateException(ex);
		}

		TextNodeCollectionExtractor extractor = new TextNodeCollectionExtractor("text()", new AlwaysCondition());

		org.junit.jupiter.api.Assertions.assertThrows(XExtractionException.class,
			() -> extractor.extractRawValue(reader, null));
	}

	@Test
	public void testDescribe()
	{
		TextNodeCollectionExtractor extractor = new TextNodeCollectionExtractor("text()", new AlwaysCondition());
		org.junit.jupiter.api.Assertions.assertTrue(extractor.describe().contains("text()"));
	}

}
