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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.ICookie;
import java.time.LocalDate;
import org.openqa.selenium.Cookie;

/**
 *
 * @author James Amoore
 */
class SeleniumCookieAdapter implements ICookie
{
	private final Cookie cookie;

	SeleniumCookieAdapter(Cookie cookie)
	{
		this.cookie = cookie;
	}

	@Override
	public String getDomain()
	{
		return this.cookie.getDomain();
	}

	@Override
	public LocalDate getExpiry()
	{
		return LocalDate.from(this.cookie.getExpiry().toInstant());
	}

	@Override
	public String getName()
	{
		return this.cookie.getName();
	}

	@Override
	public String getPath()
	{
		return this.cookie.getPath();
	}

	@Override
	public String getValue()
	{
		return this.cookie.getValue();
	}

	@Override
	public boolean isHttpOnly()
	{
		return this.cookie.isHttpOnly();
	}

	@Override
	public boolean isSecure()
	{
		return this.cookie.isSecure();
	}
}
