/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package com.github.jamoamo.entityscraper.api.xpath;

import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;

/**
 * Representation of an xpath expression. Used to evaluate a docuement model based on the xpath expression. 
 * @author James Amoore
 */
public abstract class XPathExpression
{
	/**
	 * Gets the string value result of the xpath expression for the provided document.
	 * 
	 * @param document The document that needs to be queried by this xpath expression.
	 * @return the evaluated string value.
	 * @throws XXPathException if an exception occurs evaluating the document using the xpath expression. 
	 */
	public abstract String evaluateStringValue(AHtmlDocument document)
			  throws XXPathException;
}
