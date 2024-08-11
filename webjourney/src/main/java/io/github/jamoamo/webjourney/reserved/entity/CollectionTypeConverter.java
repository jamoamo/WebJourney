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
import io.github.jamoamo.webjourney.api.mapper.AConverter;
import io.github.jamoamo.webjourney.api.mapper.XValueMappingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author James Amoore
 * @param <T> The mapper type
 */
class CollectionTypeConverter<T>
	 implements IConverter<Collection<String>, Collection<T>>
{
	 private final AConverter<T> mapping;

	 CollectionTypeConverter(AConverter<T> mapping)
	 {
		  this.mapping = mapping;
	 }

	 @Override
	 public Collection<T> convertValue(Collection<String> source,
		  IValueReader reader,
		  List<IEntityCreationListener> listeners,
		  EntityCreationContext context)
		  throws XConversionException
	 {
		  if(source == null)
		  {
				return null;
		  }
		  List<T> mappedCollection = new ArrayList<>(source.size());
		  for(String value : source)
		  {
				try
				{
					 context.processCollectionItem();
					 T mapValue = this.mapping.mapValue(value);
					 mappedCollection.add(mapValue);
				}
				catch(XValueMappingException ex)
				{
					 throw new XConversionException(ex);
				}
		  }
		  return mappedCollection;
	 }

}
