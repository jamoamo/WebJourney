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
package com.github.jamoamo.entityscraper.reserved.reflection;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author James Amoore
 */
public final class FieldInfo
{	
	private final Field field;
	
	private FieldInfo(Field field)
	{
		this.field = field;
	}
	
	/**
	 * Creates a FieldInfo for the provided field.
	 * @param field The field.
	 * @return a FieldInfo instance.
	 */
	public static FieldInfo forField(Field field)
	{
		return new FieldInfo(field);
	}
	
	/**
	 * @return the type of the generic param.
	 */
	public Class<?> getFieldGenericType()
	{
		Type type = this.field.getGenericType();
		if (type instanceof ParameterizedType) 
		{
			ParameterizedType paramType = (ParameterizedType)type;
			TypeToken paramToken = TypeToken.of(paramType.getActualTypeArguments()[0]);
			return paramToken.getRawType();
		}
		return null;
	}
}
