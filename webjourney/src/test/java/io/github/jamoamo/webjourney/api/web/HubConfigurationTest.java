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

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HubConfiguration.
 * 
 * @author James Amoore
 */
class HubConfigurationTest
{
	@Test
	void testBuilderWithValidUrl()
	{
		String hubUrl = "http://selenium-hub:4444/wd/hub";
		HubConfiguration config = HubConfiguration.builder()
			.withUrl(hubUrl)
			.build();
		
		assertEquals(hubUrl, config.getHubUrl());
		assertTrue(config.isEnabled());
		assertEquals(Duration.ofSeconds(30), config.getConnectionTimeout());
		assertEquals(Duration.ofMinutes(10), config.getSessionTimeout());
		assertEquals(3, config.getMaxRetries());
		assertEquals(Duration.ofSeconds(2), config.getRetryDelay());
		assertTrue(config.getCustomCapabilities().isEmpty());
	}
	
	@Test
	void testBuilderWithCustomValues()
	{
		String hubUrl = "https://secure-hub:4444/wd/hub";
		Duration connectionTimeout = Duration.ofSeconds(45);
		Duration sessionTimeout = Duration.ofMinutes(15);
		int maxRetries = 5;
		Duration retryDelay = Duration.ofSeconds(3);
		
		HubConfiguration config = HubConfiguration.builder()
			.withUrl(hubUrl)
			.withConnectionTimeout(connectionTimeout)
			.withSessionTimeout(sessionTimeout)
			.withMaxRetries(maxRetries)
			.withRetryDelay(retryDelay)
			.withCustomCapability("enableVNC", true)
			.withCustomCapability("version", "latest")
			.withEnabled(true)
			.build();
		
		assertEquals(hubUrl, config.getHubUrl());
		assertEquals(connectionTimeout, config.getConnectionTimeout());
		assertEquals(sessionTimeout, config.getSessionTimeout());
		assertEquals(maxRetries, config.getMaxRetries());
		assertEquals(retryDelay, config.getRetryDelay());
		assertTrue(config.isEnabled());
		
		Map<String, Object> capabilities = config.getCustomCapabilities();
		assertEquals(2, capabilities.size());
		assertEquals(true, capabilities.get("enableVNC"));
		assertEquals("latest", capabilities.get("version"));
	}
	
	@Test
	void testBuilderWithNullUrl()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			HubConfiguration.builder()
				.withUrl(null)
				.build();
		});
	}
	
	@Test
	void testBuilderWithEmptyUrl()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			HubConfiguration.builder()
				.withUrl("")
				.build();
		});
	}
	
	@Test
	void testBuilderWithoutUrlWhenEnabled()
	{
		assertThrows(IllegalStateException.class, () -> {
			HubConfiguration.builder()
				.withEnabled(true)
				.build();
		});
	}
	
	@Test
	void testBuilderWithoutUrlWhenDisabled()
	{
		HubConfiguration config = HubConfiguration.builder()
			.withEnabled(false)
			.build();
		
		assertNull(config.getHubUrl());
		assertFalse(config.isEnabled());
	}
	
	@Test
	void testBuilderWithNegativeTimeout()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			HubConfiguration.builder()
				.withUrl("http://hub:4444/wd/hub")
				.withConnectionTimeout(Duration.ofSeconds(-1))
				.build();
		});
	}
	
	@Test
	void testBuilderWithNegativeRetries()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			HubConfiguration.builder()
				.withUrl("http://hub:4444/wd/hub")
				.withMaxRetries(-1)
				.build();
		});
	}
	
	@Test
	void testBuilderWithZeroRetries()
	{
		HubConfiguration config = HubConfiguration.builder()
			.withUrl("http://hub:4444/wd/hub")
			.withMaxRetries(0)
			.build();
		
		assertEquals(0, config.getMaxRetries());
	}
	
	@Test
	void testEqualsAndHashCode()
	{
		HubConfiguration config1 = HubConfiguration.builder()
			.withUrl("http://hub:4444/wd/hub")
			.withCustomCapability("test", "value")
			.build();
		
		HubConfiguration config2 = HubConfiguration.builder()
			.withUrl("http://hub:4444/wd/hub")
			.withCustomCapability("test", "value")
			.build();
		
		assertEquals(config1, config2);
		assertEquals(config1.hashCode(), config2.hashCode());
	}
	
	@Test
	void testToString()
	{
		HubConfiguration config = HubConfiguration.builder()
			.withUrl("http://hub:4444/wd/hub")
			.build();
		
		String toString = config.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("http://hub:4444/wd/hub"));
		assertTrue(toString.contains("HubConfiguration"));
	}
}
