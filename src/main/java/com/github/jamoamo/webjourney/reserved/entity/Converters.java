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

import com.github.jamoamo.webjourney.api.mapper.AConverter;
import com.github.jamoamo.webjourney.api.mapper.DateConverter;
import com.github.jamoamo.webjourney.api.mapper.DoubleMapper;
import com.github.jamoamo.webjourney.api.mapper.IntegerMapper;
import com.github.jamoamo.webjourney.api.mapper.StringMapper;
import com.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;

/**
 *
 * @author James Amoore
 */
final class Converters
{
	private Converters(){}
	
	public static IConverter getConverterForField(EntityFieldDefn defn) throws XEntityFieldDefinitionException
	{
		TypeInfo info = TypeInfo.forClass(defn.getFieldType());
		EntityAnnotations annotations = defn.getAnnotations();
		
		if(annotations.getConversion() != null)
		{
			if(info.isCollectionType() && !annotations.hasMappedCollection())
			{
				AConverter mapper = InstanceCreator.getInstance().createInstance(annotations.getConversion().mapper());
				return new CollectionTypeConverter(mapper);
			}
			return new Converter(annotations.getConversion());
		}
		else if(info.isCollectionType())
		{
			return getCollectionMapper(defn);
		}
		else if(!info.isStandardType())
		{
			if(annotations.hasExtractFromUrl())
			{
				return new EntityCreatorConverter(defn);
			}
			return new EntityFromElementConverter(defn);
		}
		
		return determineDefaultMapper(info);
	}

	private static IConverter getCollectionMapper(EntityFieldDefn defn)
			  throws XEntityFieldDefinitionException
	{
		FieldInfo fieldInfo = FieldInfo.forField(defn.getField());
		TypeInfo genericTypeInfo = TypeInfo.forClass(fieldInfo.getFieldGenericType());
		if(genericTypeInfo.isStandardType())
		{
			return new CollectionTypeConverter(getDefaultMapper(genericTypeInfo));
		}
		else if(genericTypeInfo.hasNoArgsConstructor())
		{
			if(defn.getAnnotations().hasExtractFromUrl())
			{
				return new EntitiesCreatorConverter(defn);
			}
			return new EntitiesFromElementConverter(fieldInfo.getFieldGenericType());
		}
		throw new XEntityFieldDefinitionException("Cannot create a converter for collection type " +
				  "[" + defn.getFieldName() + "] without a mapping");
	}

	private static IConverter determineDefaultMapper(TypeInfo info)
	{
		AConverter mapper = getDefaultMapper(info);
		return new ValueConverter(mapper);
	}

	private static AConverter getDefaultMapper(TypeInfo info)
			  throws RuntimeException
	{
		AConverter mapper;
		if(info.isStringType())
		{
			mapper = new StringMapper();
		}
		else if(info.isInteger())
		{
			mapper = new IntegerMapper();
		}
		else if(info.isDouble())
		{
			mapper = new DoubleMapper();
		}
		else if(info.isDateType())
		{
			mapper = new DateConverter();
		}
		else
		{
			throw new RuntimeException("Cannot determine mapper.");
		}
		return mapper;
	}
}
