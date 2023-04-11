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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.api.web.IPreferredBrowserStrategy;
import com.github.jamoamo.entityscraper.api.web.PreferredBrowserStrategy;
import com.github.jamoamo.entityscraper.api.web.StandardBrowser;
import java.time.Duration;

class DefaultJourneyOptions implements IJourneyOptions
{
	private static final int DEFAULT_WAIT = 2;
	
	@Override
	public Duration getDefaultWait()
	{
		return Duration.ofSeconds(DEFAULT_WAIT);
	}

	@Override
	public IPreferredBrowserStrategy getPreferredBrowserStrategy()
	{
		return new PreferredBrowserStrategy(StandardBrowser.CHROME);
	}
}
