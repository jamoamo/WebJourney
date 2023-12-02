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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author James Amoore
 */
class EntityFieldEvaluator implements IEntityFieldEvaluator
{
	private final List<IExtractor> extractors;
	private final IConverter converter;
	private final ITransformer transformer;
	
	EntityFieldEvaluator(IExtractor extractor, ITransformer transformer, IConverter converter)
	{
		this.extractors = Collections.singletonList(extractor);
		this.transformer = transformer;
		this.converter = converter;
	}
	
	EntityFieldEvaluator(List<IExtractor> extractors, ITransformer transformer, IConverter converter)
	{
		this.extractors = extractors;
		this.transformer = transformer;
		this.converter = converter;
	}

	@Override
	public Object evaluate(IValueReader browser)
	{
		Object extractedValue = extractValue(browser);
		
		Object transformedValue = extractedValue;
		if(this.transformer != null)
		{
			transformedValue = this.transformer.transformValue(extractedValue);
		}

		Object convertedValue = this.converter.convertValue(transformedValue, browser);
		return convertedValue;
	}

	private Object extractValue(IValueReader browser)
		throws RuntimeException
	{
		List<IExtractor> matchingExtractors =
			this.extractors.stream().filter(extractor -> extractor.getCondition().evaluate(browser)).toList();
		Object extractedValue = null;
		if(matchingExtractors.isEmpty())
		{
			throw new RuntimeException("No Extractors apply");
		}
		else if(matchingExtractors.size() > 1)
		{
			throw new RuntimeException("More than one Extractor applies.");
		}
		else
		{
			extractedValue = this.extractors.get(0).extractRawFieldValue(browser);
		}
		return extractedValue;
	}
}
