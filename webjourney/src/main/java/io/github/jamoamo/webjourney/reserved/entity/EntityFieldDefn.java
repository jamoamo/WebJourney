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

import io.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import java.lang.reflect.Field;
import io.github.jamoamo.webjourney.annotation.MappedCollection;
import java.util.List;

/**
 * Entity Field definition.
 *
 * @author James Amoore
 */
class EntityFieldDefn
{
	private Field field;
	private EntityFieldEvaluator evaluator;
	private EntityAnnotations annotations;

	EntityFieldDefn(Field field) throws XEntityFieldDefinitionException
	{
		if(field == null)
		{
			throw new RuntimeException("Invalid Field");
		}
		this.field = field;
		
		this.annotations = new EntityAnnotations(field);
		this.annotations.validate();
		
		List<IExtractor> extractors = this.annotations.getExtractors();
		ITransformer transformer = Transformers.getTransformerForField(this);
		IConverter convert = Converters.getConverterForField(this);
		
		this.evaluator = new EntityFieldEvaluator(extractors, transformer, convert);
	}

	String getFieldName()
	{
		return this.field.getName();
	}

	Class<?> getFieldType()
	{
		return this.field.getType();
	}

	Field getField()
	{
		return this.field;
	}

	boolean isMappedCollection()
	{
		return this.field.isAnnotationPresent(MappedCollection.class);
	}

	public EntityFieldEvaluator getEvaluator()
	{
		return this.evaluator;
	}

	public EntityAnnotations getAnnotations()
	{
		return this.annotations;
	}

	@Override
	public String toString()
	{
		return this.field.getName() + "[" + this.field.getGenericType().getTypeName() + "]";
	}
}
