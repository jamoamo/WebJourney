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

import com.github.jamoamo.webjourney.api.entity.IEntityCreationListener;
import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
	private static final HashMap<String, Object> ENTITY_MAP = new HashMap<>();
	private static boolean cacheEnabled = true;

	private final EntityDefn<T> defn;
	private AElement element;
	private boolean useCache = false;
	private List<IEntityCreationListener> creationListeners;

	/**
	 * Creates a new instance.
	 *
	 * @param defn The entity definition to create an instance for
	 * @param useCache if the cache should be used
	 * @param creationListeners the creation listeners
	 */
	public EntityCreator(EntityDefn<T> defn, boolean useCache, List<IEntityCreationListener> creationListeners)
	{
		this.defn = defn;
		this.useCache = useCache;
		this.creationListeners = creationListeners;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param defn The entity definition to create an instance for.
	 * @param element The parent element.
	 * @param listeners the creation listeners
	 */
	public EntityCreator(EntityDefn<T> defn, AElement element, List<IEntityCreationListener> listeners)
	{
		this.defn = defn;
		this.element = element;
	}
	
	static void disableCache()
	{
		cacheEnabled = false;
	}

	/**
	 * Create a new entity.
	 *
	 * @param browser The browser to use.
	 *
	 * @return The newly created entity.
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException if a field could not be scraped.
	 */
	public T createNewEntity(IBrowser browser) throws XEntityFieldScrapeException
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
	T createNewEntity(IValueReader reader) throws XEntityFieldScrapeException
	{
		fireEntityCreationStarted();
		String entityKey = createEntityKey(reader);
		
		if(cacheEnabled && this.useCache && entityKey != null && ENTITY_MAP.containsKey(entityKey))
		{
			LOGGER.debug(String.format("Retrieving entity from cache [%s]", entityKey));
			return this.defn.getFieldType().cast(ENTITY_MAP.get(entityKey));
		}
		
		T instance = this.defn.createInstance();
		
		List<EntityFieldDefn> entityFields = this.defn.getEntityFields();
		
		for(EntityFieldDefn fieldDefn : entityFields)
		{
			LOGGER.debug("Setting field: " + fieldDefn.getFieldName());
			scrapeField(fieldDefn, instance, reader);
		}
		storeInstanceInCache(entityKey, instance);
		fireEntityCreated(instance);
		return instance;
	}

	private String createEntityKey(IValueReader reader)
	{
		if(reader.getBrowser() == null)
		{
			return null;
		}
		
		String entityKey = null;
		try
		{
			entityKey = this.defn.getFieldType().getName() + ":@:" + reader.getCurrentUrl();
		}
		catch(XValueReaderException ex)
		{
			//Ignore
		}
		return entityKey;
	}

	private void storeInstanceInCache(String entityKey, T instance)
	{
		if(cacheEnabled && this.useCache && entityKey != null)
		{
			LOGGER.debug(String.format("Storing entity to cache [%s]", entityKey));
			ENTITY_MAP.put(entityKey, instance);
		}
	}

	private void scrapeField(EntityFieldDefn defn, T instance, IValueReader reader)
			  throws XEntityFieldScrapeException
	{
		try
		{
			Object value = scrapeValue(defn, reader);
			LOGGER.debug("Set field value: " + defn.getFieldName() + " = " + 
							 (value == null ? "(null)" : value.toString()));
			BeanUtils.setProperty(instance, defn.getFieldName(), value);
		}
		catch(IllegalAccessException |
				InvocationTargetException | 
				XEntityEvaluationException ex)
		{
			throw new XEntityFieldScrapeException(ex);
		}
	}

	private Object scrapeValue(EntityFieldDefn defn1, IValueReader reader) throws XEntityEvaluationException
	{
		return defn1.getEvaluator().evaluate(reader, this.creationListeners);
	}

	private void fireEntityCreated(T instance)
	{
		if(this.creationListeners == null)
		{
			return;
		}
		this.creationListeners.forEach(l -> l.entityCreated(instance));
	}

	private void fireEntityCreationStarted()
	{
		if(this.creationListeners == null)
		{
			return;
		}
		this.creationListeners.forEach(listener -> listener.entityCreationStarted(this.defn.getFieldType()));
	}
}
