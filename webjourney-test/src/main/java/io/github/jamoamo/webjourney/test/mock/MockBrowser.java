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

import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.net.MalformedURLException;

/**
 * Mock implementation of IBrowser managing windows and a router.
 */
public final class MockBrowser implements IBrowser
{
	private final MockRouter router;
	private final Map<String, MockBrowserWindow> windows = new LinkedHashMap<>();

	public MockBrowser(MockRouter router)
	{
		this.router = router == null ? new MockRouter() : router;
		String main = UUID.randomUUID().toString();
		MockBrowserWindow window = new MockBrowserWindow(main, this.router);
		window.setActive(true);
		this.windows.put(main, window);
	}

	public MockRouter getRouter()
	{
		return this.router;
	}

	public void loadInitial(String url) throws XWebException, MalformedURLException
	{
		MockBrowserWindow window = (MockBrowserWindow) getActiveWindow();
		window.loadInitial(url);
	}

	@Override
	public IBrowserWindow getActiveWindow() throws XWebException
	{
		return this.windows.values().stream().filter(MockBrowserWindow::isActive).findFirst().orElse(null);
	}

	@Override
	public IBrowserWindow switchToWindow(String windowName) throws XWebException
	{
		this.windows.values().forEach(w -> w.setActive(false));
		MockBrowserWindow window = this.windows.get(windowName);
		if(window != null)
		{
			window.setActive(true);
		}
		return window;
	}

	@Override
	public IBrowserWindow openNewWindow() throws XWebException
	{
		this.windows.values().forEach(w -> w.setActive(false));
		String name = UUID.randomUUID().toString();
		MockBrowserWindow window = new MockBrowserWindow(name, this.router);
		this.windows.put(name, window);
		window.setActive(true);
		return window;
	}

	@Override
	public void exit()
	{
		this.windows.clear();
	}
}