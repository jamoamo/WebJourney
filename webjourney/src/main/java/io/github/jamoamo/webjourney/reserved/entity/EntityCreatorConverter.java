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
import io.github.jamoamo.webjourney.api.IRetryPolicy;
import java.lang.reflect.Field;
import java.net.URI;
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
	 private static final Logger logger = LoggerFactory.getLogger(EntityCreatorConverter.class);	 
	 private final EntityCreator entityCreator;

	 private IRetryPolicy retryPolicy;

	 EntityCreatorConverter(EntityFieldDefn fieldDefn)
		  throws XEntityFieldDefinitionException
	 {
		  this(fieldDefn.getFieldType(), fieldDefn.getField());
	 }

	 EntityCreatorConverter(Class<?> entityType, Field field)
		  throws XEntityFieldDefinitionException
	 {
		  try
		  {
				EntityDefn defn = new EntityDefn(entityType);
				this.entityCreator = new EntityCreator(defn, true, null);
				
				io.github.jamoamo.webjourney.annotation.Retry retryAnnotation = 
					field.getAnnotation(io.github.jamoamo.webjourney.annotation.Retry.class);
				if (retryAnnotation != null)
				{
					this.retryPolicy = io.github.jamoamo.webjourney.api.RetryPolicyBuilder.builder()
						.maxRetries(retryAnnotation.maxRetries())
						.delay(java.time.Duration.ofMillis(retryAnnotation.delayMs()))
						.build();
				}
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
				io.github.jamoamo.webjourney.api.IRetryPolicy policyToUse = this.retryPolicy;
				if (policyToUse == null && context != null && context.getRetryPolicy() != null)
				{
					policyToUse = context.getRetryPolicy();
				}
				if (policyToUse == null)
				{
					policyToUse = io.github.jamoamo.webjourney.api.RetryPolicyBuilder.builder().build();
				}

				URI uri = new URI(source);
				java.net.URL url = uri.toURL();

				Object instance = policyToUse.execute(() -> {
					reader.navigateTo(url);

					return this.entityCreator.createNewEntity(reader.getBrowser(), context);
				});
				NavigationUtils.retryNavigateBack(reader, 3, 500L);

				return instance;
		  }
		  catch (XConversionException ex)
		  {
			    throw ex;
		  }
		  catch(Exception ex)
		  {
				throw new XConversionException(ex);
		  }
	 }

}
