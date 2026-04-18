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
package io.github.jamoamo.webjourney.reserved.reflection;

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
		return getGenericType(this.field.getGenericType());
	}
	
	/**
	 * @return the TypeInfo for the field.
	 */
	public TypeInfo getFieldGenericTypeInfo()
	{
		if(getFieldGenericType() == null)
		{
			return null;
		}
		return TypeInfo.forClass(getFieldGenericType());
	}
	
	/**
	 * @return the TypeInfo for the field.
	 */
	public TypeInfo getFieldTypeInfo()
	{
		return TypeInfo.forClass(this.field.getType());
	}

	/**
	 * @return true if the field type is Optional.
	 */
	public boolean isOptionalType()
	{
		return getFieldTypeInfo().isOptionalType();
	}

	/**
	 * Returns the field type with Optional unwrapped.
	 * @return the resolved field type.
	 */
	public Class<?> getResolvedFieldType()
	{
		Type resolvedType = getResolvedFieldGenericType(this.field.getGenericType());
		return TypeToken.of(resolvedType).getRawType();
	}

	/**
	 * Returns the resolved generic type when the resolved field type is parameterized.
	 * @return the resolved generic type or null if none exists.
	 */
	public Class<?> getResolvedFieldGenericType()
	{
		return getGenericType(getResolvedFieldGenericType(this.field.getGenericType()));
	}

	/**
	 * @return the TypeInfo for the resolved field type.
	 */
	public TypeInfo getResolvedFieldTypeInfo()
	{
		return TypeInfo.forClass(getResolvedFieldType());
	}

	/**
	 * @return the TypeInfo for the resolved field generic type.
	 */
	public TypeInfo getResolvedFieldGenericTypeInfo()
	{
		Class<?> genericType = getResolvedFieldGenericType();
		if(genericType == null)
		{
			return null;
		}
		return TypeInfo.forClass(genericType);
	}

	private static Class<?> getGenericType(Type type)
	{
		if(type instanceof ParameterizedType paramType)
		{
			TypeToken<?> paramToken = TypeToken.of(paramType.getActualTypeArguments()[0]);
			return paramToken.getRawType();
		}
		return null;
	}

	private Type getResolvedFieldGenericType(Type type)
	{
		if(isOptionalType() && type instanceof ParameterizedType paramType)
		{
			return paramType.getActualTypeArguments()[0];
		}
		return type;
	}
}
