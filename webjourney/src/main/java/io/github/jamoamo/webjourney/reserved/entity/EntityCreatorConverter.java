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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class EntityCreatorConverter
	 implements IConverter<String, Object>
{
	 private Logger logger = LoggerFactory.getLogger(EntityCreatorConverter.class);
	 private final EntityCreator entityCreator;

	 EntityCreatorConverter(EntityFieldDefn fieldDefn)
		  throws XEntityFieldDefinitionException
	 {
		  try
		  {
				EntityDefn defn = new EntityDefn(fieldDefn.getFieldType());
				this.entityCreator = new EntityCreator(defn, true, null);
		  }
		  catch(XEntityDefinitionException e)
		  {
				throw new XEntityFieldDefinitionException(e);
		  }
	 }

	 @Override
	 public Object convertValue(String source,
		  IValueReader reader,
		  List<IEntityCreationListener> listeners,
		  EntityCreationContext context)
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

				Object instance = this.entityCreator.createNewEntity(reader.getBrowser(), context);

				reader.navigateBack();
				return instance;
		  }
		  catch(MalformedURLException | URISyntaxException | IllegalArgumentException | XValueReaderException
				| XEntityFieldScrapeException ex)
		  {
				throw new XConversionException(ex);
		  }
	 }

}
