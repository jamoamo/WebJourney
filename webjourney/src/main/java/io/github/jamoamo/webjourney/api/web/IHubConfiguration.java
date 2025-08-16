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
import java.util.Map;

/**
 * Configuration interface for Selenium Hub connectivity.
 * Provides settings for connecting to remote Selenium Grid infrastructure.
 * 
 * @author James Amoore
 */
public interface IHubConfiguration
{
	/**
	 * Gets the Selenium Hub URL.
	 * 
	 * @return the hub URL (e.g., "http://selenium-hub:4444/wd/hub")
	 */
	String getHubUrl();
	
	/**
	 * Gets the connection timeout for hub connectivity.
	 * 
	 * @return the connection timeout duration
	 */
	Duration getConnectionTimeout();
	
	/**
	 * Gets the session timeout for browser sessions.
	 * 
	 * @return the session timeout duration
	 */
	Duration getSessionTimeout();
	
	/**
	 * Gets the maximum number of retry attempts for failed connections.
	 * 
	 * @return the maximum retry attempts
	 */
	int getMaxRetries();
	
	/**
	 * Gets the delay between retry attempts.
	 * 
	 * @return the retry delay duration
	 */
	Duration getRetryDelay();
	
	/**
	 * Gets custom capabilities to be added to browser sessions.
	 * 
	 * @return a map of custom capability names to values
	 */
	Map<String, Object> getCustomCapabilities();
	
	/**
	 * Determines if hub connectivity is enabled.
	 * 
	 * @return true if hub connectivity should be used, false otherwise
	 */
	boolean isEnabled();
}
