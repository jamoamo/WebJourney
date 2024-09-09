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
import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.util.function.Function;

/**
 *
 * @author James Amoore
 */
class ConditionalAction extends AWebAction
{
	private final Function<IBrowser, Boolean> conditionFunction;
	private final Function<IJourneyBuilder, IJourney> functionIfTrue;
	private final Function<IJourneyBuilder, IJourney> functionIfFalse;
	
	ConditionalAction(Function<IBrowser, Boolean> conditionFunction,
										Function<IJourneyBuilder, IJourney> ifTrue)
	{
		this.conditionFunction = conditionFunction;
		this.functionIfTrue = ifTrue;
		this.functionIfFalse = null;
	}
	
	ConditionalAction(Function<IBrowser, Boolean> conditionFunction,
										Function<IJourneyBuilder, IJourney> ifTrue, 
										Function<IJourneyBuilder, IJourney> ifFalse)
	{
		this.conditionFunction = conditionFunction;
		this.functionIfTrue = ifTrue;
		this.functionIfFalse = ifFalse;
	}
	
	@Override
	protected ActionResult executeActionImpl(IJourneyContext context)
		 throws BaseJourneyActionException
	{
		if(this.conditionFunction.apply(context.getBrowser()))
		{
			BaseJourneyBuilder builder = JourneyBuilder.path();
			IJourney journey = this.functionIfTrue.apply(builder);
			journey.doJourney(context);
		}
		else
		{
			if(this.functionIfFalse != null)
			{
				BaseJourneyBuilder builder = JourneyBuilder.path();
				IJourney journey = this.functionIfFalse.apply(builder);
				journey.doJourney(context);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected String getActionName()
	{
		return "Conditional";
	}
	
}
