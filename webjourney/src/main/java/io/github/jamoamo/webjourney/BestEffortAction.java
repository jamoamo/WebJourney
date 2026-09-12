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
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import org.apache.commons.lang3.function.FailableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sub-journey that is always attempted, but whose failure does not abort the rest of the journey --
 * unlike {@link ConditionalAction}, which rethrows any exception from its sub-journey. Intended for
 * steps that are worth attempting on every run (e.g. logging in) but whose own unreliability shouldn't
 * take down journeys that don't strictly depend on them succeeding.
 *
 * @author James Amoore
 */
class BestEffortAction extends AWebAction
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BestEffortAction.class);

	private final FailableFunction<IJourneyBuilder, IJourney, ? extends JourneyException> subJourneySupplier;

	BestEffortAction(FailableFunction<IJourneyBuilder, IJourney, ? extends JourneyException> subJourneySupplier)
	{
		this.subJourneySupplier = subJourneySupplier;
	}

	@Override
	@SuppressWarnings("IllegalCatch")
	protected ActionResult executeActionImpl(IJourneyContext context)
	{
		try
		{
			BaseJourneyBuilder builder = JourneyBuilder.path();
			IJourney journey = this.subJourneySupplier.apply(builder);
			journey.doJourney(context);
		}
		catch (Exception ex)
		{
			LOGGER.warn("Best-effort sub-journey failed, continuing with the rest of the journey.", ex);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected String getActionName()
	{
		return "BestEffort";
	}
}
