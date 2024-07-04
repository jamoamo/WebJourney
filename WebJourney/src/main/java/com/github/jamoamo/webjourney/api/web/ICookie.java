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
package com.github.jamoamo.webjourney.api.web;

import java.time.LocalDate;

/**
 *
 * @author James Amoore
 */
public interface ICookie
{
	/**
	 * Retrieve the cookie domain. 
	 * @return the cookie domain
	 */
	String getDomain();

	/**
	 * Retrieve the cookie expiry. 
	 * @return the cookie expiry. 
	 */
	LocalDate getExpiry();

	/**
	 * Retrieve the cookie name. 
	 * @return the cookie name. 
	 */
	String getName();

	/**
	 * Retrieve the cookie path. 
	 * @return the cookie path
	 */
	String getPath();

	/**
	 * Retrieve the cookie value. 
	 * @return the cookie value. 
	 */
	String getValue();

	/**
	 * indicates if the cookie is http only.
	 * @return if the cookie is http only
	 */
	boolean isHttpOnly();

	/**
	 * indicates if the cookie is secure.
	 * @return if the cookie is secure
	 */
	boolean isSecure();
}
