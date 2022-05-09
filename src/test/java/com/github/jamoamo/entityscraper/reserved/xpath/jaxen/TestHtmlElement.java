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

import com.github.jamoamo.entityscraper.api.html.AHtmlAttribute;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author James Amoore
 */
public class TestHtmlElement extends AHtmlElement
{
	private final String name;
	private final String text;
	private List<AHtmlAttribute> attributes;
	private List<AHtmlElement> elements;
	
	public TestHtmlElement(String name, String text)
	{
		this(name, text, new ArrayList<>(), new ArrayList<>());
	}
	
	public TestHtmlElement(String name, List<AHtmlElement> elements)
	{
		this(name, "", new ArrayList<>(), elements);
	}
	
	public TestHtmlElement(String name, String text, List<AHtmlAttribute> attributes, List<AHtmlElement> elements)
	{
		this.name = name;
		this.text = text;
		this.attributes = attributes;
		this.elements = elements;
	}

	TestHtmlElement(String name, List<AHtmlAttribute> attributes, List<AHtmlElement> elements)
	{
		this(name, "", attributes, elements);
	}

	@Override
	public String getElementName()
	{
		return this.name;
	}

	@Override
	public List<AHtmlAttribute> getAttribute(String attribute)
	{
		return this.attributes.stream().filter(attr -> attr.getAttributeName().equals(attribute)).collect(Collectors.toList());
	}

	@Override
	public List<AHtmlAttribute> getAttributes()
	{
		return this.attributes;
	}

	@Override
	public List<AHtmlElement> getElements(String tag)
	{
		return this.elements.stream().filter(elem -> elem.getElementName().equals(tag)).collect(Collectors.toList());
	}

	@Override
	public List<AHtmlElement> getAllElements()
	{
		return this.elements;
	}

	@Override
	public String getText()
	{
		return text;
	}
}
