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

import com.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class EntitiesCreatorConverter implements IConverter<List<String>, List<Object>>
{
	private Logger logger = LoggerFactory.getLogger(EntityCreatorConverter.class);
	private final EntityCreator entityCreator;

	EntitiesCreatorConverter(EntityFieldDefn fieldDefn)
		throws XEntityFieldDefinitionException
	{
		try
		{
			EntityDefn defn = new EntityDefn(FieldInfo.forField(fieldDefn.getField()).getFieldGenericType());
			this.entityCreator = new EntityCreator(defn);
		}
		catch(XEntityDefinitionException e)
		{
			throw new XEntityFieldDefinitionException(e);
		}
	}

	@Override
	public List<Object> convertValue(List<String> source, IValueReader reader)
		throws XConversionException
	{
		if(source == null)
		{
			return null;
		}
		List<Object> objects = new ArrayList<>();
		for(String s : source)
		{
			objects.add(createEntity(s, reader));
		}

		return objects;
	}

	private Object createEntity(String source, IValueReader reader)
		throws XConversionException
	{
		if(source == null)
		{
			return null;
		}
		try
		{
			URI uri = new URI(source);
			reader.navigateTo(uri.toURL());

			Object instance = this.entityCreator.createNewEntity(reader.getBrowser());

			reader.navigateBack();
			return instance;
		}
		catch(MalformedURLException | URISyntaxException | IllegalArgumentException | XValueReaderException |
				XEntityFieldScrapeException ex)
		{
			throw new XConversionException(ex);
		}
	}

}
