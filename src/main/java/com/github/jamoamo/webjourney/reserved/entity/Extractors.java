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

import com.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import com.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.RegexExtractValue;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.annotation.Annotation;

/**
 *
 * @author James Amoore
 */
public final class Extractors
{
	private Extractors()
	{
	}

	/**
	 * Returns an extractor for the annotation.
	 *
	 * @param annotation                  the annotation
	 * @param fieldInfo                   information about the field
	 * @param extractCollectionSingularly whether collections should be extracted as one.
	 * @param hasConverter                whether a converter is present
	 *
	 * @return the extractor
	 */
	public static IExtractor getExtractorForAnnotation(
		Annotation annotation,
		FieldInfo fieldInfo,
		boolean extractCollectionSingularly,
		boolean hasConverter)
	{
		if(annotation instanceof ExtractCurrentUrl)
		{
			return getCurrentUrlExtractor();
		}
		else if(annotation instanceof ExtractFromUrl extractor)
		{
			return getUrlExtractor(fieldInfo, extractor.urlXpath(), extractor.attribute());
		}
		else if(annotation instanceof ExtractValue extractor)
		{
			return getValueExtractor(fieldInfo, extractor.path(), extractor.attribute(),
											 extractCollectionSingularly, hasConverter);
		}
		else if(annotation instanceof RegexExtractValue extractor)
		{
			return getExtractorForAnnotation(extractor.extractValue(), fieldInfo,
														extractCollectionSingularly, hasConverter);
		}
		return null;
	}

	private static IExtractor getCurrentUrlExtractor()
	{
		return new CurrentUrlExtractor();
	}

	private static IExtractor getUrlExtractor(FieldInfo fieldInfo, String xPath, String attribute)
	{
		TypeInfo fieldTypeInfo = fieldInfo.getFieldTypeInfo();

		if(!fieldTypeInfo.isStandardType() && fieldTypeInfo.hasNoArgsConstructor())
		{
			if(!attribute.isBlank())
			{
				return new AttributeExtractor(xPath, attribute);
			}
			else
			{
				return new ElementTextExtractor(xPath);
			}
		}
		throw new RuntimeException("Cannot determine a suitable value extractor. " +
			"Missing a converter or no-args constructor?");
	}

	private static IExtractor getValueExtractor(
		FieldInfo fieldInfo,
		String xPath,
		String attribute,
		boolean extractCollectionSingularly,
		boolean hasConverter)
	{
		TypeInfo typeInfo = fieldInfo.getFieldTypeInfo();
		if(!attribute.isBlank())
		{
			return getAttributeExtractor(xPath, attribute);
		}
		else if(typeInfo.isCollectionType())
		{
			if(extractCollectionSingularly)
			{
				return getTextExtractor(xPath);
			}
			return getCollectionExtractor(fieldInfo, xPath, hasConverter);
		}
		else if(!typeInfo.isStandardType())
		{
			return getNonStandardValueExtractor(xPath, typeInfo, hasConverter);
		}
		else
		{
			return getTextExtractor(xPath);
		}
	}

	private static IExtractor getTextExtractor(String xPath)
	{
		return new ElementTextExtractor(xPath);
	}

	private static IExtractor getAttributeExtractor(String xPath, String attribute)
	{
		return new AttributeExtractor(xPath, attribute);
	}

	private static IExtractor getNonStandardValueExtractor(
		String xPath,
		TypeInfo typeInfo,
		boolean hasConverter)
		throws RuntimeException
	{
		if(hasConverter)
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

	private static IExtractor getCollectionExtractor(FieldInfo fieldInfo, String xPath, boolean hasConverter)
	{
		if(fieldInfo.getFieldGenericTypeInfo().isStandardType())
		{
			return new ElementTextsCollectionExtractor(xPath);
		}
		else
		{
			if(hasConverter)
			{
				return new ElementTextsCollectionExtractor(xPath);
			}
			else
			{
				return new ElementListExtractor(xPath);
			}
		}
	}
}
