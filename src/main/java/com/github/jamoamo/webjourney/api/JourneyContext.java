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
package com.github.jamoamo.webjourney.api;

import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.reserved.log.LogMessage;
import com.github.jamoamo.webjourney.reserved.log.LogSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class JourneyContext implements IJourneyContext
{
	private final Logger logger = LoggerFactory.getLogger(JourneyContext.class);
	private final IJourneyOptions options;
	private final IBrowser browser;
	
	JourneyContext(IJourneyOptions options)
	{
		this.options = options;
		this.browser = this.options.getPreferredBrowserStrategy().getPreferredBrowser();
	}

	/**
		Log a message.
	 * @param message The message that needs to be logged.
	*/
	@Override
	public void log(LogMessage message)
	{
		String messageText = message.getMessage();
		switch(message.getSeverity())
		{
			case INFO: 
				this.logger.info(messageText); 
				break;
			case WARNING: 
				this.logger.warn(messageText); 
				break;
			case ERROR: 
				this.logger.error(messageText); 
				break;
			case TRACE: 
				this.logger.trace(messageText); 
				break;
			case DEBUG: 
				this.logger.debug(messageText); 
				break;
			default:
				throw new RuntimeException("The log message severity could not be determined.");
		}
	}
	
	
	@Override
	public IBrowser getBrowser()
	{
		return this.browser;
	}

	/**
	 * Wait for the default wait period. The wait period is determined from the journey options.
	 */
	@Override
	public void waitForDefault()
	{
		waitFor(this.options.getDefaultWait().toMillis());
	}
	
	/**
	 * Wait for the specified number of milliseconds.
	 * @param millis The number of milliseconds to wait
	 */
	@Override
	public void waitFor(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(InterruptedException ex)
		{
			log(new LogMessage(LogSeverity.WARNING, "Wait between steps was interrupted"));
		}
	}
	
}
