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

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.ICrumb;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 *
 * @author James Amoore
 */
public class SubJourneyTest
{
	
	public SubJourneyTest()
	{
	}

	/**
	 * Test of doJourney method, of class SubJourney.
	 */
	@Test
	public void testDoJourney()
	{
		List<AWebAction> actions = new ArrayList<>();
		AWebAction mockAction = Mockito.mock(AWebAction.class);
		Mockito.when(mockAction.executeAction(any())).thenReturn(ActionResult.SUCCESS);
		AWebAction mockAction2 = Mockito.mock(AWebAction.class);
		Mockito.when(mockAction2.executeAction(any())).thenReturn(ActionResult.SUCCESS);
		actions.add(mockAction);
		actions.add(mockAction2);
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		IJourneyBreadcrumb breadcrumb = Mockito.mock(IJourneyBreadcrumb.class);
		Mockito.when(context.getJourneyBreadcrumb()).thenReturn(breadcrumb);
		Mockito.when(context.getJourneyObservers()).thenReturn(Collections.singletonList(observer));
		SubJourney instance = new SubJourney(actions);
		instance.doJourney(context);
		
		ArgumentCaptor<IJourneyContext> contextCaptor = ArgumentCaptor.forClass(IJourneyContext.class);
		ArgumentCaptor<IJourneyContext> contextCaptor2 = ArgumentCaptor.forClass(IJourneyContext.class);
		Mockito.verify(mockAction).executeAction(contextCaptor.capture());
		assertSame(context, contextCaptor.getValue());
		Mockito.verify(mockAction2).executeAction(contextCaptor2.capture());
		assertSame(context, contextCaptor2.getValue());
		
		ArgumentCaptor<ICrumb> crumbCaptor = ArgumentCaptor.forClass(ICrumb.class);
		Mockito.verify(breadcrumb, times(3)).pushCrumb(crumbCaptor.capture());
		assertSame(instance, crumbCaptor.getAllValues().get(0));
		assertSame(mockAction, crumbCaptor.getAllValues().get(1));
		assertSame(mockAction2, crumbCaptor.getAllValues().get(2));
		
		Mockito.verify(breadcrumb, times(3)).popCrumb();
		
		ArgumentCaptor<AWebAction> startedActioncaptor = ArgumentCaptor.forClass(AWebAction.class);
		ArgumentCaptor<AWebAction> endedActioncaptor = ArgumentCaptor.forClass(AWebAction.class);
		Mockito.verify(observer, times(2)).actionStarted(startedActioncaptor.capture());
		Mockito.verify(observer, times(2)).actionEnded(endedActioncaptor.capture());
		assertSame(mockAction, startedActioncaptor.getAllValues().get(0));
		assertSame(mockAction2, startedActioncaptor.getAllValues().get(1));
		assertSame(mockAction, endedActioncaptor.getAllValues().get(0));
		assertSame(mockAction2, endedActioncaptor.getAllValues().get(1));	
	}
	
	/**
	 * Test of doJourney method, of class SubJourney.
	 */
	@Test
	public void testDoJourney_firstFailed()
	{
		List<AWebAction> actions = new ArrayList<>();
		AWebAction mockAction = Mockito.mock(AWebAction.class);
		Mockito.when(mockAction.executeAction(any())).thenReturn(ActionResult.FAILURE);
		AWebAction mockAction2 = Mockito.mock(AWebAction.class);
		Mockito.when(mockAction2.executeAction(any())).thenReturn(ActionResult.SUCCESS);
		actions.add(mockAction);
		actions.add(mockAction2);
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		IJourneyBreadcrumb breadcrumb = Mockito.mock(IJourneyBreadcrumb.class);
		Mockito.when(context.getJourneyBreadcrumb()).thenReturn(breadcrumb);
		Mockito.when(context.getJourneyObservers()).thenReturn(Collections.singletonList(observer));
		SubJourney instance = new SubJourney(actions);
		JourneyException assertThrows = assertThrows(JourneyException.class, () -> instance.doJourney(context));
		assertTrue(assertThrows.getMessage().startsWith("Action failed: "));
		
		ArgumentCaptor<IJourneyContext> contextCaptor = ArgumentCaptor.forClass(IJourneyContext.class);
		ArgumentCaptor<IJourneyContext> contextCaptor2 = ArgumentCaptor.forClass(IJourneyContext.class);
		Mockito.verify(mockAction).executeAction(contextCaptor.capture());
		assertSame(context, contextCaptor.getValue());
		Mockito.verify(mockAction2, never()).executeAction(contextCaptor2.capture());
		
		ArgumentCaptor<ICrumb> crumbCaptor = ArgumentCaptor.forClass(ICrumb.class);
		Mockito.verify(breadcrumb, times(2)).pushCrumb(crumbCaptor.capture());
		assertSame(instance, crumbCaptor.getAllValues().get(0));
		assertSame(mockAction, crumbCaptor.getAllValues().get(1));
		
		Mockito.verify(breadcrumb, times(2)).popCrumb();
		
		ArgumentCaptor<AWebAction> startedActioncaptor = ArgumentCaptor.forClass(AWebAction.class);
		ArgumentCaptor<AWebAction> endedActioncaptor = ArgumentCaptor.forClass(AWebAction.class);
		Mockito.verify(observer, times(1)).actionStarted(startedActioncaptor.capture());
		Mockito.verify(observer, times(1)).actionEnded(endedActioncaptor.capture());
	}
}
