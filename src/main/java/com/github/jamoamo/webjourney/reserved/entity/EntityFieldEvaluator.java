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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class EntityFieldEvaluator implements IEntityFieldEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityFieldEvaluator.class);
	
	private final List<IExtractor> extractors;
	private final IConverter converter;
	private final ITransformer transformer;
	
	EntityFieldEvaluator(List<IExtractor> extractors, ITransformer transformer, IConverter converter)
	{
		this.extractors = extractors;
		this.transformer = transformer;
		this.converter = converter;
	}

	@Override
	public Object evaluate(IValueReader browser, 
								  List<IEntityCreationListener> listeners)
		throws XEntityEvaluationException
	{
		Object extractedValue = extractValue(browser);
		
		Object transformedValue = extractedValue;
		if(this.transformer != null)
		{
			transformedValue = this.transformer.transformValue(extractedValue);
		}

		Object convertedValue = this.converter.convertValue(transformedValue, browser, new ArrayList<>());
		return convertedValue;
	}

	private Object extractValue(IValueReader browser) throws XExtractionException
	{
		List<IExtractor> matchingExtractors = new ArrayList<>();
		for(IExtractor extractor : this.extractors)
		{
			if(extractor.getCondition().evaluate(browser))
			{
				matchingExtractors.add(extractor);
			}
		}
		
		Object extractedValue = null;
		if(matchingExtractors.isEmpty())
		{
			LOGGER.warn("No extractors apply. Defaulting to null.");
			return null;
		}
		else if(matchingExtractors.size() > 1)
		{
			throw new RuntimeException("More than one Extractor applies.");
		}
		else
		{
			extractedValue = matchingExtractors.get(0).extractRawValue(browser);
		}
		return extractedValue;
	}
}
