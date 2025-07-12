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
 * Linux-specific Chrome options strategy.
 * 
 * <p>
 * This strategy configures Chrome options specifically for Linux environments,
 * including options optimized for:
 * - Headless environments (Docker containers, CI/CD)
 * - Server environments without display
 * - GPU acceleration handling in containerized environments
 * </p>
 * 
 * @author James Amoore
 * @since 1.1.0
 */
public class LinuxChromeOptionsStrategy implements IChromeOptionsStrategy
{
	private static final String TARGET_OS = "Linux";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("checkstyle:MethodLength")
	public ChromeOptions configureChromeOptions(IBrowserOptions browserOptions)
	{
		ChromeOptions options = new ChromeOptions();
		
		// Common options for all platforms
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
		
		// Linux-specific options, especially for headless environments
		options = options.addArguments(
			"--disable-gpu"
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
