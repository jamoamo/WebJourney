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
package com.github.jamoamo.entityscraper.api;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class EntityScraperBuilderTest
{
	public EntityScraperBuilderTest()
	{
	}

	/**
	 * Test of forEntity method, of class EntityScraperBuilder.
	 */
	@Test
	public void testForEntity_nullEntity()
	{
		Exception ex = assertThrows(IllegalArgumentException.class, () -> EntityScraperBuilder.forEntity(null));
		assertEquals("Entity Class may not be null!", ex.getMessage());
	}
	
	@Test
	public void testForEntity() throws Exception
	{
		EntityScraperBuilderBase baseBuilder = EntityScraperBuilder.forEntity(TestEntity.class);
		Field contextField = baseBuilder.getClass().getDeclaredField("context");
		contextField.setAccessible(true);
		EntityScrapeContext context = (EntityScrapeContext)contextField.get(baseBuilder);
		assertEquals(TestEntity.class, context.getEntityClass());
	}
}
