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
package io.github.jamoamo.webjourney.api.mapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class DoubleConverterTest
{
	@Test
	public void testMapValue_null_primitive()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = null;
		DoubleConverter instance = new DoubleConverter();
		Double result = instance.mapValue(value);
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void testMapValue_null_boxed()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = null;
		DoubleConverter instance = new DoubleConverter(false);
		Double result = instance.mapValue(value);
		assertNull(result);
	}
	
	@Test
	public void testMapValue_blank_primitive()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = " ";
		DoubleConverter instance = new DoubleConverter();
		Double result = instance.mapValue(value);
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void testMapValue_blank_boxed()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = " ";
		DoubleConverter instance = new DoubleConverter(false);
		Double result = instance.mapValue(value);
		assertNull(result);
	}
	
	@Test
	public void testMapValue_empty_primitive()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = "";
		DoubleConverter instance = new DoubleConverter();
		Double result = instance.mapValue(value);
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void testMapValue_empty_boxed()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = "";
		DoubleConverter instance = new DoubleConverter(false);
		Double result = instance.mapValue(value);
		assertNull(result);
	}
	
	@Test
	public void testMapValue_valid()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = "19.67";
		DoubleConverter instance = new DoubleConverter();
		Double result = instance.mapValue(value);
		assertEquals(19.67, result, 0.001);
	}
	
	@Test
	public void testMapValue_invalid()
		 throws Exception
	{
		System.out.println("mapValue");
		String value = "Nineteen";
		DoubleConverter instance = new DoubleConverter();
		XValueMappingException ex = assertThrows(XValueMappingException.class, () -> instance.mapValue(value));
		assertInstanceOf(NumberFormatException.class, ex.getCause());
	}
	
}
