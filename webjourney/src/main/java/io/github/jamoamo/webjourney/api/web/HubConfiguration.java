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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link IHubConfiguration}.
 * Provides configuration settings for Selenium Hub connectivity with sensible defaults.
 * 
 * @author James Amoore
 */
public final class HubConfiguration implements IHubConfiguration
{
	private final String hubUrl;
	private final Duration connectionTimeout;
	private final Duration sessionTimeout;
	private final int maxRetries;
	private final Duration retryDelay;
	private final Map<String, Object> customCapabilities;
	private final boolean enabled;
	
	/**
	 * Private constructor for builder pattern.
	 */
	private HubConfiguration(Builder builder)
	{
		this.hubUrl = builder.hubUrl;
		this.connectionTimeout = builder.connectionTimeout;
		this.sessionTimeout = builder.sessionTimeout;
		this.maxRetries = builder.maxRetries;
		this.retryDelay = builder.retryDelay;
		this.customCapabilities = Collections.unmodifiableMap(new HashMap<>(builder.customCapabilities));
		this.enabled = builder.enabled;
	}
	
	@Override
	public String getHubUrl()
	{
		return hubUrl;
	}
	
	@Override
	public Duration getConnectionTimeout()
	{
		return connectionTimeout;
	}
	
	@Override
	public Duration getSessionTimeout()
	{
		return sessionTimeout;
	}
	
	@Override
	public int getMaxRetries()
	{
		return maxRetries;
	}
	
	@Override
	public Duration getRetryDelay()
	{
		return retryDelay;
	}
	
	@Override
	public Map<String, Object> getCustomCapabilities()
	{
		return customCapabilities;
	}
	
	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Creates a new builder for hub configuration.
	 * 
	 * @return a new builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}
	
	/**
	 * Builder class for creating HubConfiguration instances.
	 */
	public static final class Builder
	{
		private String hubUrl;
		private Duration connectionTimeout = Duration.ofSeconds(30);
		private Duration sessionTimeout = Duration.ofMinutes(10);
		private int maxRetries = 3;
		private Duration retryDelay = Duration.ofSeconds(2);
		private final Map<String, Object> customCapabilities = new HashMap<>();
		private boolean enabled = true;
		
		private Builder()
		{
			// Private constructor for builder pattern
		}
		
		/**
		 * Sets the hub URL.
		 * 
		 * @param url the Selenium Hub URL
		 * @return this builder instance
		 * @throws IllegalArgumentException if url is null or empty
		 */
		public Builder withUrl(String url)
		{
			if (url == null || url.trim().isEmpty())
			{
				throw new IllegalArgumentException("Hub URL cannot be null or empty");
			}
			this.hubUrl = url.trim();
			return this;
		}
		
		/**
		 * Sets the connection timeout.
		 * 
		 * @param timeout the connection timeout duration
		 * @return this builder instance
		 * @throws IllegalArgumentException if timeout is null or negative
		 */
		public Builder withConnectionTimeout(Duration timeout)
		{
			if (timeout == null || timeout.isNegative())
			{
				throw new IllegalArgumentException("Connection timeout must be positive");
			}
			this.connectionTimeout = timeout;
			return this;
		}
		
		/**
		 * Sets the session timeout.
		 * 
		 * @param timeout the session timeout duration
		 * @return this builder instance
		 * @throws IllegalArgumentException if timeout is null or negative
		 */
		public Builder withSessionTimeout(Duration timeout)
		{
			if (timeout == null || timeout.isNegative())
			{
				throw new IllegalArgumentException("Session timeout must be positive");
			}
			this.sessionTimeout = timeout;
			return this;
		}
		
		/**
		 * Sets the maximum retry attempts.
		 * 
		 * @param retries the maximum number of retry attempts
		 * @return this builder instance
		 * @throws IllegalArgumentException if retries is negative
		 */
		public Builder withMaxRetries(int retries)
		{
			if (retries < 0)
			{
				throw new IllegalArgumentException("Max retries cannot be negative");
			}
			this.maxRetries = retries;
			return this;
		}
		
		/**
		 * Sets the retry delay.
		 * 
		 * @param delay the delay between retry attempts
		 * @return this builder instance
		 * @throws IllegalArgumentException if delay is null or negative
		 */
		public Builder withRetryDelay(Duration delay)
		{
			if (delay == null || delay.isNegative())
			{
				throw new IllegalArgumentException("Retry delay must be positive");
			}
			this.retryDelay = delay;
			return this;
		}
		
		/**
		 * Adds a custom capability.
		 * 
		 * @param key the capability name
		 * @param value the capability value
		 * @return this builder instance
		 * @throws IllegalArgumentException if key is null or empty
		 */
		public Builder withCustomCapability(String key, Object value)
		{
			if (key == null || key.trim().isEmpty())
			{
				throw new IllegalArgumentException("Capability key cannot be null or empty");
			}
			this.customCapabilities.put(key.trim(), value);
			return this;
		}
		
		/**
		 * Adds multiple custom capabilities.
		 * 
		 * @param capabilities a map of capabilities to add
		 * @return this builder instance
		 * @throws IllegalArgumentException if capabilities is null
		 */
		public Builder withCustomCapabilities(Map<String, Object> capabilities)
		{
			if (capabilities == null)
			{
				throw new IllegalArgumentException("Capabilities map cannot be null");
			}
			this.customCapabilities.putAll(capabilities);
			return this;
		}
		
		/**
		 * Sets whether hub connectivity is enabled.
		 * 
		 * @param enabled true to enable hub connectivity, false to disable
		 * @return this builder instance
		 */
		public Builder withEnabled(boolean enabled)
		{
			this.enabled = enabled;
			return this;
		}
		
		/**
		 * Builds the hub configuration.
		 * 
		 * @return a new HubConfiguration instance
		 * @throws IllegalStateException if required fields are not set
		 */
		public HubConfiguration build()
		{
			if (enabled && (hubUrl == null || hubUrl.trim().isEmpty()))
			{
				throw new IllegalStateException("Hub URL is required when hub is enabled");
			}
			return new HubConfiguration(this);
		}
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
		HubConfiguration that = (HubConfiguration) obj;
		return maxRetries == that.maxRetries &&
			   enabled == that.enabled &&
			   Objects.equals(hubUrl, that.hubUrl) &&
			   Objects.equals(connectionTimeout, that.connectionTimeout) &&
			   Objects.equals(sessionTimeout, that.sessionTimeout) &&
			   Objects.equals(retryDelay, that.retryDelay) &&
			   Objects.equals(customCapabilities, that.customCapabilities);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(hubUrl, connectionTimeout, sessionTimeout, maxRetries, 
						   retryDelay, customCapabilities, enabled);
	}
	
	@Override
	public String toString()
	{
		return "HubConfiguration{" +
			   "hubUrl='" + hubUrl + '\'' +
			   ", connectionTimeout=" + connectionTimeout +
			   ", sessionTimeout=" + sessionTimeout +
			   ", maxRetries=" + maxRetries +
			   ", retryDelay=" + retryDelay +
			   ", customCapabilities=" + customCapabilities +
			   ", enabled=" + enabled +
			   '}';
	}
}
