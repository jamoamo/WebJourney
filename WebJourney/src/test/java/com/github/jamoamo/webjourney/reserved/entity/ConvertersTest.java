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

import com.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ConvertersTest
{
	
	public ConvertersTest()
	{
	}

	/**
	 * Test of getConverterForField method, of class Converters.
	 */
	@Test
	public void testGetMapperForField_String_noConverter() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.getConversion()).thenReturn(null);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getFieldType()).then(in -> String.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		
		IConverter mapperForField = Converters.getConverterForField(defn);
		
		assertInstanceOf(ValueConverter.class, mapperForField);
	}
	
	@Test
	public void testGetMapperForField_Integer_noConverter() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.getConversion()).thenReturn(null);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getFieldType()).then(in -> Integer.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		
		IConverter mapperForField = Converters.getConverterForField(defn);
		
		assertInstanceOf(ValueConverter.class, mapperForField);
	}
	
	@Test
	public void testGetMapperForField_Double_noConverter() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.getConversion()).thenReturn(null);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getFieldType()).then(in -> Double.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		
		IConverter mapperForField = Converters.getConverterForField(defn);
		
		assertInstanceOf(ValueConverter.class, mapperForField);
	}
	
}