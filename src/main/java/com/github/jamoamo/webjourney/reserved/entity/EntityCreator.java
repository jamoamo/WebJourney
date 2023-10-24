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

import com.github.jamoamo.webjourney.api.mapper.AValueMapper;
import com.github.jamoamo.webjourney.api.mapper.DoubleMapper;
import com.github.jamoamo.webjourney.api.mapper.IntegerMapper;
import com.github.jamoamo.webjourney.api.mapper.StringMapper;
import com.github.jamoamo.webjourney.api.mapper.XValueMappingException;
import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.IWebExtractor;
import com.github.jamoamo.webjourney.api.web.WebExtractor;
import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import com.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
	 * Create a new entity.
	 *
	 * @param browser The browser to use.
	 *
	 * @return The newly created entity.
	 */
	public T createNewEntity(IBrowser browser)
	{
		T instance = this.defn.getInstance();
		List<EntityFieldDefn> entityFields = this.defn.getEntityFields();

		for(EntityFieldDefn fieldDefn : entityFields)
		{
			LOGGER.info("Setting field: " + fieldDefn.getFieldName());
			scrapeField(fieldDefn, instance, browser);
		}

		return instance;
	}

	private void scrapeField(EntityFieldDefn defn, T instance, IBrowser browser)
			  throws RuntimeException
	{
		IWebExtractor extractor = new WebExtractor(browser);
		try
		{
			Field field = defn.getField();
			Class<?> defnClass = field.getType();
			Object value = scrapeValue(defnClass, extractor, defn, field);
			BeanUtils.setProperty(instance, defn.getFieldName(), value);
		}
		catch(IllegalAccessException |
				InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private Object scrapeValue(Class<?> defnClass, IWebExtractor extractor, EntityFieldDefn defn1, Field field)
	{
		if(defn1.getExtractValue().attribute().isBlank())
		{
			return scrapeElement(defnClass, extractor, defn1, field);
		}
		else
		{
			return scrapeAttribute(defnClass, extractor, defn1, field);
		}
	}

	private Object scrapeAttribute(Class<?> defnClass, IWebExtractor extractor, EntityFieldDefn defn1, Field field)
	{
		Object value = null;
		TypeInfo info = TypeInfo.forClass(defnClass);
		if(info.isStringType())
		{
			value = extractor.extractAttribute(defn1.getExtractValue().path(), defn1.getExtractValue().attribute(),
														  strVal -> transformAndMap(defn1, strVal));
		}
		else if(info.isStandardType())
		{
			throw new RuntimeException("Cannot extract attribute to a standard type");
		}
		else if(info.isCollectionType())
		{
			throw new RuntimeException("Cannot extract attribute to a collection type");
		}
		else if(info.hasNoArgsConstructor())
		{
			throw new RuntimeException("Cannot extract attribute to a non-string type");
		}
		return value;
	}

	private Object scrapeElement(
			  Class<?> defnClass, IWebExtractor extractor, EntityFieldDefn defn1, Field field)
	{
		Object value = null;
		TypeInfo info = TypeInfo.forClass(defnClass);
		if(info.isStandardType())
		{
			value = extractor.extractValue(defn1.getExtractValue().path(), strVal -> transformAndMap(defn1, strVal));
		}
		else if(info.isCollectionType())
		{
			value = scrapeCollection(defn1, extractor, field, defnClass);
		}
		else if(info.hasNoArgsConstructor())
		{
			value = scrapeEntity(extractor, defn1);
		}
		return value;
	}

	private Object scrapeCollection(EntityFieldDefn defn1, IWebExtractor extractor, Field field,
											  Class<?> defnClass)
	{
		Object value;
		if(defn1.isMappedCollection())
		{
			value = extractor.extractValue(defn1.getExtractValue().path(), strVal -> transformAndMap(defn1, strVal));
		}
		else if(isCollectionTypeStandard(field))
		{
			value = extractCollection(defnClass, extractor, defn1);
		}
		else
		{
			value = scrapeEntityCollection(extractor, defn1);
		}
		return value;
	}

	private Object extractCollection(
			  Class<?> defnClass, IWebExtractor extractor, EntityFieldDefn defn1)
	{
		Object value;
		Collection coll = InstanceCreator.getInstance().createCollectionInstance(defnClass);
		List values = extractor.extractValues(defn1.getExtractValue().path(),
														  strVal -> transformAndMap(defn1, strVal));
		coll.addAll(values);
		value = coll;
		return value;
	}

	private Object scrapeEntity(IWebExtractor extractor, EntityFieldDefn defn1)
	{
		Object value;
		Class<?> defnCLass = defn1.getFieldType();
		value =
				  extractor.extractEntity(defn1.getExtractValue()
							 .path(),
												  we -> extractEntityFromElement(defnCLass, we, extractor));
		return value;
	}

	private Object scrapeEntityCollection(IWebExtractor extractor, EntityFieldDefn defn1)
	{
		Collection coll = InstanceCreator.getInstance().createCollectionInstance(defn1.getFieldType());
		Class<?> collectionType = FieldInfo.forField(defn1.getField()).getFieldGenericType();
		List values =
				  extractor.extractEntities(defn1.getExtractValue()
							 .path(),
													 we -> extractEntityFromElement(collectionType, we, extractor));
		coll.addAll(values);
		return coll;
	}

	private Object transformAndMap(EntityFieldDefn fieldDefinition, String value)
	{
		try
		{
			if(fieldDefinition.getTransformation() != null)
			{
				value = fieldDefinition.getTransformation().transform(value);
			}
			Object mappedValue = getMapper(fieldDefinition).mapValue(value);
			return mappedValue;
		}
		catch(XValueMappingException ex)
		{
			throw new RuntimeException("Error mapping element value: " + value + " for field " + fieldDefinition);
		}
	}

	AValueMapper getMapper(EntityFieldDefn fieldDefinition)
	{
		AValueMapper mapper = null;
		if(fieldDefinition.getMapping() != null)
		{
			mapper = InstanceCreator.getInstance().createInstance(fieldDefinition.getMapping().mapper());
		}

		if(mapper == null)
		{
			mapper = determineMapper(fieldDefinition.getField());
		}
		return mapper;
	}

	private AValueMapper determineMapper(
			  Field field)
	{
		TypeInfo type = TypeInfo.forClass(field.getType());
		if(type.implementsInterface(Collection.class))
		{
			FieldInfo fieldInfo = FieldInfo.forField(field);
			return determineMapper(fieldInfo.getFieldGenericType());
		}
		else
		{
			return determineMapper(field.getType());
		}
	}

	private AValueMapper determineMapper(
			  Class<?> type)
	{
		TypeInfo info = TypeInfo.forClass(type);
		if(info.isStringType())
		{
			return new StringMapper();
		}
		else if(info.isInteger())
		{
			return new IntegerMapper();
		}
		else if(info.isDouble())
		{
			return new DoubleMapper();
		}
		else
		{
			throw new RuntimeException("Cannot determine mapper for " + type.getCanonicalName());
		}
	}

	private boolean isCollectionTypeStandard(Field field)
	{
		FieldInfo fieldInfo = FieldInfo.forField(field);
		TypeInfo typeInfo = TypeInfo.forClass(field.getType());
		if(typeInfo.isCollectionType())
		{
			Class<?> genType = fieldInfo.getFieldGenericType();
			return TypeInfo.forClass(genType).isStandardType();
		}
		else if(typeInfo.isArrayType())
		{
			TypeInfo arrayTypeInfo = TypeInfo.forClass(field.getType().arrayType());
			return arrayTypeInfo.isStandardType();
		}
		return false;
	}

	private Object extractEntityFromElement(Class<?> defn1, AElement elem, IWebExtractor extractor)
	{
		EntityDefn typeDefn = new EntityDefn(defn1);
		Object instance = typeDefn.getInstance();
		List<EntityFieldDefn> entityFields = typeDefn.getEntityFields();
		for(EntityFieldDefn field : entityFields)
		{
			Object value = extractField(elem, field, extractor);
			try
			{
				BeanUtils.setProperty(instance, field.getFieldName(), value);
			}
			catch(IllegalAccessException |
					InvocationTargetException ex)
			{
				throw new RuntimeException(
						  "Couldn't set property [" + field.getFieldName() + "] on instance of " + instance.getClass());
			}
		}
		return instance;
	}

	private Object extractField(AElement elem, EntityFieldDefn field, IWebExtractor extractor)
			  throws RuntimeException
	{
		AElement fieldElem = elem.findElement(field.getExtractValue().path());
		Object value = null;
		TypeInfo typeInfo = TypeInfo.forClass(field.getFieldType());
		if(typeInfo.isStandardType())
		{
			value = transformAndMap(field, fieldElem.getElementText());
		}
		else if(typeInfo.isCollectionType() && isCollectionTypeStandard(field.getField()))
		{
			value = elem.findElements(field.getExtractValue().path())
					  .stream().map(e -> transformAndMap(field, e.getElementText())).toList();
		}
		else if(typeInfo.isCollectionType())
		{
			List<? extends AElement> elems = elem.findElements(field.getExtractValue().path());
			value = elems.stream().map(e ->
					  extractEntityFromElement(
								 FieldInfo.forField(field.getField()).getFieldGenericType(), e, extractor)).toList();
		}
		else if(typeInfo.hasNoArgsConstructor())
		{
			value = extractEntityFromElement(field.getFieldType(),
														elem.findElement(field.getExtractValue().path()), extractor);
		}

		return value;
	}
}
