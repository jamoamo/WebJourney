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
 * Listener interface for receiving notifications about Grid hub health changes.
 * Implementations can respond to health events for monitoring, alerting, or
 * automated recovery actions.
 * 
 * @author James Amoore
 */
public interface GridHealthListener
{
	/**
	 * Called when a hub becomes available after being unavailable.
	 *
	 * @param status the current status of the hub
	 */
	void onHubAvailable(GridStatus status);
	
	/**
	 * Called when a hub becomes unavailable after being available.
	 *
	 * @param status the current status of the hub
	 */
	void onHubUnavailable(GridStatus status);
	
	/**
	 * Called when a hub's status is updated during monitoring.
	 * This includes both available and unavailable hubs.
	 *
	 * @param previousStatus the previous status (may be null for first check)
	 * @param currentStatus the current status
	 */
	default void onStatusUpdate(GridStatus previousStatus, GridStatus currentStatus)
	{
		// Default implementation does nothing
		// Implementations can override to handle all status updates
	}
	
	/**
	 * Called when a hub health check fails with an exception.
	 *
	 * @param hubUrl the hub URL that failed
	 * @param exception the exception that occurred
	 */
	default void onHealthCheckError(String hubUrl, Exception exception)
	{
		// Default implementation does nothing
		// Implementations can override to handle check errors
	}
}
