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

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when no available hubs can be found for browser creation.
 * This is typically used in load balancing scenarios where all configured
 * hubs are unavailable or unhealthy.
 * 
 * @author James Amoore
 */
public class NoAvailableHubException extends RemoteBrowserException
{
	private static final long serialVersionUID = 1L;
	
	private final List<String> attemptedHubs;
	
	/**
	 * Creates a new no available hub exception.
	 * 
	 * @param message the error message
	 */
	public NoAvailableHubException(String message)
	{
		this(message, Collections.emptyList());
	}
	
	/**
	 * Creates a new no available hub exception with attempted hubs.
	 * 
	 * @param message the error message
	 * @param attemptedHubs the list of hub URLs that were attempted
	 */
	public NoAvailableHubException(String message, List<String> attemptedHubs)
	{
		super(message);
		this.attemptedHubs = attemptedHubs != null ? 
			Collections.unmodifiableList(attemptedHubs) : Collections.emptyList();
	}
	
	/**
	 * Creates a new no available hub exception with cause and attempted hubs.
	 * 
	 * @param message the error message
	 * @param cause the underlying cause
	 * @param attemptedHubs the list of hub URLs that were attempted
	 */
	public NoAvailableHubException(String message, Throwable cause, List<String> attemptedHubs)
	{
		super(message, cause);
		this.attemptedHubs = attemptedHubs != null ? 
			Collections.unmodifiableList(attemptedHubs) : Collections.emptyList();
	}
	
	/**
	 * Gets the list of hub URLs that were attempted.
	 * 
	 * @return an immutable list of attempted hub URLs
	 */
	public List<String> getAttemptedHubs()
	{
		return attemptedHubs;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		if (!attemptedHubs.isEmpty())
		{
			sb.append(" [attemptedHubs=").append(attemptedHubs).append("]");
		}
		return sb.toString();
	}
}
