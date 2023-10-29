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

import com.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import com.github.jamoamo.webjourney.annotation.Mapping;
import com.github.jamoamo.webjourney.annotation.Transformation;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import java.lang.reflect.Field;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.MappedCollection;
import com.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;

/**
 * Entity Field definition.
 *
 * @author James Amoore
 */
class EntityFieldDefn
{
	private Field field;
	private Transformation transformation;
	private Mapping mapping;
	private ExtractValue extractValue;
	private ExtractFromUrl extractFromUrl;
	private ExtractCurrentUrl currentUrl;

	EntityFieldDefn(Field field)
	{
		if(field == null)
		{
			throw new RuntimeException("Invalid Field");
		}
		this.field = field;
		this.transformation = field.getAnnotation(Transformation.class);
		this.mapping = field.getAnnotation(Mapping.class);
		this.extractValue = field.getAnnotation(ExtractValue.class);
		this.extractFromUrl = field.getAnnotation(ExtractFromUrl.class);
		this.currentUrl = field.getAnnotation(ExtractCurrentUrl.class);

		validateAnnotations(this.extractValue, this.extractFromUrl, this.currentUrl);
	}

	String getFieldName()
	{
		return this.field.getName();
	}

	FieldInfo getFieldInfo()
	{
		return FieldInfo.forField(this.field);
	}

	ExtractValue getExtractValue()
	{
		return this.extractValue;
	}

	ExtractFromUrl getExtractFromUrl()
	{
		return this.extractFromUrl;
	}

	ExtractCurrentUrl getCurrentUrl()
	{
		return this.currentUrl;
	}

	Transformation getTransformation()
	{
		return this.transformation;
	}

	Class<?> getFieldType()
	{
		return this.field.getType();
	}

	Field getField()
	{
		return this.field;
	}

	Mapping getMapping()
	{
		return this.mapping;
	}

	boolean isMappedCollection()
	{
		return this.field.isAnnotationPresent(MappedCollection.class);
	}

	@Override
	public String toString()
	{
		return this.field.getName() + "[" + this.field.getGenericType().getTypeName() + "]";
	}

	private void validateAnnotations(ExtractValue extractValue, 
												ExtractFromUrl extractFromUrl,
												ExtractCurrentUrl currentUrl)
	{
		boolean valid = true;
		if(extractValue != null && (extractFromUrl != null || currentUrl != null))
		{
			valid = false;
		}

		if(valid && extractFromUrl != null && currentUrl != null)
		{
			valid = false;
		}

		if(!valid)
		{
			throw new RuntimeException("Invalid Annotation Combination: Only one of ExractValue, " +
					  "ExtractFromUrl or ExtractCurrentUrl should be used");
		}
	}
}
