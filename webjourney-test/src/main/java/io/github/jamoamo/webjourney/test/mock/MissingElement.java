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
import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import java.util.Collections;
import java.util.List;

/**
 * Represents a required element that is missing. All operations throw.
 */
final class MissingElement extends AElement
{
	private XElementDoesntExistException error()
	{
		return new XElementDoesntExistException();
	}

	@Override
	public String getAttribute(String attribute) throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public String getElementText() throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public AElement findElement(String path) throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public AElement findElement(String path, boolean optional) throws XElementDoesntExistException
	{
		if(optional)
		{
			return new NullElement();
		}
		throw error();
	}

	@Override
	public List<? extends AElement> findElements(String path) throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public void click() throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public void enterText(String text) throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType) throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public String getTag() throws XElementDoesntExistException
	{
		throw error();
	}

	@Override
	public boolean exists()
	{
		return false;
	}
}