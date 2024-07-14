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

import io.github.jamoamo.webjourney.annotation.ConditionalConstant;
import io.github.jamoamo.webjourney.annotation.ConditionalExtractFromUrl;
import io.github.jamoamo.webjourney.annotation.ConditionalExtractValue;
import io.github.jamoamo.webjourney.annotation.Constant;
import io.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import io.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.annotation.RegexExtractCurrentUrl;
import io.github.jamoamo.webjourney.annotation.RegexExtractValue;
import io.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import io.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
@SuppressWarnings(value = {"MethodLength", "MultipleStringLiterals"})
public final class Extractors
{
	 private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreator.class);

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
		  if(annotation instanceof Constant constant)
		  {
				LOGGER.debug("Using ConstantExtractor with value = " + constant.value());
				return new ConstantExtractor(constant.value(), new AlwaysCondition());
		  }
		  if(annotation instanceof ExtractCurrentUrl)
		  {
				LOGGER.debug("Finding Extractor for current url.");
				return getCurrentUrlExtractor();
		  }
		  else if(annotation instanceof ExtractFromUrl extractor)
		  {
				LOGGER.debug("Finding Extractor from url.");
				return getUrlExtractor(fieldInfo, extractor.urlXpath(), extractor.attribute(), new AlwaysCondition());
		  }
		  else if(annotation instanceof ExtractValue extractor)
		  {
				LOGGER.debug("Finding Extractor for value extraction.");
				return getValueExtractor(fieldInfo, extractor.path(), extractor.attribute(), extractor.optional(),
					 extractCollectionSingularly, hasConverter, new AlwaysCondition());
		  }
		  else if(annotation instanceof RegexExtractValue extractor)
		  {
				LOGGER.debug("Finding Extractor for value extraction using regex.");
				return getExtractorForAnnotation(extractor.extractValue(), fieldInfo,
					 extractCollectionSingularly, hasConverter);
		  }
		  else if(annotation instanceof RegexExtractCurrentUrl)
		  {
				LOGGER.debug("Finding Extractor for current url using regex.");
				return getExtractorForAnnotation(new ExtractCurrentUrl()
				{
					 @Override
					 public Class<? extends Annotation> annotationType()
					 {
						  return ExtractCurrentUrl.class;
					 }

				}, fieldInfo, extractCollectionSingularly, hasConverter);
		  }
		  else if(annotation instanceof ConditionalExtractValue.RegexMatch extractor)
		  {
				LOGGER.debug("Finding Extractor for regex conditional value.");
				return getRegexMatchConditionalValueExtractor(extractor, fieldInfo,
					 extractCollectionSingularly, hasConverter);
		  }
		  else if(annotation instanceof ConditionalExtractFromUrl.RegexMatch extractor)
		  {
				LOGGER.debug("Finding Extractor for regex conditional url.");
				return getRegexMatchConditionalUrlExtractor(extractor, fieldInfo);
		  }
		  else if(annotation instanceof ConditionalConstant.RegexMatch extractor)
		  {
				LOGGER.debug("Finding Extractor for regex conditional constant.");
				return new ConstantExtractor(extractor.thenConstant()
					 .value(),
					 new RegexCondition(
						  getStringExtractor(extractor.ifExtractValue()
								.path(),
								extractor.ifExtractValue()
									 .attribute(),
								true),
						  extractor.regexPattern()));
		  }
		  LOGGER.debug("No extractor found");
		  throw new RuntimeException("Unable to determine an extractor for annotation of type " + "[" + annotation
				.annotationType() + "].");
	 }

	 private static IExtractor getRegexMatchConditionalValueExtractor(ConditionalExtractValue.RegexMatch extractor,
		  FieldInfo fieldInfo,
		  boolean extractCollectionSingularly,
		  boolean hasConverter)
	 {
		  IExtractor<String> ifExtractor = getStringExtractor(extractor.ifExtractValue()
				.path(), extractor.
					 ifExtractValue()
					 .attribute(), true);
		  RegexCondition condition = new RegexCondition(ifExtractor, extractor.regexPattern());
		  LOGGER.debug("Get Value Extractor with regex condition match");
		  return getValueExtractor(
				fieldInfo,
				extractor.thenExtractValue()
					 .path(),
				extractor.thenExtractValue()
					 .attribute(),
				false,
				extractCollectionSingularly,
				hasConverter,
				condition);
	 }

	 private static IExtractor getUrlExtractor(FieldInfo fieldInfo, String xPath, String attribute, ICondition condition)
	 {
		  TypeInfo fieldTypeInfo = fieldInfo.getFieldTypeInfo();

		  if(fieldTypeInfo.isCollectionType() && !fieldInfo.getFieldGenericTypeInfo()
				.isStandardType() && fieldInfo.getFieldGenericTypeInfo()
					 .hasNoArgsConstructor())
		  {
				if(attribute.isBlank())
				{
					 LOGGER.debug("Using ElementTextsCollectionExtractor for xpath = " + xPath);
					 return new ElementTextsCollectionExtractor(xPath, condition);
				}
				else
				{
					 LOGGER.debug("Using AttributesExtractor for xpath = " + xPath + " and attribute = " + attribute + 
						  " and optional = false");
					 return new AttributesExtractor(xPath, attribute, condition, false);
				}
		  }
		  else if(!fieldTypeInfo.isStandardType() && fieldTypeInfo.hasNoArgsConstructor())
		  {
				if(!attribute.isBlank())
				{
					 LOGGER.debug("Using AttributesExtractor for xpath = " + xPath + " and attribute = " + attribute + 
						  " and optional = false");
					 return new AttributeExtractor(xPath, attribute, condition, false);
				}
				else
				{
					 LOGGER.debug("Using ElementTextExtractor for xpath = " + xPath + " and optional = false");
					 return new ElementTextExtractor(xPath, condition, false);
				}
		  }
		  throw new RuntimeException("Cannot determine a suitable value extractor. "
				+ "Missing a converter or no-args constructor?");
	 }

	 private static IExtractor getCurrentUrlExtractor()
	 {
		  LOGGER.debug("Using CurrentUrlExtractor");
		  return new CurrentUrlExtractor();
	 }

	 private static IExtractor getRegexMatchConditionalUrlExtractor(ConditionalExtractFromUrl.RegexMatch extractor,
		  FieldInfo fieldInfo)
	 {
		  IExtractor<String> ifExtractor = getStringExtractor(extractor.ifExtractValue()
				.path(), extractor.
					 ifExtractValue()
					 .attribute(), true);
		  RegexCondition condition = new RegexCondition(ifExtractor, extractor.regexPattern());
		  LOGGER.debug("Get url Extractor with regex condition match");
		  return getUrlExtractor(
				fieldInfo,
				extractor.thenExtractFromUrl()
					 .urlXpath(),
				extractor.thenExtractFromUrl()
					 .attribute(),
				condition);
	 }

	 private static IExtractor getValueExtractor(
		  FieldInfo fieldInfo,
		  String xPath,
		  String attribute,
		  boolean optional,
		  boolean extractCollectionSingularly,
		  boolean hasConverter,
		  ICondition condition)
	 {
		  TypeInfo typeInfo = fieldInfo.getFieldTypeInfo();
		  if(typeInfo.isCollectionType())
		  {
				if(attribute.isBlank())
				{
					 LOGGER.debug("Getting collection of element text extractor. extractCollectionSingularly = " 
						  + extractCollectionSingularly);
					 return getElementTextCollectionExtractor(extractCollectionSingularly, xPath, condition, fieldInfo,
						  hasConverter, optional);
				}
				else
				{
					 LOGGER.debug("Using AttributesExtractor for xpath = " + xPath + "and attribute = " + attribute 
						  + " and optional = " + optional);
					 return new AttributesExtractor(xPath, attribute, condition, optional);
				}
		  }
		  else if(!attribute.isBlank())
		  {
				LOGGER.debug("Getting attribute extractor.");
				return getAttributeExtractor(xPath, attribute, condition, optional);
		  }
		  else if(!typeInfo.isStandardType())
		  {
				LOGGER.debug("Getting non standard value extractor.");
				return getNonStandardValueExtractor(xPath, typeInfo, hasConverter, optional);
		  }
		  else
		  {
				LOGGER.debug("Using ElementTextExtractor for xpath = " + xPath + " and optional = " + optional);
				return new ElementTextExtractor(xPath, condition, optional);
		  }
	 }

	 private static IExtractor getElementTextCollectionExtractor(boolean extractCollectionSingularly,
		  String xPath, ICondition condition,
		  FieldInfo fieldInfo, boolean hasConverter,
		  boolean optional)
	 {
		  if(extractCollectionSingularly)
		  {
				LOGGER.debug("Getting text extractor.");
				return getTextExtractor(xPath, condition, optional);
		  }
		  return getCollectionExtractor(fieldInfo, xPath, hasConverter, condition);
	 }

	 private static IExtractor<String> getStringExtractor(String path, String attribute, boolean optional)
	 {
		  if(!attribute.isBlank())
		  {
				LOGGER.debug("Using AttributeExtractor for xpath = " + path + "attribute = " + attribute 
					 + " and optional = " + optional);
				return new AttributeExtractor(path, attribute, new AlwaysCondition(), optional);
		  }
		  else
		  {
				LOGGER.debug("Using ElementTextExtractor for xpath = " + path + " and optional = " + optional);
				return new ElementTextExtractor(path, new AlwaysCondition(), optional);
		  }
	 }

	 private static IExtractor<String> getTextExtractor(String xPath, ICondition condition, boolean optional)
	 {
		  LOGGER.debug("Using ElementTextExtractor for xpath = " + xPath + " and optional = " + optional);
		  return new ElementTextExtractor(xPath, condition, optional);
	 }

	 private static IExtractor<String> getAttributeExtractor(String xPath, String attribute, ICondition condition,
		  boolean optional)
	 {
		  LOGGER.debug("Using AttributeExtractor for xpath = " + xPath + " attribute = " + attribute 
				+ " and optional = " + optional);
		  return new AttributeExtractor(xPath, attribute, condition, optional);
	 }

	 private static IExtractor getNonStandardValueExtractor(
		  String xPath, TypeInfo typeInfo, boolean hasConverter, boolean optional)
		  throws RuntimeException
	 {
		  if(hasConverter)
		  {
				LOGGER.debug("Using ElementTextExtractor for xpath = " + xPath + " and optional = " + optional);
				return new ElementTextExtractor(xPath, new AlwaysCondition(), optional);
		  }
		  if(typeInfo.hasNoArgsConstructor())
		  {
				LOGGER.debug("Using ElementExtractor for xpath = " + xPath + " and optional = " + optional);
				return new ElementExtractor(xPath, optional);
		  }
		  throw new RuntimeException("Unable to determine a suitable value extractor. "
				+ "Is there a converter or no-args constructor missing?");
	 }

	 private static IExtractor getCollectionExtractor(FieldInfo fieldInfo, String xPath,
		  boolean hasConverter, ICondition condition)
	 {
		  if(fieldInfo.getFieldGenericTypeInfo()
				.isStandardType())
		  {
				LOGGER.debug("Using ElementTextsCollectionExtractor for xpath = " + xPath);
				return new ElementTextsCollectionExtractor(xPath, condition);
		  }
		  else
		  {
				if(hasConverter)
				{
					 LOGGER.debug("Using ElementTextsCollectionExtractor for xpath = " + xPath);
					 return new ElementTextsCollectionExtractor(xPath, condition);
				}
				else
				{
					 LOGGER.debug("Using ElementListExtractor for xpath = " + xPath);
					 return new ElementListExtractor(xPath, condition);
				}
		  }
	 }

}
