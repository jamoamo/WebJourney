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
package com.github.jamoamo.webjourney;


/**
 *
 * @author James Amoore
 * @param <T> The type of iteration item
 */
class RepeatedAction<T> extends AWebAction
{
	private final SubJourney subJourney;
	private final IRepeatable<T> repeater;

	RepeatedAction(IRepeatable<T> repeater,
								 SubJourney subJourney)
	{
		if(repeater == null)
		{
			throw new RuntimeException("repeater cannot be null");
		}
		
		if(subJourney == null)
		{
			throw new RuntimeException("Sub Journey cannot be null");
		}
		
		this.subJourney = subJourney;
		this.repeater = repeater;
	}

	@Override
	protected ActionResult executeAction(IJourneyContext context)
	{
		try
		{
			Iterable<T> iterations = this.repeater.repeatIterable(context);

			for(T iterationItem : iterations)
			{
				context.setJourneyInput("repeatItem", iterationItem);
				this.subJourney.doJourney(context);
			}
			return ActionResult.SUCCESS;
		}
		catch(JourneyException ex)
		{
			return ActionResult.FAILURE;
		}
	}

}
