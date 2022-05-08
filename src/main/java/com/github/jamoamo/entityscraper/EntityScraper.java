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
package com.github.jamoamo.entityscraper;

import com.github.jamoamo.entityscraper.annotation.Entity;
import com.github.jamoamo.entityscraper.annotation.XPath;
import com.github.jamoamo.entityscraper.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.html.jsoup.JSoupParser;
import com.github.jamoamo.entityscraper.xpath.APathEvaluator;
import com.github.jamoamo.entityscraper.xpath.XPathExpression;
import com.github.jamoamo.entityscraper.xpath.XXPathException;
import com.github.jamoamo.entityscraper.xpath.jaxen.JaxenPathEvaluator;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.apache.commons.beanutils.BeanUtils;
import com.github.jamoamo.entityscraper.html.IParser;
import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 *
 * @author James Amoore
 * @param <T> The entity class of entities created by the scraper.
 */
public final class EntityScraper<T>
{
	private final Class<T> entityClass;
	private IParser scraper = new JSoupParser();
	private APathEvaluator pathEvaluator = new JaxenPathEvaluator();

	EntityScraper(Class<T> entityClass)
	{
		this.entityClass = entityClass;
	}

	final void setScraper(IParser scraper)
	{
		this.scraper = scraper;
	}

	final void setPathEvaluator(APathEvaluator evaluator)
	{
		this.pathEvaluator = evaluator;
	}
		
	public T scrape(File file)
			  throws XXPathException
	{
		return scrape(() -> {
			try
			{
				return this.scraper.parse(file, Charset.forName("UTF-8"));
			}
			catch(IOException ioe)
			{
				throw new RuntimeException(ioe);
			}});
	}

	public T scrape(URL url)
			  throws XXPathException
	{
		return scrape(() -> {
			try
			{
				return this.scraper.parse(url, 10000);
			}
			catch(IOException ioe)
			{
				throw new RuntimeException(ioe);
			}});
	}
	
	private T scrape(Supplier<AHtmlDocument> documentSupplier)
			  throws XXPathException
	{
		T entity = createInstance();
		
		AHtmlDocument document = documentSupplier.get();
		
		Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
		if(entityAnnotation == null)
		{
			throw new RuntimeException("Class is not annotated with the @Entity annotation.");
		}
		String rootXPath = entityAnnotation.rootPath();
		
		for(Field field : this.entityClass.getDeclaredFields())
		{
			processField(entity, rootXPath, document, field);
		}
		
		return entity;
	}

	private T createInstance()
	{
		T entity = null;
		try
		{
			entity = this.entityClass
					  .getConstructor(new Class<?>[]{})
					  .newInstance(new Object[]{});
		}
		catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex)
		{
			throw new RuntimeException(
					  String.format("Failed to create an instance of type: %s", 
										 entityClass.getSimpleName())
					  , ex);
		}
		return entity;
	}

	private void processField(T entity, String rootXPath, AHtmlDocument document, Field field)
			  throws XXPathException, RuntimeException
	{
		XPath xPath = field.getAnnotation(XPath.class);
		if(xPath == null)
		{
			return;
		}
		String xPathExpression = rootXPath + xPath.path();
		XPathExpression expression = this.pathEvaluator.forPath(xPathExpression);
		String value = expression.evaluateStringValue(document);
		
		try
		{
			BeanUtils.setProperty(entity, field.getName(), value);
		}
		catch(IllegalAccessException | InvocationTargetException ex)
		{
			throw new RuntimeException(
					  String.format("Failed to set field %s with value %s", field.getName(), value),
					  ex);
		}
	}
}
