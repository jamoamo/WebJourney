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
import io.github.jamoamo.webjourney.api.ITravelOptions;
import java.time.Duration;

/**
 * Resolves the effective wait to apply when reading an element.
 *
 * <p>A field may specify its own wait. When it does not (a negative value), the default configured on the
 * journey's {@link ITravelOptions#getElementWaitTimeout() travel options} is used. When neither is available no
 * wait is applied.
 *
 * @author James Amoore
 */
final class ElementWaitResolver
{
	private ElementWaitResolver()
	{
	}

	/**
	 * Resolves the wait to apply for an element read.
	 *
	 * @param waitSeconds the field level wait in seconds. A negative value inherits the global default.
	 * @param context     the entity creation context, providing access to the global default. May be {@code null}.
	 *
	 * @return the wait to apply. Never {@code null} and never negative.
	 */
	static Duration resolve(long waitSeconds, EntityCreationContext context)
	{
		if(waitSeconds >= 0)
		{
			return Duration.ofSeconds(waitSeconds);
		}
		return resolveGlobalDefault(context);
	}

	private static Duration resolveGlobalDefault(EntityCreationContext context)
	{
		if(context == null)
		{
			return Duration.ZERO;
		}
		IJourneyContext journeyContext = context.getJourneyContext();
		if(journeyContext == null)
		{
			return Duration.ZERO;
		}
		ITravelOptions options = journeyContext.getOptions();
		if(options == null)
		{
			return Duration.ZERO;
		}
		Duration timeout = options.getElementWaitTimeout();
		if(timeout == null || timeout.isNegative())
		{
			return Duration.ZERO;
		}
		return timeout;
	}
}
