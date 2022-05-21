/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy,  Fmodify, merge, publish, distribute, sublicense, and/or sell
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

import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import java.util.List;
import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.util.SingletonList;

/**
 * XPath for HTMLDocument.
 *
 * @author James Amoore
 */
public final class HtmlDocumentXPath extends BaseXPath
{
	/**
	 * Creates a new instance.
	 *
	 * @param xPathExpr The xpath expressionS
	 *
	 * @throws JaxenException if there is an error parsing the xpath expression
	 */
	public HtmlDocumentXPath(String xPathExpr)
			  throws JaxenException
	{
		super(xPathExpr, HtmlDocumentNavigator.getInstance());
	}

	@Override
	protected Context getContext(Object node)
	{
		if(node instanceof Context)
		{
			return (Context) node;
		}

		Context fullContext = new Context(getContextSupport());
		if(node instanceof AHtmlDocument)
		{
			AHtmlElement rootElement = (AHtmlElement) getNavigator().getDocumentNode(node);
			fullContext.setNodeSet(new SingletonList(rootElement));
		}
		else if(node instanceof List)
		{
			fullContext.setNodeSet((List) node);
		}
		else
		{
			List list = new SingletonList(node);
			fullContext.setNodeSet(list);
		}
		return fullContext;
	}
}
