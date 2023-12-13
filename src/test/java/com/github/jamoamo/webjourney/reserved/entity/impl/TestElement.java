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
package com.github.jamoamo.webjourney.reserved.entity.impl;

import com.github.jamoamo.webjourney.api.web.AElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 *
 * @author James Amoore
 */
public class TestElement extends AElement
{
	private final String text;
	private final AElement[] subElements;
	private final String elementName;
	private final Map<String, String> attributes;

	public TestElement(String text)
	{
		this.elementName = "";
		this.attributes = new TreeMap<>();
		this.text = text;
		this.subElements = new AElement[]
		{
		};
	}

	public TestElement(String elementName, Map<String, String> attributes, String text)
	{
		this.elementName = elementName;
		this.attributes = attributes;
		this.text = text;
		this.subElements = new AElement[]
		{
		};
	}

	public TestElement(String text, AElement[] subElements)
	{
		this.elementName = "";
		this.attributes = new TreeMap<>();
		this.text = text;
		this.subElements = subElements;
	}

	@Override
	public String getElementText()
	{
		return text;
	}

	@Override
	public AElement findElement(String path)
	{
		if(!path.startsWith("//"))
		{
			return null;
		}

		path = path.substring(2);

		if(!path.startsWith("div"))
		{
			return null;
		}

		path = path.substring(3);

		if(!path.startsWith("[") && !path.endsWith("]"))
		{
			return null;
		}

		path = path.substring(1, path.length() - 1);

		if(!path.startsWith("@id"))
		{
			return null;
		}

		String id = path.substring(5, path.length() - 1);

		Optional<AElement> findFirst =
				  Arrays.stream(subElements).filter(element -> element.getAttribute("id").equals(id)).findFirst();

		return findFirst.orElse(null);
	}

	@Override
	public List<? extends AElement> findElements(String path)
	{
		if(!path.startsWith("//"))
		{
			return null;
		}

		path = path.substring(2);

		if(!path.startsWith("div"))
		{
			return null;
		}

		path = path.substring(3);

		if(!path.startsWith("[") && !path.endsWith("]"))
		{
			return null;
		}

		path = path.substring(1, path.length() - 1);

		if(!path.startsWith("@id"))
		{
			return null;
		}

		String id = path.substring(4, path.length() - 1);

		List<AElement> list =
				  Arrays.stream(subElements).filter(element -> element.getAttribute("id").equals(id)).toList();

		return list;
	}

	@Override
	public String getAttribute(String attribute)
	{
		return attributes.get(attribute);
	}

	@Override
	public void click()
	{
		
	}

	@Override
	public void enterText(String text)
	{
		
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType)
	{
		return Arrays.stream(this.subElements).filter(elem -> elem.getTag().equals(childElementType)).toList();
	}

	@Override
	public String getTag()
	{
		return this.elementName;
	}
}
