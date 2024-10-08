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

import io.github.jamoamo.webjourney.api.mapper.AConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class CollectionConverterTest
{

	 public CollectionConverterTest()
	 {
	 }

	 /**
	  * Test of convertValue method, of class CollectionTypeConverter.
	  */
	 @Test
	 public void testConvertValue()
		  throws Exception
	 {
		  AConverter<Integer> converterMock = Mockito.mock(AConverter.class);
		  Mockito.when(converterMock.mapValue("2"))
				.thenReturn(2);
		  Mockito.when(converterMock.mapValue("5"))
				.thenReturn(5);
		  Mockito.when(converterMock.mapValue("11"))
				.thenReturn(11);

		  CollectionTypeConverter<Integer> converter = new CollectionTypeConverter<>(converterMock);
		  List<String> source = new ArrayList<>();
		  source.add("2");
		  source.add("5");
		  source.add("11");
		  Collection<Integer> convertValue = converter.convertValue(source, null, new ArrayList<>(), 
				new EntityCreationContext(null));

		  assertEquals(3, convertValue.size());
		  assertTrue(convertValue.contains(2));
		  assertTrue(convertValue.contains(5));
		  assertTrue(convertValue.contains(11));
	 }

}
