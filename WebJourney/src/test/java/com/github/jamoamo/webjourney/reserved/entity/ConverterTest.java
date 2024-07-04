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

import com.github.jamoamo.webjourney.annotation.Conversion;
import com.github.jamoamo.webjourney.api.mapper.AConverter;
import com.github.jamoamo.webjourney.api.mapper.XValueMappingException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author James Amoore
 */
public class ConverterTest
{
	public static class TestConverter extends AConverter<String>
	{
		public TestConverter()
		{
		}

		@Override
		public String mapValue(String value)
			throws XValueMappingException
		{
			return "--" + value + "--";
		}
	}
	
	/**
	 * Test of convertValue method, of class Converter.
	 */
	@Test
	public void testConvertValue() throws Exception
	{
		Conversion conversion = Mockito.mock(Conversion.class);
		Answer<Class<TestConverter>> answer = (InvocationOnMock iom) -> TestConverter.class;
		Mockito.when(conversion.mapper()).then(answer);
		
		Converter converter = new Converter(conversion);
		
		Object convertValue = converter.convertValue("A Test", null, new ArrayList<>());
		assertEquals("--A Test--", convertValue);
	}
	
}
