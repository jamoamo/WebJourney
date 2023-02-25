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
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.xpath.XXPathException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.apache.commons.beanutils.BeanUtils;
import com.github.jamoamo.entityscraper.api.mapper.XValueMappingException;
import com.github.jamoamo.entityscraper.reserved.reflection.EntityCreator;
import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Scrapes an HTML Document and creates an entity object from it based on the annotations on the entity class.
 *
 * @author James Amoore
 */
public final class EntityScraper
{
	private static final int DEFAULT_TIMEOUT = 10000;
	
	private final EntityScrapeContext context;

	EntityScraper(EntityScrapeContext context)
	{
		this.context = context;
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
	public Object scrape(File file)
		 throws XXPathException, XValueMappingException
	{
		return scrape(() ->
		{
			try
			{
				return this.context.getParser().parse(file, Charset.forName("UTF-8"));
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
	public Object scrape(URL url)
		 throws XXPathException, XValueMappingException
	{
		return scrape(() ->
		{
			try
			{
				return this.context.getParser().parse(url, DEFAULT_TIMEOUT);
			}
			catch(IOException ioe)
			{
				throw new RuntimeException(ioe);
			}
		});
	}

	private Object scrape(Supplier<AHtmlDocument> documentSupplier)
		 throws XXPathException, XValueMappingException
	{
		Object entity = createInstance();

		AHtmlDocument document = documentSupplier.get();
		Class<?> clazz = this.context.getEntityClass();
		Entity entityAnnotation = clazz.getAnnotation(Entity.class);
		if(entityAnnotation == null)
		{
			throw new RuntimeException("Class is not annotated with the @Entity annotation.");
		}

		for(Field field : this.context.getEntityClass().getDeclaredFields())
		{
			EntityFieldDefn defn = new EntityFieldDefn(field);
			if(defn.valid())
			{
				processField(entity, this.context, document, defn);
			}
		}

		return entity;
	}

	private Object createInstance()
	{
		Object entity = EntityCreator.getInstance()
			 .createEntity(this.context.getEntityClass());
		return entity;
	}

	private void processField(Object entity, EntityScrapeContext context, AHtmlDocument document, EntityFieldDefn defn)
		 throws XXPathException, XValueMappingException
	{
		AFieldScraper scraper = getScraper(defn);
		if(scraper == null)
		{
			return;
		}
		Object mappedValue = scraper.scrapeValue(document, context, defn);
		
		try
		{
			BeanUtils.setProperty(entity, defn.getFieldName(), mappedValue);
		}
		catch(IllegalAccessException | InvocationTargetException ex)
		{
			throw new RuntimeException("Failed to set scraped value on entity.", ex);
		}
	}
	
	private AFieldScraper getScraper(EntityFieldDefn defn)
	{
		if(defn.isSingleValue())
		{
			return new StringValueScraper();
		}
		else if (defn.isMultipleValue())
		{
			return new CollectionScraper();
		}
		return null;
	}
}
