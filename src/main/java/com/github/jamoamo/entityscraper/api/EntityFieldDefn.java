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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.annotation.Collection;
import com.github.jamoamo.entityscraper.annotation.Transformation;
import com.github.jamoamo.entityscraper.annotation.XPath;
import com.github.jamoamo.entityscraper.api.mapper.AValueMapper;
import com.github.jamoamo.entityscraper.api.mapper.DefaultMapper;
import com.github.jamoamo.entityscraper.reserved.reflection.MapperCreator;
import java.lang.reflect.Field;

/**
 *
 * @author James Amoore
 */
class EntityFieldDefn
{
	private Field field;
	private XPath xpath;
	private Collection collection;
	private Transformation transformation;

	EntityFieldDefn(Field field)
	{
		if(field == null)
		{
			throw new RuntimeException("Invalid Field");
		}
		this.field = field;
		this.xpath = field.getAnnotation(XPath.class);
		this.collection = field.getAnnotation(Collection.class);
		this.transformation = field.getAnnotation(Transformation.class);
	}

	public boolean valid()
	{
		if(xpath == null && collection == null && transformation == null)
		{
			return true;
		}

		if(xpath != null && collection != null)
		{
			return false;
		}

		return !(transformation != null && (xpath == null && collection == null));
	}

	public Field getField()
	{
		return field;
	}

	public XPath getXpath()
	{
		return xpath;
	}

	public Collection getCollection()
	{
		return collection;
	}

	Transformation getTransformation()
	{
		return transformation;
	}

	boolean isSingleValue()
	{
		return xpath != null;
	}

	boolean isMultipleValue()
	{
		return collection != null;
	}

	Class<?> getType()
	{
		return field.getType();
	}

	Class<?> getMultiType()
	{
		try
		{
		String listType = field.getGenericType()
			 .getTypeName();
		String genericType = listType.substring(listType.indexOf("<") + 1, listType.length() - 1);
		return Class.forName(genericType);
		}
		catch(ClassNotFoundException ex)
		{
			throw new RuntimeException("Failed to determine type of collection.");
		}
	}
	
	public String getFieldName()
	{
		return field.getName();
	}
	
	public AValueMapper getMapper()
	{
		if(xpath != null)
		{
			return MapperCreator.getInstance().createMapper(xpath.mapperClass());
		}
		return new DefaultMapper();
	}

}
