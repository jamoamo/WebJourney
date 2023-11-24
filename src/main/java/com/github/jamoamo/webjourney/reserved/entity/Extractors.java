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

import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.reflect.Field;

/**
 *
 * @author James Amoore
 */
final class Extractors
{
	private Extractors(){}
	
	static IExtractor getExtractorForField(EntityFieldDefn defn, IValueReader reader)
	{
		TypeInfo typeInfo = TypeInfo.forClass(defn.getFieldType());
		
		if(defn.getCurrentUrl() != null)
		{
			return getCurrentUrlExtractor(defn, reader, typeInfo);
		}
		else if(defn.getExtractFromUrl() != null)
		{
			return getUrlExtractor(defn, reader);
		}
		else if(defn.getExtractValue() != null)
		{
			return getValueExtractor(defn, reader, typeInfo);
		}
		
		return null;
	}
	
	private static IExtractor getCurrentUrlExtractor(EntityFieldDefn defn, IValueReader reader, TypeInfo typeInfo)
	{
		if(defn.isMappedCollection())
		{
			throw new RuntimeException("Unsupported Combination of MappedCollection and ExtractCurrentUrl");
		}
		
		if(!typeInfo.isStandardType() && defn.getMapping() == null)
		{
			throw new RuntimeException("ExtractCurrentUrl not supported for non standard Java type without a converter");
		}
		
		return new CurrentUrlExtractor(reader);
	}

	private static IExtractor getValueExtractor(EntityFieldDefn defn, IValueReader reader, TypeInfo typeInfo)
	{
		String xPath = defn.getExtractValue().path();
		if(!defn.getExtractValue().attribute().isBlank())
		{
			return new AttributeExtractor(reader, xPath, defn.getExtractValue().attribute());
		}
		else if(typeInfo.isCollectionType())
		{
			if(defn.isMappedCollection())
			{
				return new ElementTextExtractor(reader, xPath);
			}
			return getCollectionExtractor(defn, reader, xPath);
		}
		else if(!typeInfo.isStandardType())
		{
			return getNonStandardValueExtractor(defn, reader, xPath, typeInfo);
		}
		else
		{
			return new ElementTextExtractor(reader, xPath);
		}
	}

	private static IExtractor getNonStandardValueExtractor(EntityFieldDefn defn, IValueReader reader, String xPath,
																			TypeInfo typeInfo)
			  throws RuntimeException
	{
		if(defn.getMapping() != null)
		{
			return new ElementTextExtractor(reader, xPath);
		}
		if(typeInfo.hasNoArgsConstructor())
		{
			return new ElementExtractor(reader, xPath);
		}
		throw new RuntimeException("Unable to determine a suitable value extractor. " +
				  "Is there a converter or no-args constructor missing?");
	}

	private static IExtractor getCollectionExtractor(EntityFieldDefn defn, IValueReader reader, String xPath)
	{
		if(isCollectionTypeStandard(defn.getField()))
		{
			return new ElementTextsCollectionExtractor(reader, xPath);
		}
		else
		{
			if(defn.getMapping() != null)
			{
				return new ElementTextsCollectionExtractor(reader, xPath);
			}
			else
			{
				return new ElementListExtractor(reader, xPath);
			}
		}
	}

	private static IExtractor getUrlExtractor(EntityFieldDefn defn, IValueReader browser)
	{
		if(!TypeInfo.forClass(defn.getFieldType()).isStandardType() 
				  && TypeInfo.forClass(defn.getFieldType()).hasNoArgsConstructor())
		{
			if(!defn.getExtractFromUrl().attribute().isBlank())
			{
				return new AttributeExtractor(browser, defn.getExtractFromUrl().urlXpath(),
						  defn.getExtractFromUrl().attribute());
			}
			else
			{
				return new ElementTextExtractor(browser, defn.getExtractFromUrl().urlXpath());
			}
		}
		throw new RuntimeException("Cannot determine a suitable value extractor. " +
					  "Missing a converter or no-args constructor?");
	}
	
	private static boolean isCollectionTypeStandard(Field field)
	{
		FieldInfo fieldInfo = FieldInfo.forField(field);
		TypeInfo typeInfo = TypeInfo.forClass(field.getType());
		if(typeInfo.isCollectionType())
		{
			Class<?> genType = fieldInfo.getFieldGenericType();
			return TypeInfo.forClass(genType).isStandardType();
		}
		else if(typeInfo.isArrayType())
		{
			TypeInfo arrayTypeInfo = TypeInfo.forClass(field.getType().arrayType());
			return arrayTypeInfo.isStandardType();
		}
		return false;
	}
}
