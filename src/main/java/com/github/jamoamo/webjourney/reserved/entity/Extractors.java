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

import com.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
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
	
	static IExtractor getExtractorForField(EntityFieldDefn defn)
	{
		TypeInfo typeInfo = TypeInfo.forClass(defn.getFieldType());
		EntityAnnotations annotations = defn.getAnnotations();
		
		if (annotations.hasCurrentUrl())
		{
			return getCurrentUrlExtractor(defn, typeInfo);
		}
		else if(annotations.hasExtractFromUrl())
		{
			return getUrlExtractor(defn);
		}
		else if(annotations.hasExtractValue())
		{
			return getValueExtractor(defn, typeInfo);
		}
		
		return null;
	}
	
	private static IExtractor getCurrentUrlExtractor(EntityFieldDefn defn, TypeInfo typeInfo)
	{
		EntityAnnotations annotations = defn.getAnnotations();
		if(defn.isMappedCollection())
		{
			throw new RuntimeException("Unsupported Combination of MappedCollection and ExtractCurrentUrl");
		}
		
		if(!typeInfo.isStandardType() && annotations.hasConversion())
		{
			throw new RuntimeException("ExtractCurrentUrl not supported for non standard Java type without a converter");
		}
		
		return new CurrentUrlExtractor();
	}

	private static IExtractor getValueExtractor(EntityFieldDefn defn, TypeInfo typeInfo)
	{
		EntityAnnotations annotations = defn.getAnnotations();
		String xPath = annotations.getExtractValue().path();
		
		if(!annotations.getExtractValue().attribute().isBlank())
		{
			return getAttributeExtractor(defn, xPath);
		}
		else if(typeInfo.isCollectionType())
		{
			if(annotations.hasMappedCollection())
			{
				return getTextExtractor(defn, xPath);
			}
			return getCollectionExtractor(defn, xPath);
		}
		else if(!typeInfo.isStandardType())
		{
			return getNonStandardValueExtractor(defn, xPath, typeInfo);
		}
		else
		{
			return getTextExtractor(defn, xPath);
		}
	}

	private static IExtractor getTextExtractor(EntityFieldDefn defn, String xPath)
	{

		return new ElementTextExtractor(xPath);
	}

	private static IExtractor getAttributeExtractor(EntityFieldDefn defn, String xPath)
	{
		String attribute = defn.getAnnotations().getExtractValue().attribute();
		return new AttributeExtractor(xPath, attribute);
	}

	private static IExtractor getNonStandardValueExtractor(EntityFieldDefn defn, String xPath,
																			TypeInfo typeInfo)
			  throws RuntimeException
	{
		if(defn.getAnnotations().getConversion() != null)
		{
			return new ElementTextExtractor(xPath);
		}
		if(typeInfo.hasNoArgsConstructor())
		{
			return new ElementExtractor(xPath);
		}
		throw new RuntimeException("Unable to determine a suitable value extractor. " +
				  "Is there a converter or no-args constructor missing?");
	}

	private static IExtractor getCollectionExtractor(EntityFieldDefn defn, String xPath)
	{
		if(isCollectionTypeStandard(defn.getField()))
		{
			return new ElementTextsCollectionExtractor(xPath);
		}
		else
		{
			if(defn.getAnnotations().getConversion() != null)
			{
				return new ElementTextsCollectionExtractor(xPath);
			}
			else
			{
				return new ElementListExtractor(xPath);
			}
		}
	}

	private static IExtractor getUrlExtractor(EntityFieldDefn defn)
	{
		if(!TypeInfo.forClass(defn.getFieldType()).isStandardType() 
				  && TypeInfo.forClass(defn.getFieldType()).hasNoArgsConstructor())
		{
			if(!defn.getAnnotations().getExtractFromUrl().attribute().isBlank())
			{
				return new AttributeExtractor(defn.getAnnotations().getExtractFromUrl().urlXpath(),
						  defn.getAnnotations().getExtractFromUrl().attribute());
			}
			else
			{
				return new ElementTextExtractor(defn.getAnnotations().getExtractFromUrl().urlXpath());
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
