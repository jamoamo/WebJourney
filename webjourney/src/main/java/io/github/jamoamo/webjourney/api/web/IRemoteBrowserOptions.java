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
 * Extension of IBrowserOptions that includes remote/hub configuration.
 * Allows combining local browser options with remote execution settings.
 * 
 * @author James Amoore
 */
public interface IRemoteBrowserOptions extends IBrowserOptions
{
	/**
	 * Gets the hub configuration for remote execution.
	 * 
	 * @return the hub configuration, or null if remote execution is not configured
	 */
	IHubConfiguration getHubConfiguration();
	
	/**
	 * Determines if this configuration is for remote execution.
	 * 
	 * @return true if remote execution is configured and enabled, false otherwise
	 */
	boolean isRemoteExecution();
	
	/**
	 * Creates a remote browser options instance that combines local options with hub configuration.
	 * 
	 * @param localOptions the local browser options to extend
	 * @param hubConfig the hub configuration for remote execution
	 * @return a new IRemoteBrowserOptions instance
	 * @throws IllegalArgumentException if localOptions or hubConfig is null
	 */
	static IRemoteBrowserOptions remote(IBrowserOptions localOptions, IHubConfiguration hubConfig)
	{
		if (localOptions == null)
		{
			throw new IllegalArgumentException("Local options cannot be null");
		}
		if (hubConfig == null)
		{
			throw new IllegalArgumentException("Hub configuration cannot be null");
		}
		return new RemoteBrowserOptionsAdapter(localOptions, hubConfig);
	}
}
