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
 * Exception thrown when connection to Selenium Hub fails.
 * This includes network connectivity issues, hub unavailability,
 * and authentication failures.
 * 
 * @author James Amoore
 */
public class HubConnectionException extends RemoteBrowserException
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new hub connection exception.
	 * 
	 * @param message the error message
	 */
	public HubConnectionException(String message)
	{
		super(message);
	}
	
	/**
	 * Creates a new hub connection exception with a cause.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 */
	public HubConnectionException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Creates a new hub connection exception with hub context.
	 * 
	 * @param message the error message
	 * @param hubUrl the hub URL that failed
	 * @param retryAttempts the number of retry attempts made
	 */
	public HubConnectionException(String message, String hubUrl, int retryAttempts)
	{
		super(message, hubUrl, retryAttempts);
	}
	
	/**
	 * Creates a new hub connection exception with full context.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 * @param hubUrl the hub URL that failed
	 * @param retryAttempts the number of retry attempts made
	 */
	public HubConnectionException(String message, Throwable cause, String hubUrl, int retryAttempts)
	{
		super(message, cause, hubUrl, retryAttempts);
	}
}
