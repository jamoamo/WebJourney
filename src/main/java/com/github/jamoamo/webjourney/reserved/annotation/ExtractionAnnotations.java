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
package com.github.jamoamo.webjourney.reserved.annotation;

import com.github.jamoamo.webjourney.annotation.ConditionalConstant;
import com.github.jamoamo.webjourney.annotation.ConditionalExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ConditionalExtractValue;
import com.github.jamoamo.webjourney.annotation.Constant;
import com.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import com.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.RegexExtractCurrentUrl;
import com.github.jamoamo.webjourney.annotation.RegexExtractValue;
import com.github.jamoamo.webjourney.reserved.entity.Extractors;
import com.github.jamoamo.webjourney.reserved.entity.IExtractor;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author James Amoore
 */
public final class ExtractionAnnotations
{
	private static final List<Class<? extends Annotation>> ALWAYS_EXTRACT_ANNOTATIONS = new ArrayList<>();
	private static final List<Class<? extends Annotation>> CONDITIONALLY_EXTRACT_ANNOTATIONS = new ArrayList<>();
	
	static 
	{
		ALWAYS_EXTRACT_ANNOTATIONS.add(ExtractValue.class);
		ALWAYS_EXTRACT_ANNOTATIONS.add(ExtractFromUrl.class);
		ALWAYS_EXTRACT_ANNOTATIONS.add(ExtractCurrentUrl.class);
		ALWAYS_EXTRACT_ANNOTATIONS.add(RegexExtractValue.class);
		ALWAYS_EXTRACT_ANNOTATIONS.add(RegexExtractCurrentUrl.class);
		ALWAYS_EXTRACT_ANNOTATIONS.add(Constant.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalExtractValue.RegexMatch.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalExtractValue.RegexMatches.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalExtractFromUrl.RegexMatch.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalExtractFromUrl.RegexMatches.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalConstant.RegexMatch.class);
		CONDITIONALLY_EXTRACT_ANNOTATIONS.add(ConditionalConstant.RegexMatches.class);
	}
	
	private final List<IExtractor> extractors;
	
	private final List<? extends Annotation> alwaysExtractAnnotations;
	
	private final List<? extends Annotation> conditionalExtractAnnotations;
	
	private final boolean hasExtractFromUrl;
	
	/**
	 * Constructor.
	 * @param fieldInfo the field info of the field.
	 * @param annotations the extract annotations.
	 * @param extractCollectionSingularly should collections be extracted as one or individually.
	 * @param hasConverter is a converter present
	 */
	public ExtractionAnnotations(
		FieldInfo fieldInfo, 
		List<? extends Annotation> annotations, 
		boolean extractCollectionSingularly,
		boolean hasConverter)
	{
		this.extractors = getExtractorsForAnnotations(fieldInfo, annotations, extractCollectionSingularly, hasConverter);
		
		this.alwaysExtractAnnotations = annotations.stream()
			.filter(a -> ALWAYS_EXTRACT_ANNOTATIONS.contains(a.annotationType()))
			.toList();
		
		this.conditionalExtractAnnotations = annotations.stream()
			.filter(a -> CONDITIONALLY_EXTRACT_ANNOTATIONS.contains(a.annotationType()))
			.toList();
		
		this.hasExtractFromUrl = annotations.stream()
			.map(a -> getFinalExtractor(a))
			.anyMatch(a -> a instanceof ExtractFromUrl);
	}
	
	/**
	 * Indicates if the annotation is an extract annotation. 
	 * @param annotation the annotation
	 * @return if the annotation is an extract annotation
	 */
	public static boolean isExtractAnnotation(Annotation annotation)
	{
		return ALWAYS_EXTRACT_ANNOTATIONS.contains(annotation.annotationType()) 
			|| CONDITIONALLY_EXTRACT_ANNOTATIONS.contains(annotation.annotationType());
	}
	
	/**
	 * @return the Always Extract Annotations
	 */
	public static List<Class<? extends Annotation>> getAlwaysExtractAnnotations()
	{
		return ALWAYS_EXTRACT_ANNOTATIONS;
	}
	
	/**
	 * @return the Conditionally Extract Annotations
	 */
	public static List<Class<? extends Annotation>> getConditionallyExtractAnnotations()
	{
		return CONDITIONALLY_EXTRACT_ANNOTATIONS;
	}

	private static List<IExtractor> getExtractorsForAnnotations(FieldInfo fieldInfo,
															List<? extends Annotation> annotations, 
															boolean extractCollectionSingularly, 
															boolean hasConverter)
	{
		return annotations.stream()
			.flatMap(annotation -> getAnnotationStream(annotation))
			.map(annotation -> getExtractor(annotation, fieldInfo, extractCollectionSingularly, hasConverter))
			.toList();
	}
	
	private static Stream<? extends Annotation> getAnnotationStream(Annotation annotation)
	{
		if(annotation instanceof ConditionalExtractFromUrl.RegexMatches regexMatches)
		{
			return Arrays.stream(regexMatches.value());
		}
		else if(annotation instanceof ConditionalExtractValue.RegexMatches regexMatches)
		{
			return Arrays.stream(regexMatches.value());
		}
		else if(annotation instanceof ConditionalConstant.RegexMatches regexMatches)
		{
			return Arrays.stream(regexMatches.value());
		}
		return Stream.of(annotation);
	}
	
	private static IExtractor getExtractor(Annotation annotation, FieldInfo fieldInfo,
															 boolean extractCollectionSingularly, boolean hasConverter)
	{
		return Extractors.getExtractorForAnnotation(annotation, fieldInfo, extractCollectionSingularly,
																  hasConverter);
	}
	
	/**
	 * @return the extractors.
	 */
	public List<IExtractor> getExtractors()
	{
		return this.extractors;
	}

	boolean hasMaxiumOneAlwaysExtractor()
	{
		return this.alwaysExtractAnnotations.size() <= 1;
	}
	
	boolean hasConditionalExtractor()
	{
		return !this.conditionalExtractAnnotations.isEmpty();
	}
	
	boolean hasAlwaysExtractor()
	{
		return !this.alwaysExtractAnnotations.isEmpty();
	}

	private Annotation getFinalExtractor(Annotation a)
	{
		if(a instanceof ConditionalExtractFromUrl.RegexMatch con)
		{
			return con.thenExtractFromUrl();
		}
		else if(a instanceof ConditionalExtractValue.RegexMatch con)
		{
			return con.thenExtractValue();
		}
		else if(a instanceof ConditionalExtractValue.RegexMatches con)
		{
			return con.value()[0].thenExtractValue();
		}
		else if(a instanceof ConditionalExtractFromUrl.RegexMatches con)
		{
			return con.value()[0].thenExtractFromUrl();
		}
		else if(a instanceof ConditionalConstant.RegexMatches con)
		{
			return con.value()[0].thenConstant();
		}
		return a;
	}

	boolean hasExtractFromUrl()
	{
		return this.hasExtractFromUrl;
	}
}
