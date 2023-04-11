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

import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 *
 * @author James Amoore
 */
public class LoginTest
{
	@Test
	public void test() throws Exception
	{
		LoginForm loginForm = new LoginForm("amoore.james@gmail.com", "J8a7m1e0s7ca");
		
		 JourneyPath path = JourneyPath.start()
			.navigateTo(Navigate.toUrl("https://my.cricketarchive.com"))
			 .fillForm(FormFill.fillForm(loginForm))
			 .repeat(
				  RepeatedPath.forEach(Arrays.stream(new Integer[]{1}).map(i -> new InputForm(i)))
				  .withDelay(Duration.ofSeconds(5))
				  .navigateTo(Navigate.toUrl("https://cricketarchive.com/cgi-bin/ask_the_scorecard_oracle.cgi"))
				  .waitFor(WaitFor.forDuration(Duration.ofSeconds(5)))
				  .click(ButtonClick.clickButtonByXPath("//a[@data-cc-event='click:dismiss']"))
				  .fillFormFromInput()
				  .scrape(
						ScrapePage.forEntity(Entity.class)
						.consumeWith(c -> {System.out.println(((Entity)c).getTestName());})
				  )
			 );
		
		Journey journey = Journey.create()
				  .followPath(path);
				  
			
		
		journey.execute();
	}
}
