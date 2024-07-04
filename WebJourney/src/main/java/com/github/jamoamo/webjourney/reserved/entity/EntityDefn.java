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

import com.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import java.util.Arrays;
import java.util.List;
import com.github.jamoamo.webjourney.reserved.annotation.ExtractionAnnotations;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.Failable;

/**
 * An entity defn.
 *
 * @author James Amoore
 * @param <T> The Entity class.
 */
public final class EntityDefn<T>
{
	private final Class<T> entityClass;
	private final List<EntityFieldDefn> entityFields;

	/**
	 * A new EntityDefn for the entity class.
	 *
	 * @param entityClass The entity class.
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException 
	 *		if there was an error creating the definition
	 */
	public EntityDefn(Class<T> entityClass) throws XEntityDefinitionException
	{
		this.entityClass = entityClass;
		this.entityFields = determineEntityFields();
	}

	T createInstance()
	{
		return InstanceCreator.getInstance().createInstance(this.entityClass);
	}

	List<EntityFieldDefn> getEntityFields()
	{
		return this.entityFields;
	}

	private List<EntityFieldDefn> determineEntityFields() throws XEntityDefinitionException
	{
		return Failable.stream(Arrays.stream(this.entityClass.getDeclaredFields()))
			.filter(field -> Arrays.stream(field.getAnnotations())
				.anyMatch(a -> ExtractionAnnotations.isExtractAnnotation(a)))
			.map(field -> new EntityFieldDefn(field))
			.collect(Collectors.toList());
	}

	Class<T> getFieldType()
	{
		return this.entityClass;
	}
}
