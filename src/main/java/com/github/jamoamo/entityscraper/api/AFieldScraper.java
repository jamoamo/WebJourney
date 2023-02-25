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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.annotation.Transformation;
import com.github.jamoamo.entityscraper.api.function.ATransformationFunction;
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.mapper.AValueMapper;
import com.github.jamoamo.entityscraper.api.mapper.XValueMappingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author James Amoore
 */
abstract class AFieldScraper
{
	abstract Object scrapeValue(AHtmlDocument document, EntityScrapeContext context, EntityFieldDefn defn);
	
	protected Object mapAndTransformValue(AValueMapper mapper, String evaluatedValue, Class<?> fieldClass,
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
}
