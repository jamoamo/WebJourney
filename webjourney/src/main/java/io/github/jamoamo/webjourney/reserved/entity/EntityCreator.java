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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.api.entity.IEntityCreationListener;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Creator of entities.
 *
 * @author James Amoore
 * @param <T> the type of the entity
 */
public final class EntityCreator<T>
{
	private static final String LOGGING_ENTITY_FIELD_LABEL = "labels.WebJourney.scrape.entity.field";
	private static final String LOGGING_ENTITY_CLASS_LABEL = "labels.WebJourney.scrape.entity.class";
	private static final String LOGGING_ENTITY_PATH_LABEL = "labels.WebJourney.scrape.entity.path";
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreator.class);
	private static final HashMap<String, Object> ENTITY_MAP = new HashMap<>();
	private static boolean cacheEnabled = true;

	private EntityCreationContext context;
	private final EntityDefn<T> defn;
	private AElement element;
	private boolean useCache = false;
	private List<IEntityCreationListener> creationListeners;

	/**
	 * Creates a new instance.
	 *
	 * @param defn              The entity definition to create an instance for
	 * @param useCache          if the cache should be used
	 * @param creationListeners the creation listeners
	 */
	public EntityCreator(
		EntityDefn<T> defn,
		boolean useCache,
		List<IEntityCreationListener> creationListeners)
	{
		this.defn = defn;
		this.useCache = useCache;
		this.creationListeners = creationListeners;

	}

	/**
	 * Creates a new instance.
	 *
	 * @param defn      The entity definition to create an instance for.
	 * @param element   The parent element.
	 * @param listeners the creation listeners
	 */
	public EntityCreator(
		EntityDefn<T> defn,
		AElement element,
		List<IEntityCreationListener> listeners)
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
	 *
	 * @throws io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException if a field could
	 *                                                                                     not be
	 *                                                                                     scraped.
	 */
	public T createNewEntity(IBrowser browser) throws XEntityFieldScrapeException
	{
		return this.createNewEntity(browser, null);
	}

	/**
	 * Create a new entity.
	 *
	 * @param browser The browser to use.
	 * @param context The entity creation context if an entity creation is already in progress.
	 *
	 * @return The newly created entity.
	 *
	 * @throws io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException if a field could
	 *                                                                                     not be
	 *                                                                                     scraped.
	 */
	public T createNewEntity(IBrowser browser, EntityCreationContext context) throws XEntityFieldScrapeException
	{
		MDC.put(LOGGING_ENTITY_CLASS_LABEL, this.defn.getFieldType().getCanonicalName());
		this.context = context == null ? new EntityCreationContext(
			this.defn) : context;
		T result = null;
		try
		{
			IValueReader reader = this.element == null ? new BrowserValueReader(
				browser)
				: new ParentElementValueReader(
					browser,
					this.element);
			result = createNewEntity(reader);
		}
		finally
		{
			MDC.remove(LOGGING_ENTITY_CLASS_LABEL);
			this.context = null;
		}
		return result;
	}

	/**
	 * Create a new entity.
	 *
	 * @param reader the value ready to use
	 *
	 * @return The newly created entity
	 */
	T createNewEntity(IValueReader reader) throws XEntityFieldScrapeException
	{
		MDC.put(LOGGING_ENTITY_CLASS_LABEL, this.defn.getFieldType().getCanonicalName());
		fireEntityCreationStarted();
		LOGGER.debug("Creating new entity of type " + this.defn.getFieldType().getCanonicalName());
		T instance = null;
		try
		{
			String entityKey = createEntityKey(reader);
			if (cacheEnabled && this.useCache && entityKey != null && ENTITY_MAP.containsKey(entityKey))
			{
				LOGGER.debug(String.format("Retrieving entity from cache [%s]", entityKey));
				return this.defn.getFieldType().cast(ENTITY_MAP.get(entityKey));
			}
			LOGGER.debug("Creating new entity of type " + this.defn.getFieldType().getCanonicalName());
			instance = this.defn.createInstance();
			for (EntityFieldDefn fieldDefn : this.defn.getEntityFields())
			{
				LOGGER.debug("Processing field: " + fieldDefn.getFieldName());
				processFieldDefn(fieldDefn, instance, reader);
				LOGGER.debug("Processed field: " + fieldDefn.getFieldName());
			}
			storeInstanceInCache(entityKey, instance);
			fireEntityCreated(instance);
		}
		finally
		{
			MDC.remove(LOGGING_ENTITY_CLASS_LABEL);
		}
		LOGGER.debug("Created new entity of type " + this.defn.getFieldType().getCanonicalName());
		return instance;
	}

	private void processFieldDefn(EntityFieldDefn fieldDefn, T instance, IValueReader reader)
		throws IllegalArgumentException,
		XEntityFieldScrapeException
	{
		try
		{
			this.context.processField(fieldDefn);
			MDC.put(LOGGING_ENTITY_FIELD_LABEL,
				this.defn.getFieldType().getCanonicalName() + "[" + fieldDefn.getFieldName() + "]");
			LOGGER.debug("Setting field: " + fieldDefn.getFieldName());
			scrapeField(fieldDefn, instance, reader);
		}
		finally
		{
			MDC.remove(LOGGING_ENTITY_FIELD_LABEL);
			this.context.fieldProcessComplete();
		}
	}

	private String createEntityKey(IValueReader reader)
	{
		if (reader.getBrowser() == null)
		{
			return null;
		}

		String entityKey = null;
		try
		{
			entityKey = this.defn.getFieldType().getName() + ":@:" + reader.getCurrentUrl();
		}
		catch (XValueReaderException ex)
		{
			// Ignore
		}
		return entityKey;
	}

	private void storeInstanceInCache(String entityKey, T instance)
	{
		if (cacheEnabled && this.useCache && entityKey != null)
		{
			LOGGER.debug(String.format("Storing entity to cache [%s]", entityKey));
			ENTITY_MAP.put(entityKey, instance);
		}
	}

	private void scrapeField(EntityFieldDefn defn, T instance, IValueReader reader) throws XEntityFieldScrapeException
	{
		try
		{
			Object value = scrapeValue(defn, reader);
			LOGGER
				.debug("Set field value: " + defn.getFieldName() + " = " + (value == null ? "(null)" : value.toString()));
			ConvertUtils.register(new IntegerConverter(
				null), Integer.class);
			ConvertUtils.register(new LongConverter(
				null), Long.class);
			ConvertUtils.register(new DoubleConverter(
				null), Double.class);
			LOGGER
				.debug("Setting property: " + defn.getFieldName() + " = " + (value == null ? "(null)" : value.toString()));
			BeanUtils.setProperty(instance, defn.getFieldName(), value);
		}
		catch (IllegalAccessException | InvocationTargetException ex)
		{
			throw new XEntityFieldScrapeException(
				ex);
		}
		catch (XEntityEvaluationException ex)
		{
			LOGGER.atError()
				.setMessage("Failure scraping field")
				.setCause(ex)
				.addKeyValue(LOGGING_ENTITY_PATH_LABEL, this.context.getContext())
				.addKeyValue("error.message", ex.getMessage())
				.log();
			throw new XEntityFieldScrapeException(
				ex);
		}
	}

	private Object scrapeValue(EntityFieldDefn defn1, IValueReader reader) throws XEntityEvaluationException
	{
		LOGGER.debug("Scraping value for entity field: {}", defn1.getFieldName());
		return defn1.getEvaluator().evaluate(reader, this.creationListeners, this.context);
	}

	private void fireEntityCreated(T instance)
	{
		if (this.creationListeners == null)
		{
			return;
		}
		this.creationListeners.forEach(l -> l.entityCreated(instance));
	}

	private void fireEntityCreationStarted()
	{
		if (this.creationListeners == null)
		{
			return;
		}
		this.creationListeners.forEach(listener -> listener.entityCreationStarted(this.defn.getFieldType()));
	}

}
