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

import com.github.jamoamo.webjourney.annotation.Conversion;
import com.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import com.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.MappedCollection;
import com.github.jamoamo.webjourney.annotation.Transformation;
import java.lang.reflect.Field;
import java.util.Arrays;
import com.github.jamoamo.webjourney.annotation.RegexExtractValue;
import com.github.jamoamo.webjourney.reserved.entity.IExtractor;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A collection of the annotations for an entity.
 * @author James Amoore
 */
public class EntityAnnotations
{
	private final ExtractValue extractValue;
	private final ExtractCurrentUrl currentUrl;
	private final ExtractFromUrl extractFromUrl;
	private final RegexExtractValue regexExtract;
	private final MappedCollection mappedCollection;
	private final Transformation transformation;
	private final Conversion conversion;
	
	private final ExtractionAnnotations extractionAnnotations;

	/**
	 * Constructor.
	 * @param field The field to get the annotations from.
	 */
	public EntityAnnotations(Field field)
	{
		this.extractValue = field.getAnnotation(ExtractValue.class);
		this.currentUrl = field.getAnnotation(ExtractCurrentUrl.class);
		this.extractFromUrl = field.getAnnotation(ExtractFromUrl.class);
		this.regexExtract = field.getAnnotation(RegexExtractValue.class);
		this.mappedCollection = field.getAnnotation(MappedCollection.class);
		this.transformation = field.getAnnotation(Transformation.class);
		this.conversion = field.getAnnotation(Conversion.class);
		
		this.extractionAnnotations = new ExtractionAnnotations(
			FieldInfo.forField(field), 
			getExtractionAnnotations(field.getAnnotations()), 
			this.mappedCollection != null, 
			this.conversion != null);
	}
	
	/**
	 * Is the ExtractValue annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasExtractValue()
	{
		return this.extractValue != null || this.hasRegexExtractValue();
	}
	
	/**
	 * Is the ExtractCurrentUrl annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasCurrentUrl()
	{
		return this.currentUrl != null;
	}
	
	/**
	 * Is the ExtractFromUrl annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasExtractFromUrl()
	{
		return this.extractFromUrl != null;
	}

	/**
	 * Is the RegexExtractValue annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasRegexExtractValue()
	{
		return this.regexExtract != null;
	}
	
	/**
	 * Is the MappedCollection annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasMappedCollection()
	{
		return this.mappedCollection != null;
	}
	
	/**
	 * Is the Transformation annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasTransformation()
	{
		return this.transformation != null;
	}
	
	/**
	 * Is the Conversion annotation present.
	 * @return {code true} if the annotation is present.
	 */
	public boolean hasConversion()
	{
		return this.conversion != null;
	}
	
	/**
	 * @return the ExtractValue annotation.
	 */
	public ExtractValue getExtractValue()
	{
		if(this.extractValue != null)
		{
			return this.extractValue;
		}
		else
		{
			return this.regexExtract.extractValue();
		}
	}

	/**
	 * @return the ExtractCurrentUrl annotation.
	 */
	public ExtractCurrentUrl getCurrentUrl()
	{
		return this.currentUrl;
	}

	/**
	 * @return the ExtractFromUrl annotation.
	 */
	public ExtractFromUrl getExtractFromUrl()
	{
		return this.extractFromUrl;
	}

	/**
	 * @return the RegexExtractValue annotation.
	 */
	public RegexExtractValue getRegexExtract()
	{
		return this.regexExtract;
	}

	/**
	 * @return the MappedCollection annotation.
	 */
	public MappedCollection getMappedCollection()
	{
		return this.mappedCollection;
	}

	/**
	 * @return the Transformation annotation.
	 */
	public Transformation getTransformation()
	{
		return this.transformation;
	}

	/**
	 * @return the Conversion annotation.
	 */
	public Conversion getConversion()
	{
		return this.conversion;
	}
	
	/**
	 * validates that the combination of annotations is valid.
	 */
	public void validate()
	{
		boolean onlyOneExtractorAnnotation = Arrays.stream(
			new Object[]{this.extractValue, this.extractFromUrl, this.currentUrl, this.regexExtract})
			.filter(o -> o != null)
			.count() == 1;
		
		if(!onlyOneExtractorAnnotation)
		{
			throw new RuntimeException("Invalid combination of annotations");
		}
	}

	private List<? extends Annotation> getExtractionAnnotations(Annotation[] annotations)
	{
		return Arrays.stream(annotations).filter(a -> ExtractionAnnotations.isExtractAnnotation(a)).toList();
	}

	/**
	 * @return the extractors
	 */
	public List<IExtractor> getExtractors()
	{
		return this.extractionAnnotations.getExtractors();
	}
}
