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

import com.github.jamoamo.webjourney.annotation.Mapping;
import com.github.jamoamo.webjourney.annotation.Transformation;
import com.github.jamoamo.webjourney.api.transform.ATransformationFunction;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import java.lang.reflect.Field;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.MappedCollection;

/**
 * Entity Field definition.
 * @author James Amoore
 */
class EntityFieldDefn
{
	private Field field;
	private Transformation transformation;
	private Mapping mapping;
	private ExtractValue extractValue;

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

	ATransformationFunction getTransformation()
	{
		if(this.transformation != null)
		{
			return InstanceCreator.getInstance().createInstance(this.transformation.transformFunction());
		}
		return null;
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
}
