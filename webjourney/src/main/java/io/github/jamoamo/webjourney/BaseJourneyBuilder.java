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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.function.FailableConsumer;

/**
 * Base builder for building a journey.
 *
 * @author James Amoore
 */
public class BaseJourneyBuilder
{
	private final JourneyBuild build;

	BaseJourneyBuilder()
	{
		this.build = new JourneyBuild();
	}

	BaseJourneyBuilder(WebJourney journey)
	{
		this.build = new JourneyBuild(journey);
	}

	BaseJourneyBuilder(JourneyBuild build)
	{
		this.build = build;
	}

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 */
	public BaseJourneyBuilder navigateTo(URL url)
	{
		this.build.addAction(new NavigateAction(NavigationTarget.toUrl(url)));
		return this;
	}

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 *
	 * @throws java.net.MalformedURLException if the url is malformed
	 */
	public ActionOptionsJourneyBuilder navigateTo(String url)
			  throws MalformedURLException
	{
		this.build.addAction(new NavigateAction(NavigationTarget.toUrl(url)));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to navigate to the previous page in the browsers history.
	 *
	 * @return the current builder
	 */
	public ActionOptionsJourneyBuilder navigateBack()
	{
		this.build.addAction(new NavigateAction(NavigationTarget.back()));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to navigate to the next page in the browsers history.
	 *
	 * @return the current builder
	 */
	public ActionOptionsJourneyBuilder navigateForward()
	{
		this.build.addAction(new NavigateAction(NavigationTarget.forward()));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to refresh the current page.
	 *
	 * @return the current builder
	 */
	public ActionOptionsJourneyBuilder refreshPage()
	{
		this.build.addAction(new NavigateAction(NavigationTarget.refresh()));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to complete a form on the page using the provided object. It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param formObject The object to complete the form using
	 *
	 * @return the current builder
	 */
	public ActionOptionsJourneyBuilder completeForm(Object formObject)
	{
		this.build.addAction(new CompleteFormAction(formObject));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to complete a form on the page and submit it using the provided object.
	 * It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param pageObject The object to complete the form using
	 *
	 * @return the current builder
	 */
	public ActionOptionsJourneyBuilder completeFormAndSubmit(Object pageObject)
	{
		CompleteFormAction action = new CompleteFormAction(pageObject);
		action.setSubmitAfterwards();
		this.build.addAction(action);
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to consume the page described by the provided page class and consume the resultant object using
	 * the provided page consumer.
	 *
	 * @param <T>          The type of the page object to be consumed.
	 * @param pageClass    The class describing the object to be created from the page. It is expected that the class be
	 *                     suitably annotated to consume the page.
	 * @param pageConsumer The consumer that will receive the created page object.
	 *
	 * @return the current builder
	 */
	public <T> BaseJourneyBuilder consumePage(Class<T> pageClass,
															FailableConsumer<T, ? extends PageConsumerException> pageConsumer)
	{
		ConsumePageAction<T> action = new ConsumePageAction<>(pageClass, pageConsumer);
		this.build.addAction(action);
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageObject An object representing the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 */
	public BaseJourneyBuilder clickButton(Object pageObject, String buttonName)
	{
		ClickButtonAction action = new ClickButtonAction(pageObject, buttonName);
		this.build.addAction(action);
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageClass  The class representation of the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 */
	public BaseJourneyBuilder clickButton(Class pageClass, String buttonName)
	{
		ClickButtonAction action = new ClickButtonAction(pageClass, buttonName);
		this.build.addAction(action);
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageClass        The class representation of the page.
	 * @param elementName      The name of element in the pageClass for whose children the sub journey should be
	 *                         repeated.
	 * @param childElementType The type of child element that should be considered. null if all child elements
	 *                         should be considered.
	 * @param subJourney       The sub journey to repeat
	 *
	 * @return the current builder
	 */
	public BaseJourneyBuilder forEachChildElement(
			  final Class pageClass,
			  final String elementName,
			  String childElementType,
			  SubJourney subJourney)
	{
		RepeatedAction action = new RepeatedAction(
				  new RepeatForChildElement(pageClass, elementName, childElementType), subJourney);
		this.build.addAction(action);
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Retrieves the state of the journey build in progress.
	 *
	 * @return the in-progress journey build state.
	 */
	protected JourneyBuild getBuild()
	{
		return this.build;
	}

	/**
	 * Builds an instance of WebJourney.
	 *
	 * @return a built WebJourney instance.
	 */
	public WebJourney build()
	{
		return this.build.getJourney();
	}
}
