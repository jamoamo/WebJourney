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
package com.github.jamoamo.webjourney.api.mapper;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class DateConverterTest
{
	
	public DateConverterTest()
	{
	}

	/**
	 * Test of mapValue method, of class DateConverter.
	 */
	@Test
	public void testMapValue_th_February()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("28th February 2023");
		assertEquals(28, date.getDayOfMonth());
		assertEquals(Month.FEBRUARY, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_st_January()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("1st January 1901");
		assertEquals(1, date.getDayOfMonth());
		assertEquals(Month.JANUARY, date.getMonth());
		assertEquals(1901, date.getYear());
	}
	
	@Test
	public void testMapValue_nd_March()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("22nd March 1956");
		assertEquals(22, date.getDayOfMonth());
		assertEquals(Month.MARCH, date.getMonth());
		assertEquals(1956, date.getYear());
	}
	
	@Test
	public void testMapValue_th_April()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("4th April 1960");
		assertEquals(4, date.getDayOfMonth());
		assertEquals(Month.APRIL, date.getMonth());
		assertEquals(1960, date.getYear());
	}
	
	@Test
	public void testMapValue_th_May()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("10th May 2023");
		assertEquals(10, date.getDayOfMonth());
		assertEquals(Month.MAY, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_June()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th June 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.JUNE, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_July()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th July 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.JULY, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_August()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th August 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.AUGUST, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	@Test
	public void testMapValue_th_September()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th September 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.SEPTEMBER, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_October()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th October 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.OCTOBER, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_November()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th November 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.NOVEMBER, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_th_December()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("11th December 2023");
		assertEquals(11, date.getDayOfMonth());
		assertEquals(Month.DECEMBER, date.getMonth());
		assertEquals(2023, date.getYear());
	}
	
	@Test
	public void testMapValue_null()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue(null);
		assertNull(date);
	}
	
	@Test
	public void testMapValue_blank()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue(" ");
		assertNull(date);
	}
	
	@Test
	public void testMapValue_empty()
		throws Exception
	{
		DateConverter converter = new DateConverter();
		LocalDate date = converter.mapValue("");
		assertNull(date);
	}
}
