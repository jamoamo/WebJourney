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

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.function.Failable;

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
	public String getCurrentUrl() throws XValueReaderException
	{
		try
		{
			return this.browser
				.getActiveWindow()
				.getCurrentUrl();
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public String getElementText(String xPath, boolean optional) throws XValueReaderException
	{
		try
		{
			return this.browser
				.getActiveWindow()
				.getCurrentPage()
				.getElement(xPath, optional)
				.getElementText();
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public AElement getElement(String xPath, boolean optional) throws XValueReaderException
	{
		try
		{
			return this.browser.getActiveWindow().getCurrentPage().getElement(xPath, optional);
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public String getAttribute(String elementXPath, String attr) throws XValueReaderException
	{
		try
		{
			return this.browser.getActiveWindow().getCurrentPage().getElement(elementXPath, false).getAttribute(attr);
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}
	
	@Override
	public List<String> getAttributes(String elementXPath, String attr) throws XValueReaderException
	{
		try
		{
			return Failable.stream(
					this.browser
						.getActiveWindow()
						.getCurrentPage()
						.getElements(elementXPath))
				.map(e -> e.getAttribute(attr))
				.collect(Collectors.toList());
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public void navigateTo(URL url) throws XValueReaderException
	{
		try
		{
			this.browser.getActiveWindow().navigateToUrl(url);
		}
		catch(XWebException error)
		{
			throw new XValueReaderException(error);
		}
	}

	@Override
	public void navigateBack() throws XValueReaderException
	{
		try
		{
			this.browser.getActiveWindow().navigateBack();
		}
		catch(XWebException error)
		{
			throw new XValueReaderException(error);
		}
	}

	@Override
	public List<? extends AElement> getElements(String xPath) throws XValueReaderException
	{
		try
		{
			return this.browser.getActiveWindow().getCurrentPage().getElements(xPath);
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public List<String> getElementTexts(String xPath) throws XValueReaderException
	{
		try
		{
			return Failable.stream(
					this.browser
						.getActiveWindow()
						.getCurrentPage()
						.getElements(xPath))
				.map(e -> e.getElementText())
				.collect(Collectors.toList());
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public IBrowser getBrowser()
	{
		return this.browser;
	}

	@Override
	public void openNewWindow() throws XValueReaderException
	{
		try
		{
			this.browser.openNewWindow();
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}

	@Override
	public void closeWindow() throws XValueReaderException
	{
		try
		{
			this.browser.getActiveWindow().close();
		}
		catch(XWebException ex)
		{
			throw new XValueReaderException(ex);
		}
	}
	
}
