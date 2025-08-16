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

import java.time.Duration;

/**
 * Interface for monitoring the health and availability of Selenium Grid hubs.
 * Provides methods for checking hub status, registering listeners for health changes,
 * and managing continuous monitoring.
 * 
 * @author James Amoore
 */
public interface IGridHealthMonitor
{
	/**
	 * Checks if a Selenium Hub is currently available and responding.
	 * This method performs a synchronous health check.
	 *
	 * @param hubUrl the hub URL to check
	 * @return true if the hub is available, false otherwise
	 */
	boolean isHubAvailable(String hubUrl);
	
	/**
	 * Gets detailed status information for a hub.
	 *
	 * @param hubUrl the hub URL to check
	 * @return the hub status information
	 */
	GridStatus getHubStatus(String hubUrl);
	
	/**
	 * Registers a listener to be notified of health status changes.
	 *
	 * @param listener the health listener to register
	 */
	void registerHealthListener(GridHealthListener listener);
	
	/**
	 * Unregisters a previously registered health listener.
	 *
	 * @param listener the health listener to unregister
	 */
	void unregisterHealthListener(GridHealthListener listener);
	
	/**
	 * Starts continuous monitoring of registered hubs.
	 * Uses the default monitoring interval.
	 */
	void startMonitoring();
	
	/**
	 * Starts continuous monitoring with a specific interval.
	 *
	 * @param interval the monitoring interval
	 */
	void startMonitoring(Duration interval);
	
	/**
	 * Stops continuous monitoring.
	 */
	void stopMonitoring();
	
	/**
	 * Determines if monitoring is currently active.
	 *
	 * @return true if monitoring is active, false otherwise
	 */
	boolean isMonitoring();
	
	/**
	 * Adds a hub URL to the monitoring list.
	 *
	 * @param hubUrl the hub URL to monitor
	 */
	void addHub(String hubUrl);
	
	/**
	 * Removes a hub URL from the monitoring list.
	 *
	 * @param hubUrl the hub URL to stop monitoring
	 */
	void removeHub(String hubUrl);
}
