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
package io.github.jamoamo.webjourney.test.mock;

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.util.ArrayList;
import java.util.List;

/**
 * A mock web page backed by a root mock element.
 */
public final class MockWebPage implements IWebPage
{
	private final MockElement root;
	private String title;

	private MockWebPage(MockElement root)
	{
		this.root = root;
	}

	public static MockWebPage root(MockElement root)
	{
		return new MockWebPage(root);
	}

	public MockWebPage title(String title)
	{
		this.title = title;
		return this;
	}

	String getTitle()
	{
		return this.title;
	}

	MockElement getRoot()
	{
		return this.root;
	}

	@Override
	public AElement getElement(String xPath) throws XWebException
	{
		return getElement(xPath, false);
	}

	@Override
	public AElement getElement(String xPath, boolean optional) throws XWebException
	{
		if(this.root == null)
		{
			return optional ? new NullElement() : new MissingElement();
		}
		AElement element = this.root.findElement(xPath, true);
		if(element == null)
		{
			return optional ? new NullElement() : new MissingElement();
		}
		return element;
	}

	@Override
	public List<? extends AElement> getElements(String xPath) throws XWebException
	{
		if(this.root == null)
		{
			return new ArrayList<>();
		}
		return this.root.findElements(xPath);
	}

	@Override
	public List<? extends AElement> getElementsByTag(String tag) throws XWebException
	{
		if(this.root == null)
		{
			return new ArrayList<>();
		}
		return this.root.getChildrenByTag(tag);
	}
}