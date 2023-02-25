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

import com.github.jamoamo.entityscraper.api.xpath.IPathEvaluator;
import com.github.jamoamo.entityscraper.reserved.html.jsoup.JSoupParser;
import com.github.jamoamo.entityscraper.reserved.xpath.jaxen.JaxenPathEvaluator;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author James Amoore
 */
public class EntityScraperBuilderBaseTest
{
	
	public EntityScraperBuilderBaseTest()
	{
	}

	/**
	 * Test of usingParser method, of class EntityScraperBuilderBase.
	 */
	@Test
	public void testUsingParser_null() throws Exception
	{
		EntityScraperBuilderBase instance = new EntityScraperBuilderBase(createContext());
		Exception ex = assertThrows(IllegalArgumentException.class, () -> instance.usingParser(null));
		assertEquals("Parser may not be null!", ex.getMessage());
	}
	
	@Test
	public void testUsingParser() throws Exception
	{
		EntityScraperBuilderBase instance = new EntityScraperBuilderBase(createContext());
		JSoupParser parser = new JSoupParser();
		instance.usingParser(parser);
		Field contextField = instance.getClass().getDeclaredField("context");
		contextField.setAccessible(true);
		EntityScrapeContext context = (EntityScrapeContext)contextField.get(instance);
		assertEquals(parser, context.getParser());
	}

	/**
	 * Test of usingPathEvaluator method, of class EntityScraperBuilderBase.
	 */
	@Test
	public void testUsingPathEvaluator_null()
	{
		EntityScraperBuilderBase instance = new EntityScraperBuilderBase(createContext());
		Exception ex = assertThrows(IllegalArgumentException.class, () -> instance.usingPathEvaluator(null));
		assertEquals("Evaluator may not be null!", ex.getMessage());
	}
	
	@Test
	public void testUsingPathEvaluator() throws Exception
	{
		EntityScraperBuilderBase instance = new EntityScraperBuilderBase(createContext());
		JaxenPathEvaluator evaluator = new JaxenPathEvaluator();
		instance.usingPathEvaluator(evaluator);
		Field contextField = instance.getClass().getDeclaredField("context");
		contextField.setAccessible(true);
		EntityScrapeContext context = (EntityScrapeContext)contextField.get(instance);
		assertEquals(evaluator, context.getEvaluator());
	}

	/**
	 * Test of build method, of class EntityScraperBuilderBase.
	 */
	@Test
	public void testBuild() throws Exception
	{
		EntityScraperBuilderBase instance = new EntityScraperBuilderBase(createContext());
		EntityScraper scraper = instance.build();
		
		Field contextField = scraper.getClass().getDeclaredField("context");
		contextField.setAccessible(true);
		EntityScrapeContext context = (EntityScrapeContext)contextField.get(scraper);
		assertEquals(TestEntity.class, context.getEntityClass());
		assertEquals(JSoupParser.class, context.getParser().getClass());
		assertEquals(JaxenPathEvaluator.class, context.getEvaluator().getClass());
	}

	private EntityScrapeContext createContext()
	{
		return new EntityScrapeContext(TestEntity.class);
	}
	
}
