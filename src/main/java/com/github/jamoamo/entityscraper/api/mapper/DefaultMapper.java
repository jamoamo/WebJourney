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

import com.github.jamoamo.entityscraper.api.XUnsupportedValueType;
import com.github.jamoamo.entityscraper.reserved.reflection.MapperCreator;

/**
 * Default mapper that determines the target mapper type from the Field type.
 *
 * @author James Amoore
 */
public final class DefaultMapper
		  extends AValueMapper<Object>
{
	/**
	 * Maps the value based on the field value type.
	 *
	 * @param value The value read from the HTML document.
	 * @param fieldClass The class of the field to be mapped
	 *
	 * @return The mapped value.
	 *
	 * @throws XValueMappingException if an exception occurs mapping the value.
	 */
	@Override
	public Object mapValue(String value, Class<?> fieldClass)
			  throws XValueMappingException
	{
		Class<? extends AValueMapper> defaultMapperClass = getMapperClassForField(fieldClass);
		AValueMapper mapper = getMapper(defaultMapperClass);

		return mapper.mapValue(value, fieldClass);
	}

	private Class<? extends AValueMapper> getMapperClassForField(Class<?> type)
			  throws XUnsupportedValueType
	{
		Class<? extends AValueMapper> defaultMapperClass = null;
		if(type.equals(String.class))
		{
			defaultMapperClass = StringMapper.class;
		}
		else if(type.equals(Integer.class) || "int".equals(type.getName()))
		{
			defaultMapperClass = IntegerMapper.class;
		}
		else if(type.equals(Double.class) || "double".equals(type.getName()))
		{
			defaultMapperClass = DoubleMapper.class;
		}
		else
		{
			throw new XUnsupportedValueType(type);
		}
		return defaultMapperClass;
	}

	private AValueMapper getMapper(
			  Class<? extends AValueMapper> defaultMapperClass)
			  throws XValueMappingException
	{
		return MapperCreator.getInstance().createMapper(defaultMapperClass);
	}

}
