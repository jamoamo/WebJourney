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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 *
 * @author James Amoore
 */
public class WebTravellerTest
{

	/**
	 * Test of travelJourney method, of class WebTraveller.
	 */
	@Test
	public void testTravelJourney_emptyObservers()
	{
		System.out.println("travelJourney");
		IJourney journey = Mockito.mock(IJourney.class);
		IBrowser browser = Mockito.mock(IBrowser.class);
		IPreferredBrowserStrategy browserStrategy = Mockito.mock(IPreferredBrowserStrategy.class);
		Mockito.when(browserStrategy.getPreferredBrowser(ArgumentMatchers.any())).thenReturn(browser);
		ITravelOptions travelOptions = Mockito.mock(ITravelOptions.class);
		Mockito.when(travelOptions.getJourneyObservers()).thenReturn(new ArrayList<>());
		
		Mockito.when(travelOptions.getPreferredBrowserStrategy()).thenReturn(browserStrategy);
		WebTraveller instance = new WebTraveller(travelOptions);
		instance.travelJourney(journey);
		
		ArgumentCaptor<IJourneyContext> contextCaptor = ArgumentCaptor.forClass(IJourneyContext.class);
		Mockito.verify(journey).doJourney(contextCaptor.capture());
		Assertions.assertTrue(contextCaptor.getValue().getJourneyObservers().isEmpty());
		
		Mockito.verify(browser).exit();
	}
	
	@Test
	public void testTravelJourney_oneObserver()
	{
		System.out.println("travelJourney");
		IJourney journey = Mockito.mock(IJourney.class);
		IBrowser browser = Mockito.mock(IBrowser.class);
		IPreferredBrowserStrategy browserStrategy = Mockito.mock(IPreferredBrowserStrategy.class);
		Mockito.when(browserStrategy.getPreferredBrowser(ArgumentMatchers.any())).thenReturn(browser);
		ITravelOptions travelOptions = Mockito.mock(ITravelOptions.class);
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		Mockito.when(travelOptions.getJourneyObservers()).thenReturn(Collections.singletonList(observer));
		
		Mockito.when(travelOptions.getPreferredBrowserStrategy()).thenReturn(browserStrategy);
		WebTraveller instance = new WebTraveller(travelOptions);
		instance.travelJourney(journey);
		
		ArgumentCaptor<IJourneyContext> contextCaptor = ArgumentCaptor.forClass(IJourneyContext.class);
		Mockito.verify(journey).doJourney(contextCaptor.capture());
		Assertions.assertEquals(1, contextCaptor.getValue().getJourneyObservers().size());
		
		Mockito.verify(browser).exit();
	}
	
	@Test
	public void testTravelJourney_failed()
	{
		System.out.println("travelJourney");
		IJourney journey = Mockito.mock(IJourney.class);
		IBrowser browser = Mockito.mock(IBrowser.class);
		IPreferredBrowserStrategy browserStrategy = Mockito.mock(IPreferredBrowserStrategy.class);
		Mockito.when(browserStrategy.getPreferredBrowser(ArgumentMatchers.any())).thenReturn(browser);
		ITravelOptions travelOptions = Mockito.mock(ITravelOptions.class);
		Mockito.when(travelOptions.getJourneyObservers()).thenReturn(new ArrayList<>());
		
		Mockito.doThrow(new JourneyException("Journey Failed.")).when(journey).doJourney(ArgumentMatchers.any());
		
		Mockito.when(travelOptions.getPreferredBrowserStrategy()).thenReturn(browserStrategy);
		WebTraveller instance = new WebTraveller(travelOptions);
		JourneyException assertThrows =
			 Assertions.assertThrows(JourneyException.class, () -> instance.travelJourney(journey));
		
		Assertions.assertEquals("Journey Failed.", assertThrows.getMessage());
		
		Mockito.verify(browser).exit();
	}
}
