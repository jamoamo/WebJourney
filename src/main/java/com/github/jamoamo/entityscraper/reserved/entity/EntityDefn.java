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
package com.github.jamoamo.entityscraper.reserved.entity;

import com.github.jamoamo.entityscraper.annotation.EntityField;
import com.github.jamoamo.entityscraper.reserved.reflection.InstanceCreator;
import java.util.Arrays;
import java.util.List;

/**
 * An entity defn.
 * @author James Amoore
 * @param <T> The Entity class.
 */
public final class EntityDefn<T>
{
	private final Class<T> entityClass;
	private final T entityInstance;
	private final List<EntityFieldDefn> entityFields;
	
	/**
	 * A new EntityDefn for the entity class.
	 * @param entityClass The entity class.
	 */
	public EntityDefn(Class<T> entityClass)
	{
		this.entityClass = entityClass;
		this.entityInstance = InstanceCreator.getInstance().createInstance(this.entityClass);
		this.entityFields = determineEntityFields();
	}
	
	T getInstance()
	{
		return this.entityInstance;
	}
	
	List<EntityFieldDefn> getEntityFields()
	{
		return this.entityFields;
	}

	private List<EntityFieldDefn> determineEntityFields()
	{
		return Arrays.stream(this.entityClass.getDeclaredFields())
			 .filter(field -> field.isAnnotationPresent(EntityField.class))
			 .map(field -> new EntityFieldDefn(field)).toList();
	}
}
