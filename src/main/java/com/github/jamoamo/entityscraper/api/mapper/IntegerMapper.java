/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package com.github.jamoamo.entityscraper.api.mapper;

import java.lang.reflect.Field;

/**
 * Value Mapper that returns an Integer.
 * 
 * @author James Amoore
 */
public class IntegerMapper extends AValueMapper<Integer>
{
	/**
	 * Maps the value to an Integer.
	 * 
	 * @param value The value read from the HTML document.
	 * @param field The field that the value will be set on.
	 * @return The mapped value.
	 * @throws XValueMappingException If the value is not an integer.
	 */
	@Override
	public Integer mapValue(String value, Field field)
			  throws XValueMappingException
	{
		if(value == null || value.isEmpty() ||  value.isBlank())
		{
			return 0;
		}
		
		try
		{
			return Integer.valueOf(value);
		}
		catch(NumberFormatException ex)
		{
			throw new XValueMappingException(ex);
		}
	}
}
