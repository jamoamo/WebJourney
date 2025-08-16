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

import java.util.Objects;

/**
 * Adapter that combines local browser options with hub configuration.
 * This class allows existing IBrowserOptions implementations to be used
 * with remote execution by wrapping them with hub configuration.
 * 
 * @author James Amoore
 */
final class RemoteBrowserOptionsAdapter implements IRemoteBrowserOptions
{
	private final IBrowserOptions localOptions;
	private final IHubConfiguration hubConfiguration;
	
	/**
	 * Creates a new adapter instance.
	 * 
	 * @param localOptions the local browser options to delegate to
	 * @param hubConfiguration the hub configuration for remote execution
	 */
	RemoteBrowserOptionsAdapter(IBrowserOptions localOptions, IHubConfiguration hubConfiguration)
	{
		this.localOptions = Objects.requireNonNull(localOptions, "Local options cannot be null");
		this.hubConfiguration = Objects.requireNonNull(hubConfiguration, "Hub configuration cannot be null");
	}
	
	@Override
	public boolean isHeadless()
	{
		return localOptions.isHeadless();
	}
	
	@Override
	public boolean acceptUnexpectedAlerts()
	{
		return localOptions.acceptUnexpectedAlerts();
	}
	
	@Override
	public IHubConfiguration getHubConfiguration()
	{
		return hubConfiguration;
	}
	
	@Override
	public boolean isRemoteExecution()
	{
		return hubConfiguration != null && hubConfiguration.isEnabled();
	}
	
	/**
	 * Gets the underlying local options instance.
	 * 
	 * @return the local browser options
	 */
	public IBrowserOptions getLocalOptions()
	{
		return localOptions;
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
		RemoteBrowserOptionsAdapter that = (RemoteBrowserOptionsAdapter) obj;
		return Objects.equals(localOptions, that.localOptions) &&
			   Objects.equals(hubConfiguration, that.hubConfiguration);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(localOptions, hubConfiguration);
	}
	
	@Override
	public String toString()
	{
		return "RemoteBrowserOptionsAdapter{" +
			   "localOptions=" + localOptions +
			   ", hubConfiguration=" + hubConfiguration +
			   '}';
	}
}
