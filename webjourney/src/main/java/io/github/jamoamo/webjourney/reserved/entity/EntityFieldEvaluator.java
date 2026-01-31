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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class EntityFieldEvaluator implements IEntityFieldEvaluator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityFieldEvaluator.class);

	private final List<IExtractor> extractors;
	private final IConverter converter;
	private final ITransformer transformer;

	EntityFieldEvaluator(
		List<IExtractor> extractors,
		ITransformer transformer,
		IConverter converter)
	{
		this.extractors = extractors;
		this.transformer = transformer;
		this.converter = converter;
	}

	@Override
	public Object evaluate(IValueReader browser, List<IEntityCreationListener> listeners,
		EntityCreationContext entityCreationContext) throws XEntityEvaluationException
	{
		Object extractedValue = extractValue(browser, entityCreationContext);

		Object transformedValue = extractedValue;
		if (this.transformer != null)
		{
			transformedValue = this.transformer.transformValue(extractedValue);
		}

		Object convertedValue =
			this.converter.convertValue(transformedValue, browser, new ArrayList<>(), entityCreationContext);
		return convertedValue;
	}

	private Object extractValue(IValueReader browser, EntityCreationContext entityCreationContext)
		throws XExtractionException
	{
		LOGGER.debug("Evaluating value for entity field");
		List<IExtractor> matchingExtractors = new ArrayList<>();
		for (IExtractor extractor : this.extractors)
		{
			LOGGER.debug("Checking extractor: {}", extractor.describe());
			if (extractor.getCondition().evaluate(browser, entityCreationContext))
			{
				LOGGER.debug("Extractor applies");
				matchingExtractors.add(extractor);
			}
		}

		Object extractedValue = null;
		if (matchingExtractors.isEmpty())
		{
			LOGGER.warn("No extractors apply. Defaulting to null.");
			return null;
		}
		else if (matchingExtractors.size() > 1)
		{
			String extractors = matchingExtractors.stream().map(IExtractor::describe).collect(Collectors.joining(", "));
			throw new XEntityEvaluationException(
				"More than one Extractor applies. Extractors: " + extractors);
		}
		else
		{
			LOGGER.debug("Using extractor: {}", matchingExtractors.get(0).describe());
			extractedValue = matchingExtractors.get(0).extractRawValue(browser, entityCreationContext);
		}
		LOGGER.debug("Extracted value: {}", extractedValue);
		return extractedValue;
	}

}
