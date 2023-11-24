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

import com.github.jamoamo.webjourney.api.mapper.AValueMapper;
import com.github.jamoamo.webjourney.api.mapper.DoubleMapper;
import com.github.jamoamo.webjourney.api.mapper.IntegerMapper;
import com.github.jamoamo.webjourney.api.mapper.StringMapper;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;

/**
 *
 * @author James Amoore
 */
final class Mappers
{
	private Mappers(){}
	
	public static IConverter getMapperForField(IValueReader reader, EntityFieldDefn defn)
	{
		TypeInfo info = TypeInfo.forClass(defn.getFieldType());
		if(defn.getMapping() != null)
		{
			if(info.isCollectionType() && !defn.isMappedCollection())
			{
				AValueMapper mapper = InstanceCreator.getInstance().createInstance(defn.getMapping().mapper());
				return new CollectionMapper(mapper);
			}
			return new Mapper(defn.getMapping());
		}
		else if(info.isCollectionType())
		{
			return getCollectionMapper(defn, reader);
		}
		else if(!info.isStandardType())
		{
			if(defn.getExtractFromUrl() != null)
			{
				return new EntityCreatorConverter(reader, defn);
			}
			return new EntityFromElementConverter(reader, defn);
		}
		
		return determineDefaultMapper(info);
	}

	private static IConverter getCollectionMapper(EntityFieldDefn defn, IValueReader reader)
			  throws RuntimeException
	{
		FieldInfo fieldInfo = FieldInfo.forField(defn.getField());
		TypeInfo genericTypeInfo = TypeInfo.forClass(fieldInfo.getFieldGenericType());
		if(genericTypeInfo.isStandardType())
		{
			return new CollectionMapper(getDefaultMapper(genericTypeInfo));
		}
		else if(genericTypeInfo.hasNoArgsConstructor())
		{
			return new EntitiesFromElementConverter(reader, fieldInfo.getFieldGenericType());
		}
		throw new RuntimeException("Cannot create a converter for collection type " +
				  "[" + defn.getFieldName() + "] without a mapping");
	}

	private static IConverter determineDefaultMapper(TypeInfo info)
	{
		AValueMapper mapper = getDefaultMapper(info);
		return new ValueMapper(mapper);
	}

	private static AValueMapper getDefaultMapper(TypeInfo info)
			  throws RuntimeException
	{
		AValueMapper mapper;
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
		else
		{
			throw new RuntimeException("Cannot determine mapper.");
		}
		return mapper;
	}
}
