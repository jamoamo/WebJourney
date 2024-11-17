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
package io.github.jamoamo.webjourney.api.mapper;

/**
 * Value mapper that returns a Double.
 *
 * @author James Amoore
 */
public class DoubleConverter
	 extends AConverter<Double>
{
	private final boolean isPrimitive;
	
	/**
	 * Constructor.
	 */
	public DoubleConverter()
	{
		this.isPrimitive = true;
	}
	
	/**
	 * Constructor.
	 * @param isPrimitive Should the result be treated as a primitive. i.e. should it allow nulls.
	 */
	public DoubleConverter(boolean isPrimitive)
	{
		this.isPrimitive = isPrimitive;
	}
	
	/**
	 * Maps the value to a double.
	 *
	 * @param value The value read from the file
	 *
	 * @return the mapped value
	 *
	 * @throws XValueMappingException if the value can't be converted to a double.
	 */
	@Override
	public Double mapValue(String value)
		 throws XValueMappingException
	{
		if(value == null || value.isEmpty() || value.isBlank())
		{
			if(!this.isPrimitive)
			{
				return null;
			}
			return 0.0;
		}

		try
		{
			return Double.valueOf(value);
		}
		catch(NumberFormatException ex)
		{
			throw new XValueMappingException(ex);
		}
	}

}
