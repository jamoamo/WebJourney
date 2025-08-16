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
 * Exception thrown when browser session creation or management fails on the hub.
 * This includes session capacity issues, capability rejection, and session timeout problems.
 * 
 * @author James Amoore
 */
public class HubSessionException extends RemoteBrowserException
{
	private static final long serialVersionUID = 1L;
	
	private final String sessionId;
	
	/**
	 * Creates a new hub session exception.
	 * 
	 * @param message the error message
	 */
	public HubSessionException(String message)
	{
		this(message, null, null);
	}
	
	/**
	 * Creates a new hub session exception with a cause.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 */
	public HubSessionException(String message, Throwable cause)
	{
		this(message, cause, null);
	}
	
	/**
	 * Creates a new hub session exception with session context.
	 * 
	 * @param message the error message
	 * @param sessionId the session ID that failed, if available
	 */
	public HubSessionException(String message, String sessionId)
	{
		this(message, null, sessionId);
	}
	
	/**
	 * Creates a new hub session exception with full context.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 * @param sessionId the session ID that failed, if available
	 */
	public HubSessionException(String message, Throwable cause, String sessionId)
	{
		super(message, cause);
		this.sessionId = sessionId;
	}
	
	/**
	 * Creates a new hub session exception with hub and session context.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 * @param hubUrl the hub URL that failed
	 * @param sessionId the session ID that failed, if available
	 * @param retryAttempts the number of retry attempts made
	 */
	public HubSessionException(String message, Throwable cause, String hubUrl, String sessionId, int retryAttempts)
	{
		super(message, cause, hubUrl, retryAttempts);
		this.sessionId = sessionId;
	}
	
	/**
	 * Gets the session ID associated with this failure.
	 * 
	 * @return the session ID, or null if not available
	 */
	public String getSessionId()
	{
		return sessionId;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		if (sessionId != null)
		{
			sb.append(" [sessionId=").append(sessionId).append("]");
		}
		return sb.toString();
	}
}
