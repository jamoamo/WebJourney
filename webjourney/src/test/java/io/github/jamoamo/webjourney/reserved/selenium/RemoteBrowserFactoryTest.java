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

import io.github.jamoamo.webjourney.api.web.HubConfiguration;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.http.ClientConfig;

/**
 * Tests for {@link RemoteBrowserFactory}.
 *
 * @author James Amoore
 */
public class RemoteBrowserFactoryTest
{
	@Test
	public void testCreateClientConfig_appliesHubTimeouts() throws Exception
	{
		HubConfiguration hubConfiguration = HubConfiguration.builder()
			.withUrl("http://selenium-hub:4444/wd/hub")
			.withConnectionTimeout(Duration.ofSeconds(15))
			.withCommandTimeout(Duration.ofSeconds(60))
			.build();
		RemoteChromeBrowserFactory factory = new RemoteChromeBrowserFactory(hubConfiguration);
		URL url = new URL(hubConfiguration.getHubUrl());

		ClientConfig config = factory.createClientConfig(url);

		Assertions.assertEquals(url, config.baseUrl());
		Assertions.assertEquals(Duration.ofSeconds(15), config.connectionTimeout());
		Assertions.assertEquals(Duration.ofSeconds(60), config.readTimeout());
	}

	@Test
	public void testCreateClientConfig_defaultsCommandTimeoutToSeleniumDefault() throws Exception
	{
		HubConfiguration hubConfiguration = HubConfiguration.builder()
			.withUrl("http://selenium-hub:4444/wd/hub")
			.build();
		RemoteChromeBrowserFactory factory = new RemoteChromeBrowserFactory(hubConfiguration);

		ClientConfig config = factory.createClientConfig(new URL(hubConfiguration.getHubUrl()));

		Assertions.assertEquals(ClientConfig.defaultConfig().readTimeout(), config.readTimeout());
	}
}
