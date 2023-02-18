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

import com.github.jamoamo.entityscraper.annotation.Entity;
import com.github.jamoamo.entityscraper.annotation.XPath;
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.reserved.html.jsoup.JSoupParser;
import com.github.jamoamo.entityscraper.api.xpath.IPathEvaluator;
import com.github.jamoamo.entityscraper.api.xpath.AXPathExpression;
import com.github.jamoamo.entityscraper.api.xpath.XXPathException;
import com.github.jamoamo.entityscraper.reserved.xpath.jaxen.JaxenPathEvaluator;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.apache.commons.beanutils.BeanUtils;
import com.github.jamoamo.entityscraper.api.html.IParser;
import com.github.jamoamo.entityscraper.api.mapper.AValueMapper;
import com.github.jamoamo.entityscraper.api.mapper.XValueMappingException;
import com.github.jamoamo.entityscraper.reserved.reflection.EntityCreator;
import com.github.jamoamo.entityscraper.reserved.reflection.MapperCreator;
import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Scrapes an HTML Document and creates an entity object from it based on the annotations on the entity class.
 *
 * @author James Amoore
 * @param <T> The entity class of entities created by the scraper.
 */
public final class EntityScraper<T>
{
	private static final int DEFAULT_TIMEOUT = 10000;

	private final Class<T> entityClass;
	private IParser scraper = new JSoupParser();
	private IPathEvaluator pathEvaluator = new JaxenPathEvaluator();

	EntityScraper(Class<T> entityClass)
	{
		this.entityClass = entityClass;
	}

	void setScraper(IParser scraper)
	{
		this.scraper = scraper;
	}

	void setPathEvaluator(IPathEvaluator evaluator)
	{
		this.pathEvaluator = evaluator;
	}

	/**
	 * Scrapes an html file to produce an entity.
	 *
	 * @param file The file to scrape
	 *
	 * @return the scraped entity
	 *
	 * @throws XXPathException        if there is an xpath exception
	 * @throws XValueMappingException if there is an error creating the entity and setting its values.
	 */
	public T scrape(File file)
			  throws XXPathException, XValueMappingException
	{
		return scrape(() ->
		{
			try
			{
				return this.scraper.parse(file, Charset.forName("UTF-8"));
			}
			catch(IOException ioe)
			{
				throw new RuntimeException(ioe);
			}
		});
	}

	/**
	 * Scrapes the file at the provided URL to produce an entity.
	 *
	 * @param url The url to scrape
	 *
	 * @return the scraped entity
	 *
	 * @throws XXPathException        if there is an xpath exception
	 * @throws XValueMappingException if there is an error creating the entity and setting its values.
	 */
	public T scrape(URL url)
			  throws XXPathException, XValueMappingException
	{
		return scrape(() ->
		{
			try
			{
				return this.scraper.parse(url, DEFAULT_TIMEOUT);
			}
			catch(IOException ioe)
			{
				throw new RuntimeException(ioe);
			}
		});
	}

	private T scrape(Supplier<AHtmlDocument> documentSupplier)
			  throws XXPathException, XValueMappingException
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
		T entity = EntityCreator.getInstance()
				  .createEntity(this.entityClass);
		return entity;
	}

	private void processField(T entity, String rootXPath, AHtmlDocument document, Field field)
			  throws XXPathException, XValueMappingException
	{
		XPath xPath = field.getAnnotation(XPath.class);
		if(xPath == null)
		{
			return;
		}
		String evaluatedValue = evaluateValue(rootXPath, xPath, document);
		mapValue(xPath, evaluatedValue, field, entity);
	}

	private void mapValue(XPath xPath, String evaluatedValue, Field field, T entity)
		 throws RuntimeException, XValueMappingException
	{
		AValueMapper mapper = createMapper(xPath);
		try
		{
			Object mappedValue = mapper.mapValue(evaluatedValue, field);
			BeanUtils.setProperty(entity, field.getName(), mappedValue);
		}
		catch(IllegalAccessException | InvocationTargetException ex)
		{
			throw new RuntimeException(
				 String.format("Failed to set field %s with value %s", field.getName(), evaluatedValue),
				 ex);
		}
	}

	private String evaluateValue(String rootXPath, XPath xPath, AHtmlDocument document)
		 throws RuntimeException, XXPathException
	{
		String xPathExpression = rootXPath + xPath.path();
		AXPathExpression expression = null;
		try
		{
			expression = this.pathEvaluator.forPath(xPathExpression);
		}
		catch(XXPathException exception)
		{
			throw new RuntimeException("Error evaluating xpath.");
		}
		String evaluatedValue = expression.evaluateStringValue(document);
		return evaluatedValue;
	}

	private AValueMapper createMapper(XPath xPath)
			  throws RuntimeException
	{
		AValueMapper mapper = MapperCreator.getInstance()
				  .createMapper(xPath.mapperClass());
		return mapper;
	}
}
