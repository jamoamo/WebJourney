/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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

/**
 * Extractor that extracts the current index of a collection being processed.
 * 
 * @author James Amoore
 */
public final class CollectionIndexExtractor implements IExtractor<String>
{
	private final int baseIndex;

	CollectionIndexExtractor(
		int baseIndex)
	{
		this.baseIndex = baseIndex;
	}

	/**
	 * Extract the index.
	 * 
	 * @param reader                unused. the reader to read a value.
	 * @param entityCreationContext the current context
	 * @return the current index.
	 * @throws XExtractionException if a collection is not being processed.
	 */
	@Override
	public String extractRawValue(IValueReader reader, EntityCreationContext entityCreationContext)
		throws XExtractionException
	{
		if (entityCreationContext.getExistingIndex() == null)
		{
			throw new XExtractionException(
				"Can't use index when not part of a collection.");
		}

		return Integer.toString(entityCreationContext.getExistingIndex() + this.baseIndex);
	}

	/**
	 * @return an AlwaysCondition instance.
	 */
	@Override
	public ICondition getCondition()
	{
		return new AlwaysCondition();
	}

	@Override
	public String describe()
	{
		return "Collection Index";
	}
}
