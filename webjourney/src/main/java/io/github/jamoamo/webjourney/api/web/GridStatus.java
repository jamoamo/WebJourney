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

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the health status of a Selenium Grid hub.
 * Contains information about availability, response time, and error details.
 * 
 * @author James Amoore
 */
public final class GridStatus
{
	private final String hubUrl;
	private final boolean available;
	private final long responseTimeMs;
	private final Instant lastChecked;
	private final String errorMessage;
	private final int consecutiveFailures;
	
	/**
	 * Creates a new grid status for an available hub.
	 *
	 * @param hubUrl the hub URL
	 * @param responseTimeMs the response time in milliseconds
	 * @param lastChecked when the status was last checked
	 */
	public GridStatus(String hubUrl, long responseTimeMs, Instant lastChecked)
	{
		this(hubUrl, true, responseTimeMs, lastChecked, null, 0);
	}
	
	/**
	 * Creates a new grid status for an unavailable hub.
	 *
	 * @param hubUrl the hub URL
	 * @param lastChecked when the status was last checked
	 * @param errorMessage the error message
	 * @param consecutiveFailures the number of consecutive failures
	 */
	public GridStatus(String hubUrl, Instant lastChecked, String errorMessage, int consecutiveFailures)
	{
		this(hubUrl, false, -1, lastChecked, errorMessage, consecutiveFailures);
	}
	
	/**
	 * Creates a new grid status with full details.
	 *
	 * @param hubUrl the hub URL
	 * @param available whether the hub is available
	 * @param responseTimeMs the response time in milliseconds (-1 if unavailable)
	 * @param lastChecked when the status was last checked
	 * @param errorMessage the error message (null if available)
	 * @param consecutiveFailures the number of consecutive failures
	 */
	public GridStatus(String hubUrl, boolean available, long responseTimeMs, 
					 Instant lastChecked, String errorMessage, int consecutiveFailures)
	{
		this.hubUrl = Objects.requireNonNull(hubUrl, "Hub URL cannot be null");
		this.available = available;
		this.responseTimeMs = responseTimeMs;
		this.lastChecked = Objects.requireNonNull(lastChecked, "Last checked time cannot be null");
		this.errorMessage = errorMessage;
		this.consecutiveFailures = Math.max(0, consecutiveFailures);
	}
	
	/**
	 * Gets the hub URL.
	 *
	 * @return the hub URL
	 */
	public String getHubUrl()
	{
		return hubUrl;
	}
	
	/**
	 * Determines if the hub is available.
	 *
	 * @return true if the hub is available, false otherwise
	 */
	public boolean isAvailable()
	{
		return available;
	}
	
	/**
	 * Gets the response time in milliseconds.
	 *
	 * @return the response time, or -1 if the hub is unavailable
	 */
	public long getResponseTimeMs()
	{
		return responseTimeMs;
	}
	
	/**
	 * Gets when the status was last checked.
	 *
	 * @return the last checked timestamp
	 */
	public Instant getLastChecked()
	{
		return lastChecked;
	}
	
	/**
	 * Gets the error message if the hub is unavailable.
	 *
	 * @return the error message, or null if the hub is available
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	/**
	 * Gets the number of consecutive failures.
	 *
	 * @return the consecutive failures count
	 */
	public int getConsecutiveFailures()
	{
		return consecutiveFailures;
	}
	
	/**
	 * Determines if this status is stale based on the given age threshold.
	 *
	 * @param maxAge the maximum age before status is considered stale
	 * @return true if the status is stale, false otherwise
	 */
	public boolean isStale(java.time.Duration maxAge)
	{
		return lastChecked.plus(maxAge).isBefore(Instant.now());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}
		GridStatus that = (GridStatus) obj;
		return available == that.available &&
			   responseTimeMs == that.responseTimeMs &&
			   consecutiveFailures == that.consecutiveFailures &&
			   Objects.equals(hubUrl, that.hubUrl) &&
			   Objects.equals(lastChecked, that.lastChecked) &&
			   Objects.equals(errorMessage, that.errorMessage);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(hubUrl, available, responseTimeMs, lastChecked, errorMessage, consecutiveFailures);
	}
	
	@Override
	public String toString()
	{
		if (available)
		{
			return String.format("GridStatus{hubUrl='%s', available=true, responseTime=%dms, lastChecked=%s}", 
								hubUrl, responseTimeMs, lastChecked);
		}
		else
		{
			return String.format("GridStatus{hubUrl='%s', available=false, error='%s', failures=%d, lastChecked=%s}", 
								hubUrl, errorMessage, consecutiveFailures, lastChecked);
		}
	}
}
