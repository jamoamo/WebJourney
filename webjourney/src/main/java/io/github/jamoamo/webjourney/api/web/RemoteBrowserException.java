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
 * Base exception for remote browser creation and management failures.
 * This exception and its subclasses represent various failure scenarios
 * when working with remote Selenium Hub infrastructure.
 * 
 * @author James Amoore
 */
public class RemoteBrowserException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	private final String hubUrl;
	private final int retryAttempts;
	
	/**
	 * Creates a new remote browser exception.
	 * 
	 * @param message the error message
	 */
	public RemoteBrowserException(String message)
	{
		this(message, null, null, 0);
	}
	
	/**
	 * Creates a new remote browser exception with a cause.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 */
	public RemoteBrowserException(String message, Throwable cause)
	{
		this(message, cause, null, 0);
	}
	
	/**
	 * Creates a new remote browser exception with hub context.
	 * 
	 * @param message the error message
	 * @param hubUrl the hub URL that failed
	 * @param retryAttempts the number of retry attempts made
	 */
	public RemoteBrowserException(String message, String hubUrl, int retryAttempts)
	{
		this(message, null, hubUrl, retryAttempts);
	}
	
	/**
	 * Creates a new remote browser exception with full context.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 * @param hubUrl the hub URL that failed
	 * @param retryAttempts the number of retry attempts made
	 */
	public RemoteBrowserException(String message, Throwable cause, String hubUrl, int retryAttempts)
	{
		super(message, cause);
		this.hubUrl = hubUrl;
		this.retryAttempts = Math.max(0, retryAttempts);
	}
	
	/**
	 * Gets the hub URL associated with this failure.
	 * 
	 * @return the hub URL, or null if not specified
	 */
	public String getHubUrl()
	{
		return hubUrl;
	}
	
	/**
	 * Gets the number of retry attempts made before failing.
	 * 
	 * @return the retry attempts count
	 */
	public int getRetryAttempts()
	{
		return retryAttempts;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		if (hubUrl != null)
		{
			sb.append(" [hubUrl=").append(hubUrl).append("]");
		}
		if (retryAttempts > 0)
		{
			sb.append(" [retryAttempts=").append(retryAttempts).append("]");
		}
		return sb.toString();
	}
}
