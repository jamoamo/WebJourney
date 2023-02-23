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

import com.github.jamoamo.entityscraper.annotation.Collection;
import com.github.jamoamo.entityscraper.annotation.Entity;
import com.github.jamoamo.entityscraper.annotation.Transformation;
import com.github.jamoamo.entityscraper.annotation.XPath;
import com.github.jamoamo.entityscraper.api.function.ATransformationFunction;
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
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
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
			EntityFieldDefn defn = new EntityFieldDefn(field);
			if(defn.valid())
			{
				processField(entity, rootXPath, document, defn);
			}
		}

		return entity;
	}

	private T createInstance()
	{
		T entity = EntityCreator.getInstance()
			 .createEntity(this.entityClass);
		return entity;
	}

	private void processField(T entity, String rootXPath, AHtmlDocument document, EntityFieldDefn defn)
		 throws XXPathException, XValueMappingException
	{
		Field field = defn.getField();
		if(defn.isSingleValue())
		{
			String evaluatedValue = evaluateValue(rootXPath, defn.getXpath(), document);
			mapStringValue(entity, defn, evaluatedValue);
		}
		else
		{
			List<String> evaluatedValues = evaluateCollectionValue(rootXPath, defn.getCollection(), document);
			mapListValue(entity, defn, evaluatedValues);
		}
	}

	private void mapListValue(T entity, EntityFieldDefn defn, List<String> evaluatedValues)
		 throws XValueMappingException
	{
		AValueMapper mapper = createMapper(defn.getCollection().mapperClass());
		try
		{
			List<Object> objects = new ArrayList();
			for(String evaluatedValue : evaluatedValues)
			{
				Class<?> genericType = defn.getMultiType();
				Object mappedValue =
					 mapAndTransformValue(mapper, evaluatedValue, genericType, defn.getTransformation());
				objects.add(mappedValue);
			}
			BeanUtils.setProperty(entity, defn.getFieldName(), objects);
		}
		catch(IllegalAccessException
			 | InvocationTargetException ex)
		{
			throwFieldSetError(defn.getFieldName(), evaluatedValues, ex);
		}
	}

	private void throwFieldSetError(String fieldName, Object evaluatedValues,
		 Exception ex)
		 throws RuntimeException
	{
		throw new RuntimeException(
			 String.format("Failed to set field %s with value %s", fieldName, evaluatedValues),
			 ex);
	}

	private void mapStringValue(T entity, EntityFieldDefn defn, String evaluatedValue)
		 throws XValueMappingException
	{
		AValueMapper mapper = createMapper(defn.getXpath().mapperClass());
		try
		{
			Object mappedValue = mapAndTransformValue(mapper, evaluatedValue, defn.getType(), defn.getTransformation());
			BeanUtils.setProperty(entity, defn.getFieldName(), mappedValue);
		}
		catch(IllegalAccessException
			 | InvocationTargetException ex)
		{
			throwFieldSetError(defn.getFieldName(), evaluatedValue, ex);
		}
	}

	private Object mapAndTransformValue(AValueMapper mapper, String evaluatedValue, Class<?> fieldClass,
		 Transformation transformation)
		 throws XValueMappingException
	{
		if(transformation != null)
		{
			try
			{
				Method method = transformation.functionClass()
					 .getMethod("transform", new Class[]{String.class});
				ATransformationFunction func = transformation.functionClass()
					 .getConstructor(new Class[]{})
					 .newInstance(new Object[]{});
				evaluatedValue = method.invoke(func, evaluatedValue)
					 .toString();
			}
			catch(NoSuchMethodException
				 | InstantiationException
				 | IllegalAccessException
				 | InvocationTargetException ex)
			{
				throw new RuntimeException("Failed to transform value.", ex);
			}
		}

		Object mappedValue = mapper.mapValue(evaluatedValue, fieldClass);
		return mappedValue;
	}

	private List<String> evaluateCollectionValue(String root, Collection collection, AHtmlDocument document)
		 throws XXPathException
	{
		String xPathExpression = root + collection.path();
		AXPathExpression expression = null;
		try
		{
			expression = this.pathEvaluator.forPath(xPathExpression);
		}
		catch(XXPathException exception)
		{
			throw new RuntimeException("Invalid xpath for list.");
		}
		List<String> evaluatedValue = expression.evaluateListValue(document);
		return evaluatedValue;
	}

	private String evaluateValue(String rootXPath, XPath xPath, AHtmlDocument document)
		 throws XXPathException
	{
		String xPathExpression = rootXPath + xPath.path();
		AXPathExpression expression = null;
		try
		{
			expression = this.pathEvaluator.forPath(xPathExpression);
		}
		catch(XXPathException exception)
		{
			throw new RuntimeException("Invalid xpath.");
		}
		String evaluatedValue = expression.evaluateStringValue(document);
		return evaluatedValue;
	}

	private AValueMapper createMapper(Class xPathClass)
		 throws RuntimeException
	{
		AValueMapper mapper = MapperCreator.getInstance()
			 .createMapper(xPathClass);
		return mapper;
	}

}
