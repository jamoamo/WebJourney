/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package com.github.jamoamo.entityscraper.reserved.reflection;

import com.github.jamoamo.entityscraper.api.mapper.AValueMapper;

/**
 *
 * @author James Amoore
 */
public class MapperCreator extends InstanceCreator
{
	private static final EntityCreator INSTANCE = new EntityCreator();
	
	public static EntityCreator getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * Creates a new entity of the given entity class type.
	 * 
	 * @param entityClass The class of the entity type
	 * @return 
	 */
	public <T extends AValueMapper> T createEntity(Class<T> entityClass)
	{
		return createInstance(entityClass);
	}
}
