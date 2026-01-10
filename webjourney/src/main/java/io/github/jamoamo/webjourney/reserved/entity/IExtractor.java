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

/**
 * An extractor of values.
 *
 * @author James Amoore
 * @param <T> Extractor type
 */
public interface IExtractor<T>
{
	/**
	 * Extracts a value using the provided value reader.
	 *
	 * @param reader                the reader
	 * @param entityCreationContext entity creation context
	 *
	 * @return the raw value.
	 *
	 * @throws io.github.jamoamo.webjourney.reserved.entity.XExtractionException if an error occurs
	 *                                                                              during extraction
	 */
	T extractRawValue(IValueReader reader, EntityCreationContext entityCreationContext) throws XExtractionException;

	/**
	 * Returns the condition under which this extractor should be used.
	 *
	 * @return the condition
	 */
	ICondition getCondition();

	/**
	 * Provides a human readable description of the extractor.
	 * 
	 * @return the extractor's description
	 */
	default String describe()
	{
		return this.getClass().getSimpleName();
	}

}
