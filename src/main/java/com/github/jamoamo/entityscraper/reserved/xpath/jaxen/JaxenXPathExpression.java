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
package com.github.jamoamo.entityscraper.reserved.xpath.jaxen;

import com.github.jamoamo.entityscraper.api.xpath.XXPathException;
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import com.github.jamoamo.entityscraper.api.xpath.AXPathExpression;
import java.util.List;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

/**
 * XPathExpression implementation that uses Jaxen to query the document model.
 *
 * @author James Amoore
 */
public final class JaxenXPathExpression
	 extends AXPathExpression
{
	private final XPath xpath;

	/**
	 * Creates a new instance.
	 * @param xpathExpr The xpath expression string
	 * @throws JaxenException if there was an error parsing the xpath expression
	 */
	public JaxenXPathExpression(String xpathExpr)
		 throws JaxenException
	{
		xpath = new HtmlDocumentXPath(xpathExpr);
	}

	/**
	 * Evaluates the provided document with the xpath expression and returns the evaluated string value.
	 * 
	 * @param document The doucment to evaluate
	 * @return The string value result of the evaluation.
	 * @throws XXPathException if there is an error.
	 */
	@Override
	public String evaluateStringValue(AHtmlDocument document)
		 throws XXPathException
	{
		try
		{
			return xpath.stringValueOf(document);
		}
		catch(JaxenException ex)
		{
			throw new XXPathException(ex);
		}
	}

	@Override
	public List<String> evaluateListValue(AHtmlDocument document)
		 throws XXPathException
	{
		try
		{
			List<AHtmlElement> elements = xpath.selectNodes(document);
			return elements.stream().map(element -> element.getText()).toList();
		}
		catch(JaxenException ex)
		{
			throw new XXPathException(ex);
		}
	}

}
