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

import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing connections to Selenium Hub.
 * Provides methods for checking hub availability, validating URLs,
 * and managing connection parameters.
 * 
 * @author James Amoore
 */
public final class HubConnectionUtils
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HubConnectionUtils.class);
	private static final int DEFAULT_CONNECT_TIMEOUT = 5000; // 5 seconds
	private static final int DEFAULT_READ_TIMEOUT = 5000; // 5 seconds
	
	private HubConnectionUtils()
	{
		// Utility class - no instances
	}
	
	/**
	 * Checks if a Selenium Hub is available and responding.
	 * This method attempts to connect to the hub's status endpoint
	 * to verify availability.
	 *
	 * @param hubUrl the hub URL to check
	 * @return true if the hub is available, false otherwise
	 */
	public static boolean isHubAvailable(String hubUrl)
	{
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			return false;
		}
		
		try
		{
			String statusUrl = convertToStatusUrl(hubUrl);
			java.net.URL url = new java.net.URL(statusUrl);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
			connection.setInstanceFollowRedirects(true);
			
			int responseCode = connection.getResponseCode();
			boolean available = responseCode == HttpURLConnection.HTTP_OK;
			
			LOGGER.debug("Hub availability check: {} -> {} (HTTP {})", 
						statusUrl, available ? "AVAILABLE" : "UNAVAILABLE", responseCode);
			
			return available;
		}
		catch (Exception e)
		{
			LOGGER.debug("Hub availability check failed: {} -> UNAVAILABLE ({})", 
						hubUrl, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Validates that a hub URL is well-formed and accessible.
	 *
	 * @param hubUrl the hub URL to validate
	 * @throws IllegalArgumentException if the URL is invalid
	 */
	public static void validateHubUrl(String hubUrl)
	{
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			throw new IllegalArgumentException("Hub URL cannot be null or empty");
		}
		
		try
		{
			java.net.URL url = new java.net.URL(hubUrl);
			
			// Check protocol
			String protocol = url.getProtocol();
			if (!"http".equals(protocol) && !"https".equals(protocol))
			{
				throw new IllegalArgumentException("Hub URL must use HTTP or HTTPS protocol: " + hubUrl);
			}
			
			// Check host
			String host = url.getHost();
			if (host == null || host.trim().isEmpty())
			{
				throw new IllegalArgumentException("Hub URL must specify a valid host: " + hubUrl);
			}
			
			// Check path
			String path = url.getPath();
			if (!path.contains("/wd/hub"))
			{
				LOGGER.warn("Hub URL does not contain expected '/wd/hub' path: {}", hubUrl);
			}
		}
		catch (IllegalArgumentException e)
		{
			throw e;
		}
		catch (java.net.MalformedURLException e)
		{
			throw new IllegalArgumentException("Invalid hub URL: " + hubUrl, e);
		}
	}
	
	/**
	 * Converts a hub URL to its corresponding status endpoint URL.
	 * For example: http://hub:4444/wd/hub -> http://hub:4444/status
	 *
	 * @param hubUrl the hub URL
	 * @return the status endpoint URL
	 */
	private static String convertToStatusUrl(String hubUrl)
	{
		try
		{
			java.net.URL url = new java.net.URL(hubUrl);
			String baseUrl = url.getProtocol() + "://" + url.getHost();
			
			if (url.getPort() != -1)
			{
				baseUrl += ":" + url.getPort();
			}
			
			return baseUrl + "/status";
		}
		catch (java.net.MalformedURLException e)
		{
			// Fallback: try to replace /wd/hub with /status
			if (hubUrl.endsWith("/wd/hub"))
			{
				return hubUrl.substring(0, hubUrl.length() - 7) + "/status";
			}
			
			// Last resort: append /status
			return hubUrl + (hubUrl.endsWith("/") ? "status" : "/status");
		}
	}
}
