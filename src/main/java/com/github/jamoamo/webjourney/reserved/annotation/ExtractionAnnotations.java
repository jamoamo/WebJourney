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

import com.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import com.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.RegexExtractValue;
import com.github.jamoamo.webjourney.reserved.entity.Extractors;
import com.github.jamoamo.webjourney.reserved.entity.IExtractor;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public class ExtractionAnnotations
{
	private static final List<Class<? extends Annotation>> EXTRACT_ANNOTATION_CLASSES = new ArrayList<>();
	
	static 
	{
		EXTRACT_ANNOTATION_CLASSES.add(ExtractValue.class);
		EXTRACT_ANNOTATION_CLASSES.add(ExtractFromUrl.class);
		EXTRACT_ANNOTATION_CLASSES.add(ExtractCurrentUrl.class);
		EXTRACT_ANNOTATION_CLASSES.add(RegexExtractValue.class);
	}
	
	private final List<IExtractor> extractors;
	
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
	}
	
	/**
	 * Indicates if the annotation is an extract annotation. 
	 * @param annotation the annotation
	 * @return if the annotation is an extract annotation
	 */
	public static boolean isExtractAnnotation(Annotation annotation)
	{
		return EXTRACT_ANNOTATION_CLASSES.contains(annotation.annotationType());
	}

	private static List<IExtractor> getExtractorsForAnnotations(FieldInfo fieldInfo,
															List<? extends Annotation> annotations, 
															boolean extractCollectionSingularly, 
															boolean hasConverter)
	{
		return annotations.stream()
			.map(annotation -> getExtractor(annotation, fieldInfo, extractCollectionSingularly, hasConverter))
			.toList();
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
}
