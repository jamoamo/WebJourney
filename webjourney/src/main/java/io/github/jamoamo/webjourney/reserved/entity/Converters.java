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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.api.mapper.AConverter;
import io.github.jamoamo.webjourney.api.mapper.DateConverter;
import io.github.jamoamo.webjourney.api.mapper.DoubleConverter;
import io.github.jamoamo.webjourney.api.mapper.IntegerConverter;
import io.github.jamoamo.webjourney.api.mapper.StringMapper;
import io.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import io.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import io.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import io.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.reflect.Field;

/**
 *
 * @author James Amoore
 */
final class Converters
{
	private Converters(){}
	
	public static IConverter getConverterForField(EntityFieldDefn defn) throws XEntityFieldDefinitionException
	{
		FieldInfo fieldInfo = getFieldInfo(defn);
		TypeInfo info = getResolvedFieldTypeInfo(defn, fieldInfo);
		EntityAnnotations annotations = defn.getAnnotations();
		
		if(isOptionalField(fieldInfo))
		{
			return new OptionalConverter(getConverterForType(defn, fieldInfo, info, annotations));
		}
		return getConverterForType(defn, fieldInfo, info, annotations);
	}

	private static IConverter getConverterForType(
		EntityFieldDefn defn,
		FieldInfo fieldInfo,
		TypeInfo info,
		EntityAnnotations annotations) throws XEntityFieldDefinitionException
	{
		
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
			return getCollectionMapper(defn, fieldInfo);
		}
		else if(!info.isStandardType())
		{
			if(annotations.hasExtractFromUrl())
			{
				return new EntityCreatorConverter(info.getType(), defn.getField());
			}
			return new EntityFromElementConverter(info.getType());
		}
		
		return determineDefaultMapper(info);
	}

	private static IConverter getCollectionMapper(EntityFieldDefn defn, FieldInfo fieldInfo)
			  throws XEntityFieldDefinitionException
	{
		if(fieldInfo == null)
		{
			throw new XEntityFieldDefinitionException("Cannot determine collection type without field metadata.");
		}
		TypeInfo genericTypeInfo = fieldInfo.getResolvedFieldGenericTypeInfo();
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
			return new EntitiesFromElementConverter(fieldInfo.getResolvedFieldGenericType());
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
			mapper = new IntegerConverter(info.isPrimitive());
		}
		else if(info.isDouble())
		{
			mapper = new DoubleConverter(info.isPrimitive());
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

	private static FieldInfo getFieldInfo(EntityFieldDefn defn)
	{
		Field field = defn.getField();
		if(field == null)
		{
			return null;
		}
		return FieldInfo.forField(field);
	}

	private static TypeInfo getResolvedFieldTypeInfo(EntityFieldDefn defn, FieldInfo fieldInfo)
	{
		if(fieldInfo != null)
		{
			TypeInfo resolvedTypeInfo = fieldInfo.getResolvedFieldTypeInfo();
			if(resolvedTypeInfo != null)
			{
				return resolvedTypeInfo;
			}
		}
		return TypeInfo.forClass(defn.getFieldType());
	}

	private static boolean isOptionalField(FieldInfo fieldInfo)
	{
		return fieldInfo != null && fieldInfo.isOptionalType();
	}
}
