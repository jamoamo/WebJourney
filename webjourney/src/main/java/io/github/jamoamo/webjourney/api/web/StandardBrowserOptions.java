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
package io.github.jamoamo.webjourney.api.web;

/**
 * Standard Browser Options.
 * @author James Amoore
 */
public class StandardBrowserOptions implements IBrowserOptions
{
	private final boolean isHeadless;
	private final boolean acceptUnexpectedAlerts;
	
	/**
	 * Constructor.
	 * @param isHeadless should the browser be headless
	 * @param acceptUnexpectedAlerts should the browser accept unexpected alerts
	 */
	public StandardBrowserOptions(boolean isHeadless, boolean acceptUnexpectedAlerts)
	{
		this.isHeadless = isHeadless;
		this.acceptUnexpectedAlerts = acceptUnexpectedAlerts;
	}
	
	/**
	 * Should the browser be headless.
	 * @return true if the browser should be headless.
	 */
	@Override
	public boolean isHeadless()
	{
		return this.isHeadless;
	}

	/**
	 * Should unexpected alerts be accepted.
	 * @return true if unexpected alerts should be accepted.
	 */
	@Override
	public boolean acceptUnexpectedAlerts()
	{
		return this.acceptUnexpectedAlerts;
	}
	
}
