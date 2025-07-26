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
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Strategy interface for configuring Chrome options based on operating system.
 * 
 * <p>
 * This interface defines the contract for different Chrome options strategies
 * that can be applied based on the operating system or environment requirements.
 * Each strategy implementation should provide Chrome options optimized for
 * a specific platform.
 * </p>
 * 
 * @author James Amoore
 * @since 1.1.0
 */
public interface IChromeOptionsStrategy
{
	/**
	 * Configures Chrome options for the specific operating system or environment.
	 * 
	 * @param browserOptions the browser options containing user preferences
	 * @return configured ChromeOptions instance
	 */
	ChromeOptions configureChromeOptions(IBrowserOptions browserOptions);
	
	/**
	 * Gets the name of the operating system this strategy is designed for.
	 * 
	 * @return the operating system name (e.g., "Windows", "Linux", "macOS")
	 */
	String getTargetOperatingSystem();
} 
