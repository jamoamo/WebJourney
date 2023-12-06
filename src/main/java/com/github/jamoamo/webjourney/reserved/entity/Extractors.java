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

import com.github.jamoamo.webjourney.annotation.ConditionalConstant;
import com.github.jamoamo.webjourney.annotation.ConditionalExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ConditionalExtractValue;
import com.github.jamoamo.webjourney.annotation.Constant;
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
	@SuppressWarnings("MethodLength")
	public static IExtractor getExtractorForAnnotation(
		Annotation annotation,
		FieldInfo fieldInfo,
		boolean extractCollectionSingularly,
		boolean hasConverter)
	{
		if(annotation instanceof Constant constant)
		{
			return new ConstantExtractor(constant.value(), new AlwaysCondition());
		}
		if(annotation instanceof ExtractCurrentUrl)
		{
			return getCurrentUrlExtractor();
		}
		else if(annotation instanceof ExtractFromUrl extractor)
		{
			return getUrlExtractor(fieldInfo, extractor.urlXpath(), extractor.attribute(), new AlwaysCondition());
		}
		else if(annotation instanceof ExtractValue extractor)
		{
			return getValueExtractor(fieldInfo, extractor.path(), extractor.attribute(),
											 extractCollectionSingularly, hasConverter, new AlwaysCondition());
		}
		else if(annotation instanceof RegexExtractValue extractor)
		{
			return getExtractorForAnnotation(extractor.extractValue(), fieldInfo,
														extractCollectionSingularly, hasConverter);
		}
		else if(annotation instanceof ConditionalExtractValue.RegexMatch extractor)
		{
			return getRegexMatchConditionalValueExtractor(extractor, fieldInfo,
																		 extractCollectionSingularly, hasConverter);
		}
		else if(annotation instanceof ConditionalExtractFromUrl.RegexMatch extractor)
		{
			return getRegexMatchConditionalUrlExtractor(extractor, fieldInfo);
		}
		else if(annotation instanceof ConditionalConstant.RegexMatch extractor)
		{
			return new ConstantExtractor(extractor.thenConstant().value(), 
				new RegexCondition(
					getStringExtractor(extractor.ifExtractValue().path(), 
											 extractor.ifExtractValue().attribute()), 
					extractor.regexPattern()));
		}
		throw new RuntimeException("Unable to determine an extractor for annotation of type " +
			"[" + annotation.annotationType() + "].");
	}

	private static IExtractor getRegexMatchConditionalValueExtractor(ConditionalExtractValue.RegexMatch extractor,
																						  FieldInfo fieldInfo,
																						  boolean extractCollectionSingularly,
																						  boolean hasConverter)
	{
		IExtractor<String> ifExtractor = getStringExtractor(extractor.ifExtractValue().path(), extractor.
																			 ifExtractValue().attribute());
		RegexCondition condition = new RegexCondition(ifExtractor, extractor.regexPattern());
		return getValueExtractor(
			fieldInfo,
			extractor.thenExtractValue().path(),
			extractor.thenExtractValue().attribute(),
			extractCollectionSingularly,
			hasConverter,
			condition);
	}

	private static IExtractor getUrlExtractor(FieldInfo fieldInfo, String xPath, String attribute, ICondition condition)
	{
		TypeInfo fieldTypeInfo = fieldInfo.getFieldTypeInfo();

		if(fieldTypeInfo.isCollectionType() &&
			!fieldInfo.getFieldGenericTypeInfo().isStandardType() &&
			fieldInfo.getFieldGenericTypeInfo().hasNoArgsConstructor())
		{
			if(attribute.isBlank())
			{
				return new ElementTextsCollectionExtractor(xPath, condition);
			}
			else
			{
				return new AttributesExtractor(xPath, attribute, condition);
			}
		}
		else if(!fieldTypeInfo.isStandardType() && fieldTypeInfo.hasNoArgsConstructor())
		{
			if(!attribute.isBlank())
			{
				return new AttributeExtractor(xPath, attribute, condition);
			}
			else
			{
				return new ElementTextExtractor(xPath, condition);
			}
		}
		throw new RuntimeException("Cannot determine a suitable value extractor. " +
			"Missing a converter or no-args constructor?");
	}

	private static IExtractor getCurrentUrlExtractor()
	{
		return new CurrentUrlExtractor();
	}

	private static IExtractor getRegexMatchConditionalUrlExtractor(ConditionalExtractFromUrl.RegexMatch extractor,
																						FieldInfo fieldInfo)
	{
		IExtractor<String> ifExtractor = getStringExtractor(extractor.ifExtractValue().path(), extractor.
																			 ifExtractValue().attribute());
		RegexCondition condition = new RegexCondition(ifExtractor, extractor.regexPattern());
		return getUrlExtractor(
			fieldInfo,
			extractor.thenExtractFromUrl().urlXpath(),
			extractor.thenExtractFromUrl().attribute(),
			condition);
	}

	private static IExtractor getValueExtractor(
		FieldInfo fieldInfo,
		String xPath,
		String attribute,
		boolean extractCollectionSingularly,
		boolean hasConverter,
		ICondition condition)
	{
		TypeInfo typeInfo = fieldInfo.getFieldTypeInfo();
		if(!attribute.isBlank())
		{
			return getAttributeExtractor(xPath, attribute, condition);
		}
		else if(typeInfo.isCollectionType())
		{
			if(attribute.isBlank())
			{
				return getElementTextCollectionExtractor(extractCollectionSingularly, xPath, condition, fieldInfo,
																	  hasConverter);
			}
			else
			{
				return new AttributesExtractor(xPath, attribute, condition);
			}
		}
		else if(!typeInfo.isStandardType())
		{
			return getNonStandardValueExtractor(xPath, typeInfo, hasConverter);
		}
		else
		{
			return new ElementTextExtractor(xPath, condition);
		}
	}

	private static IExtractor getElementTextCollectionExtractor(boolean extractCollectionSingularly, String xPath,
																					ICondition condition, FieldInfo fieldInfo,
																					boolean hasConverter)
	{
		if(extractCollectionSingularly)
		{
			return getTextExtractor(xPath, condition);
		}
		return getCollectionExtractor(fieldInfo, xPath, hasConverter, condition);
	}

	private static IExtractor<String> getStringExtractor(String path, String attribute)
	{
		if(!attribute.isBlank())
		{
			return new AttributeExtractor(path, attribute);
		}
		else
		{
			return new ElementTextExtractor(path);
		}
	}

	private static IExtractor<String> getTextExtractor(String xPath, ICondition condition)
	{
		return new ElementTextExtractor(xPath, condition);
	}

	private static IExtractor<String> getAttributeExtractor(String xPath, String attribute, ICondition condition)
	{
		return new AttributeExtractor(xPath, attribute, condition);
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

	private static IExtractor getCollectionExtractor(FieldInfo fieldInfo, String xPath, boolean hasConverter,
																	 ICondition condition)
	{
		if(fieldInfo.getFieldGenericTypeInfo().isStandardType())
		{
			return new ElementTextsCollectionExtractor(xPath, condition);
		}
		else
		{
			if(hasConverter)
			{
				return new ElementTextsCollectionExtractor(xPath, condition);
			}
			else
			{
				return new ElementListExtractor(xPath, condition);
			}
		}
	}
}
