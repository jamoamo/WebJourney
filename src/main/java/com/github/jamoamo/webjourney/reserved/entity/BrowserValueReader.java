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
package com.github.jamoamo.webjourney.reserved.entity;

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.XNavigationError;
import java.net.URL;
import java.util.List;

/**
 *
 * @author James Amoore
 */
class BrowserValueReader implements IValueReader
{
	private final IBrowser browser;
	
	BrowserValueReader(IBrowser browser)
	{
		this.browser = browser;
	}
	
	@Override
	public String getCurrentUrl()
	{
		return this.browser.getActiveWindow().getCurrentUrl();
	}

	@Override
	public String getElementText(String xPath)
	{
		return this.browser.getActiveWindow().getCurrentPage().getElement(xPath).getElementText();
	}

	@Override
	public AElement getElement(String xPath)
	{
		return this.browser.getActiveWindow().getCurrentPage().getElement(xPath);
	}

	@Override
	public String getAttribute(String elementXPath, String attr)
	{
		return this.browser.getActiveWindow().getCurrentPage().getElement(elementXPath).getAttribute(attr);
	}

	@Override
	public void navigateTo(URL url)
	{
		try
		{
			this.browser.getActiveWindow().navigateToUrl(url);
		}
		catch(XNavigationError error)
		{
			throw new RuntimeException(error.getMessage());
		}
	}

	@Override
	public void navigateBack()
	{
		try
		{
			this.browser.getActiveWindow().navigateBack();
		}
		catch(XNavigationError error)
		{
			throw new RuntimeException(error.getMessage());
		}
	}

	@Override
	public List<? extends AElement> getElements(String xPath)
	{
		return this.browser.getActiveWindow().getCurrentPage().getElements(xPath);
	}

	@Override
	public List<String> getElementTexts(String xPath)
	{
		return this.browser.getActiveWindow()
			.getCurrentPage()
			.getElements(xPath)
			.stream()
			.map(e -> e.getElementText())
			.toList();
	}

	@Override
	public IBrowser getBrowser()
	{
		return this.browser;
	}

	@Override
	public void openNewWindow()
	{
		this.browser.openNewWindow();
	}

	@Override
	public void closeWindow()
	{
		this.browser.getActiveWindow().close();
	}
	
}
