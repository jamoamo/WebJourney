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
package io.github.jamoamo.webjourney.test;

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import io.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import io.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException;
import io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException;
import java.util.ArrayList;
import io.github.jamoamo.webjourney.api.web.XWebException;
import io.github.jamoamo.webjourney.test.mock.MockBrowser;
import io.github.jamoamo.webjourney.test.mock.MockRouter;

/**
 * Entity Creation tester.
 * @author James Amoore
 * @param <T> the type of the entity to be created
 */
public final class TestCreator<T>
{
	private final Class<T> entityClass;

	/**
	 * Constructor.
	 * @param entityClass The entity class to test creation of.
	 */
	public TestCreator(Class<T> entityClass)
	{
		this.entityClass = entityClass;
	}

	/**
	 * Create the entity using the provided browser.
	 * @param browser the browser.
	 * @return the CreationResult.
	 */
	public CreationResult<T> createEntityWith(IBrowser browser)
	{
		CreationResult result = new CreationResult();
		try
		{
			EntityCreator<T> creator = new EntityCreator<>(new EntityDefn<>(this.entityClass), false, new ArrayList<>());
			result.setCreatedEntiy(creator.createNewEntity(browser));
			result.setResult(CreationResult.Result.SUCCESS);
		}
		catch(XEntityFieldScrapeException | XEntityDefinitionException ex)
		{
			result.setResult(CreationResult.Result.FAILED);
			result.setFailureException(ex);
		}
		return result;
	}
	
	/**
	 * Create the entity for the provided element and browser.
	 * @param element the element
	 * @param browser the browser
	 * @return the creation result
	 */
	public CreationResult<T> createEntityWith(AElement element, IBrowser browser)
	{
		CreationResult result = new CreationResult();
		try
		{
			EntityCreator<T> creator = new EntityCreator<>(new EntityDefn<>(this.entityClass), element, new ArrayList<>());
			result.setCreatedEntiy(creator.createNewEntity(browser));
			result.setResult(CreationResult.Result.SUCCESS);
		}
		catch(XEntityFieldScrapeException | XEntityDefinitionException ex)
		{
			result.setResult(CreationResult.Result.FAILED);
			result.setFailureException(ex);
		}
		return result;
	}

	/**
	 * Convenience: create an entity using a MockBrowser initialised with a router and initial URL.
	 * @param router the router
	 * @param initialUrl the initial URL to load (may be null)
	 * @return the creation result
	 */
	public CreationResult<T> createEntityWith(MockRouter router, String initialUrl)
	{
		MockBrowser browser = new MockBrowser(router);
		try
		{
			browser.loadInitial(initialUrl);
		}
		catch(Exception ex)
		{
			CreationResult<T> r = new CreationResult<>();
			r.setResult(CreationResult.Result.FAILED);
			r.setFailureException(ex);
			return r;
		}
		return createEntityWith((IBrowser) browser);
	}
}
