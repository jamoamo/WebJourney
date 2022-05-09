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
package com.github.jamoamo.entityscraper.api;

/**
 * Builder for building Entity Scrapers for a provided entity class
 * 
 * @author James Amoore
 */
public class EntityScraperBuilder
{
	//Private constructor to prevent instances of this class being created.
	private EntityScraperBuilder(){}
	
	/**
	 * Create an {@link EntityScraper} builder for instances of the provided class. The entity class should be annotated with {@code @Entity}.
	 * 
	 * @param entityClass The class that the EntityScraper should create instances of.
	 * @return a builder object for building instances of {@link EntityScraper}
	 */
	public static EntityScraperBuilderBase forEntity(Class<?> entityClass)
	{
		return new EntityScraperBuilderBase(entityClass);
	}
}
