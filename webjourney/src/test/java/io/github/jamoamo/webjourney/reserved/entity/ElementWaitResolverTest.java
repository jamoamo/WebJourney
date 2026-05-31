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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IRetryPolicy;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ElementWaitResolverTest
{
	 @Test
	 public void testResolve_fieldWaitTakesPrecedence()
	 {
		  EntityCreationContext context = contextWithGlobalWait(Duration.ofSeconds(30));
		  assertEquals(Duration.ofSeconds(5), ElementWaitResolver.resolve(5, context));
	 }

	 @Test
	 public void testResolve_fieldWaitZeroDisablesWait()
	 {
		  EntityCreationContext context = contextWithGlobalWait(Duration.ofSeconds(30));
		  assertEquals(Duration.ZERO, ElementWaitResolver.resolve(0, context));
	 }

	 @Test
	 public void testResolve_negativeFieldWaitInheritsGlobalDefault()
	 {
		  EntityCreationContext context = contextWithGlobalWait(Duration.ofSeconds(7));
		  assertEquals(Duration.ofSeconds(7), ElementWaitResolver.resolve(-1, context));
	 }

	 @Test
	 public void testResolve_nullContextResolvesToZero()
	 {
		  assertEquals(Duration.ZERO, ElementWaitResolver.resolve(-1, null));
	 }

	 @Test
	 public void testResolve_nullJourneyContextResolvesToZero()
	 {
		  EntityCreationContext context =
				new EntityCreationContext(Mockito.mock(EntityDefn.class), Mockito.mock(IRetryPolicy.class), null);
		  assertEquals(Duration.ZERO, ElementWaitResolver.resolve(-1, context));
	 }

	 @Test
	 public void testResolve_nullGlobalDefaultResolvesToZero()
	 {
		  EntityCreationContext context = contextWithGlobalWait(null);
		  assertEquals(Duration.ZERO, ElementWaitResolver.resolve(-1, context));
	 }

	 private static EntityCreationContext contextWithGlobalWait(Duration globalWait)
	 {
		  ITravelOptions options = Mockito.mock(ITravelOptions.class);
		  Mockito.when(options.getElementWaitTimeout()).thenReturn(globalWait);
		  IJourneyContext journeyContext = Mockito.mock(IJourneyContext.class);
		  Mockito.when(journeyContext.getOptions()).thenReturn(options);
		  return new EntityCreationContext(
				Mockito.mock(EntityDefn.class), Mockito.mock(IRetryPolicy.class), journeyContext);
	 }
}
