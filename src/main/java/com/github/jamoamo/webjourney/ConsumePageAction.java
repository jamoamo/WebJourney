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

import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import com.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Amoore
 * @param <T> The type of the page class to be consumed.
 */
class ConsumePageAction<T> extends AWebAction
{
	private final Logger logger = LoggerFactory.getLogger(ConsumePageAction.class);
	
	private final Class<T> pageClass;
	private final Consumer<T> pageConsumer;
	
	ConsumePageAction(Class<T> pageClass, Consumer<T> pageConsumer)
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
	protected ActionResult executeAction(IJourneyContext context)
	{
		IBrowser browser = context.getBrowser();
		EntityDefn entityDefn = new EntityDefn(this.pageClass);
		EntityCreator<T> creator = new EntityCreator(entityDefn);
		T instance = creator.createNewEntity(browser);
		
		this.pageConsumer.accept(instance);
		
		return ActionResult.SUCCESS;
	}
}
