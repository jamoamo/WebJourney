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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating Chrome options strategies based on the operating system.
 * 
 * <p>
 * This factory analyzes the current operating system and returns the most
 * appropriate Chrome options strategy to handle platform-specific requirements
 * and optimizations.
 * </p>
 * 
 * <p>
 * The factory detects the following operating systems:
 * - Windows: Uses WindowsChromeOptionsStrategy
 * - Linux: Uses LinuxChromeOptionsStrategy  
 * - macOS and others: Uses DefaultChromeOptionsStrategy
 * </p>
 * 
 * @author James Amoore
 * @since 1.1.0
 */
public final class ChromeOptionsStrategyFactory
{
	private static final String WINDOWS_OS_NAME = "windows";
	private static final String LINUX_OS_NAME = "linux";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChromeOptionsStrategyFactory.class);
	
	private ChromeOptionsStrategyFactory()
	{
		// Private constructor to prevent instantiation
	}

	/**
	 * Creates the appropriate Chrome options strategy for the current operating system.
	 * 
	 * @return ChromeOptionsStrategy instance optimized for the current OS
	 */
	public static IChromeOptionsStrategy createStrategy()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		LOGGER.debug("Detected operating system: {}", osName);
		
		IChromeOptionsStrategy strategy;
		
		if (osName.contains(WINDOWS_OS_NAME)) 
		{
			strategy = new WindowsChromeOptionsStrategy();
			LOGGER.debug("Selected Windows Chrome options strategy");
		} 
		else if (osName.contains(LINUX_OS_NAME)) 
		{
			strategy = new LinuxChromeOptionsStrategy();
			LOGGER.debug("Selected Linux Chrome options strategy");
		} 
		else 
		{
			strategy = new DefaultChromeOptionsStrategy();
			LOGGER.debug("Selected default Chrome options strategy for OS: {}", osName);
		}
		
		return strategy;
	}
	
	/**
	 * Creates a Chrome options strategy for a specific operating system.
	 * This method is primarily used for testing purposes.
	 * 
	 * @param osName the operating system name to create a strategy for
	 * @return ChromeOptionsStrategy instance for the specified OS
	 */
	public static IChromeOptionsStrategy createStrategy(String osName)
	{
		String normalizedOsName = osName.toLowerCase();
		LOGGER.debug("Creating strategy for specified OS: {}", normalizedOsName);
		
		IChromeOptionsStrategy strategy;
		
		if (normalizedOsName.contains(WINDOWS_OS_NAME)) 
		{
			strategy = new WindowsChromeOptionsStrategy();
		} 
		else if (normalizedOsName.contains(LINUX_OS_NAME)) 
		{
			strategy = new LinuxChromeOptionsStrategy();
		} 
		else 
		{
			strategy = new DefaultChromeOptionsStrategy();
		}
		
		return strategy;
	}
} 
