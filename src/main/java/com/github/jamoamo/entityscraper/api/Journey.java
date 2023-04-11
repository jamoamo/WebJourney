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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.reserved.log.LogMessage;
import com.github.jamoamo.entityscraper.reserved.log.LogSeverity;

/**
 *
 * @author James Amoore
 */
public final class Journey
{
	private IJourneyPath journeyPath;
	private IJourneyOptions journeyOptions = new DefaultJourneyOptions();
	
	/**
	 * Creates a new Web browsing journey.
	 * @return a Journey instance.
	 */
	public static Journey create()
	{
		return new Journey();
	}
	
	/**
	 * Use the provided path on the journey. Repeated calls will override the path.
	 * @param path the path to follow.
	 * @return this instance.
	 */
	public Journey followPath(IJourneyPath path)
	{
		this.journeyPath = path;
		return this;
	}
	
	/**
	 * Use the journey options on the journey.
	 * @param options the journey options.
	 * @return this instance.
	 */
	public Journey withOptions(IJourneyOptions options)
	{
		this.journeyOptions = options;
		return this;
	}
	
	/**
	 * Execute the journey.
	 */
	public void execute()
	{
		IJourneyContext context = new JourneyContext(this.journeyOptions);
		
		context.log(new LogMessage(LogSeverity.INFO, "Journey Starting."));
		
		journeyPath.followPath(context);
		
		context.getBrowser().exit();
		
		context.log(new LogMessage(LogSeverity.INFO, "Journey Ending."));
	}
}
