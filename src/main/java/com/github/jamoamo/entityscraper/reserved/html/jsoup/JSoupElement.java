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
package com.github.jamoamo.entityscraper.reserved.html.jsoup;

import com.github.jamoamo.entityscraper.api.html.AHtmlAttribute;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;

/**
 *
 * @author James Amoore
 */
public class JSoupElement
	 extends AHtmlElement
{
	private final Element element;

	public JSoupElement(Element element)
	{
		this.element = element;
	}

	@Override
	public String getElementName()
	{
		return element.tagName();
	}

	@Override
	public List<AHtmlAttribute> getAttribute(String attribute)
	{
		return element.attributes()
			 .asList()
			 .stream()
			 .filter(attr -> attr.getKey()
				  .equals(attribute))
			 .map(attr -> new JSoupAttribute(attr))
			 .collect(Collectors.toList());
	}

	@Override
	public List<AHtmlAttribute> getAttributes()
	{
		return element.attributes()
			 .asList()
			 .stream()
			 .map(attr -> new JSoupAttribute(attr))
			 .collect(Collectors.toList());
	}

	@Override
	public List<AHtmlElement> getElements(String tag)
	{
		return element.children()
			 .stream()
			 .filter(elem -> elem.tagName()
				  .equals(tag))
			 .map(elem -> new JSoupElement(elem))
			 .collect(Collectors.toList());
	}

	@Override
	public List<AHtmlElement> getAllElements()
	{
		return element
			 .children()
			 .stream()
			 .map(elem -> new JSoupElement(elem))
			 .collect(Collectors.toList());
	}

	@Override
	public String getText()
	{
		return element.ownText();
	}

}
