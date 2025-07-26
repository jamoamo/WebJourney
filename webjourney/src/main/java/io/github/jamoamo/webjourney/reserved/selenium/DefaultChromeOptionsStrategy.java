/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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

import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Default Chrome options strategy for macOS and other operating systems.
 * 
 * <p>
 * This strategy provides a conservative set of Chrome options that should work
 * across most operating systems. It serves as the fallback strategy for 
 * operating systems that don't have specific optimization requirements.
 * </p>
 * 
 * <p>
 * This strategy is designed to be safe and compatible with:
 * - macOS
 * - BSD variants  
 * - Other Unix-like systems
 * - Any operating system not explicitly handled by specific strategies
 * </p>
 * 
 * @author James Amoore
 * @since 1.1.0
 */
public class DefaultChromeOptionsStrategy implements IChromeOptionsStrategy
{
	private static final String TARGET_OS = "Default";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("checkstyle:MethodLength")
	public ChromeOptions configureChromeOptions(IBrowserOptions browserOptions)
	{
		ChromeOptions options = new ChromeOptions();
		
		// Conservative options that should work on most platforms
		options = options.addArguments(
			"--no-sandbox", 
			"--remote-allow-origins=*", 
			"--disable-dev-shm-usage",
			"--disable-extensions",
			"--disable-background-timer-throttling",
			"--disable-backgrounding-occluded-windows",
			"--disable-renderer-backgrounding",
			"--window-size=1920,1080"
		);
		
		// Configure headless mode if requested
		if (browserOptions.isHeadless()) 
		{
			options = options.addArguments("--headless=new");
		}
		
		// Configure alert behavior
		if (browserOptions.acceptUnexpectedAlerts()) 
		{
			options = (ChromeOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
		} 
		else 
		{
			options = (ChromeOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
		}
		
		return options;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTargetOperatingSystem()
	{
		return TARGET_OS;
	}
} 
