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

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creator of entities.
 *
 * @author James Amoore
 * @param <T> the type of the entity
 */
public final class EntityCreator<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreator.class);

	private final EntityDefn<T> defn;
	private AElement element;

	/**
	 * Creates a new instance.
	 *
	 * @param defn The entity definition to create an instance for
	 */
	public EntityCreator(EntityDefn<T> defn)
	{
		this.defn = defn;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param defn The entity definition to create an instance for.
	 * @param element The parent element.
	 */
	public EntityCreator(EntityDefn<T> defn, AElement element)
	{
		this.defn = defn;
		this.element = element;
	}

	/**
	 * Create a new entity.
	 *
	 * @param browser The browser to use.
	 *
	 * @return The newly created entity.
	 */
	public T createNewEntity(IBrowser browser)
	{
		IValueReader reader = this.element == null ? 
									 new BrowserValueReader(browser) : new ParentElementValueReader(browser, this.element);

		return createNewEntity(reader);
	}

	/**
	 * Create a new entity.
	 * 
	 * @param reader the value ready to use
	 * @return The newly created entity
	 */
	public T createNewEntity(IValueReader reader)
	{
		T instance = this.defn.createInstance();
		List<EntityFieldDefn> entityFields = this.defn.getEntityFields();
		
		for(EntityFieldDefn fieldDefn : entityFields)
		{
			LOGGER.info("Setting field: " + fieldDefn.getFieldName());
			scrapeField(fieldDefn, instance, reader);
		}

		return instance;
	}

	private void scrapeField(EntityFieldDefn defn, T instance, IValueReader reader)
			  throws RuntimeException
	{
		try
		{
			Object value = scrapeValue(defn, reader);
			BeanUtils.setProperty(instance, defn.getFieldName(), value);
		}
		catch(IllegalAccessException |
				InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private Object scrapeValue(EntityFieldDefn defn1, IValueReader reader)
	{
		IExtractor valueExtractor = Extractors.getExtractorForField(defn1, reader);
		ITransformer valueTransformer = Transformers.getTransformerForField(defn1);
		IConverter valueMapper = Mappers.getMapperForField(reader, defn1);
		
		Object value = valueExtractor.extractRawFieldValue();
		if(valueTransformer != null)
		{
			value = valueTransformer.transformValue(value);
		}

		Object mappedValue = valueMapper.mapValue(value);
		return mappedValue;
	}
}
