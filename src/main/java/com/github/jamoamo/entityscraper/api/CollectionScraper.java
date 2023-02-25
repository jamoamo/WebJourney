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

import com.github.jamoamo.entityscraper.annotation.Entity;
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.mapper.AValueMapper;
import com.github.jamoamo.entityscraper.api.mapper.XValueMappingException;
import com.github.jamoamo.entityscraper.api.xpath.AXPathExpression;
import com.github.jamoamo.entityscraper.api.xpath.XXPathException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author James Amoore
 */
class CollectionScraper extends AFieldScraper
{
	@Override
	Object scrapeValue(AHtmlDocument document, EntityScrapeContext context, EntityFieldDefn defn)
	{
		try
		{
			List<String> evaluatedValue = evaluateValue(document, context, defn);
			return mapValue(defn, evaluatedValue);
		}
		catch(XXPathException | XValueMappingException ex)
		{
			throw new RuntimeException("Value Scraping failed for field " + defn.getFieldName());
		}
	}
	
	private List<String> evaluateValue(AHtmlDocument document, EntityScrapeContext context, EntityFieldDefn defn)
		 throws XXPathException
	{
		Class<?> entityClass = context.getEntityClass();
		Entity entity = entityClass.getAnnotation(Entity.class);
		
		String xPathExpression = entity.rootPath() + defn.getCollection().path();
		AXPathExpression expression = context.getEvaluator().forPath(xPathExpression);

		List<String> evaluatedValue = expression.evaluateListValue(document);
		return evaluatedValue;
	}
	
	private List<Object> mapValue(EntityFieldDefn defn, List<String> evaluatedValues)
		 throws XValueMappingException
	{
		AValueMapper mapper = defn.getMapper();
		List<Object> objects = new ArrayList();
		for(String evaluatedValue : evaluatedValues)
		{
			Class<?> genericType = defn.getMultiType();
			Object mappedValue =
				 mapAndTransformValue(mapper, evaluatedValue, genericType, defn.getTransformation());
			objects.add(mappedValue);
		}
		return objects;
	}
}
