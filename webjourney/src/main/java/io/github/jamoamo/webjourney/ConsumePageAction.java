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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.PageConsumerException;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import io.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import io.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException;
import io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException;
import org.apache.commons.lang3.function.FailableConsumer;

/**
 * @author James Amoore
 * @param <T> The type of the page class to be consumed.
 */
class ConsumePageAction<T> extends AWebAction
{
	private final Class<T> pageClass;
	private final FailableConsumer<T, ? extends PageConsumerException> pageConsumer;
	
	ConsumePageAction(Class<T> pageClass, FailableConsumer<T, ? extends PageConsumerException> pageConsumer)
	{
		if(pageConsumer == null)
		{
			throw new NullPointerException("Page Consumer should not be null.");
		}
		
		if(pageClass == null)
		{
			throw new NullPointerException("Page Class should not be null.");
		}
		
		this.pageClass = pageClass;
		this.pageConsumer = pageConsumer;
	}
	
	@Override
	protected ActionResult executeActionImpl(IJourneyContext context)
			  throws BaseJourneyActionException
	{
		try
		{
			IBrowser browser = context.getBrowser();
			EntityDefn entityDefn = new EntityDefn(this.pageClass);
			EntityCreator<T> creator = new EntityCreator(entityDefn, false, context.getJourneyObservers());
			T instance = creator.createNewEntity(browser);
			this.pageConsumer.accept(instance);
		}
		catch(PageConsumerException | XEntityDefinitionException | XEntityFieldScrapeException ex)
		{
			throw new BaseJourneyActionException(ex.getMessage(), this, ex);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	protected String getActionName()
	{
		return "Consume Page";
	}
}
