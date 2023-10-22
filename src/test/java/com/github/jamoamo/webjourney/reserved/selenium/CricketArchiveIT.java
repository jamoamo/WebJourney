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
package com.github.jamoamo.webjourney.reserved.selenium;

import com.github.jamoamo.webjourney.LoginForm;
import com.github.jamoamo.webjourney.Entity;
import com.github.jamoamo.webjourney.InputForm;
import com.github.jamoamo.webjourney.JourneyBuilder;
import com.github.jamoamo.webjourney.TravelOptions;
import com.github.jamoamo.webjourney.WebJourney;
import com.github.jamoamo.webjourney.WebTraveller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author James Amoore
 */
public class CricketArchiveIT
{
	@Test
	public void test() throws Exception
	{
		LoginForm loginForm = new LoginForm("amoore.james@gmail.com", "J8a7m1e0s7ca");
		
		WebJourney journey = JourneyBuilder.path()
			.navigateTo("https://my.cricketarchive.com")
			.completeFormAndSubmit(loginForm)
			.navigateTo("https://cricketarchive.com/cgi-bin/ask_the_scorecard_oracle.cgi")
			.completeFormAndSubmit(new InputForm(1))
			.consumePage(Entity.class, (c -> 
					  Assertions.assertEquals("James Lillywhite's XI in Australia and New Zealand 1876/77 (1st Test)", 
													  c.getTestName())
					  )
			)
			.build();
		
		WebTraveller traveller = new WebTraveller(new TravelOptions());
		traveller.travelJourney(journey);
	}
}
