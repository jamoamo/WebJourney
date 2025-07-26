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

import io.github.jamoamo.webjourney.api.PageConsumerException;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.JourneyBuilderException;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;

/**
 * Base builder for building a journey. Provides a fluent API for defining web actions
 * and constructing a {@link IJourney}.
 *
 * @author James Amoore
 * @see IJourneyBuilder
 * @see IJourney
 * @see WebTraveller
 * @since 1.0.0
 */
public class BaseJourneyBuilder implements IJourneyBuilder
{
	private final JourneyBuild build;

	/**
	 * Constructs a new BaseJourneyBuilder.
	 * @since 1.0.0
	 */
	BaseJourneyBuilder()
	{
		this.build = new JourneyBuild();
	}

	/**
	 * Constructs a new BaseJourneyBuilder with an existing WebJourney.
	 * @param journey The existing web journey.
	 * @since 1.0.0
	 */
	BaseJourneyBuilder(WebJourney journey)
	{
		this.build = new JourneyBuild(journey);
	}

	/**
	 * Constructs a new BaseJourneyBuilder with an existing JourneyBuild.
	 * @param build The existing journey build state.
	 * @since 1.0.0
	 */
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 * @since 1.0.0
	 */
	@Override
	public ActionOptionsJourneyBuilder navigateTo(String url)
			  throws JourneyBuilderException
	{
		try
		{
			this.build.addAction(new NavigateAction(NavigationTarget.toUrl(url)));
			return new ActionOptionsJourneyBuilder(this.build);
		}
		catch(MalformedURLException ex)
		{
			throw new JourneyBuilderException(ex);
		}
	}

	/**
	 * Adds an action to navigate to the previous page in the browsers history.
	 *
	 * @return the current builder
	 * @since 1.0.0
	 */
	@Override
	public ActionOptionsJourneyBuilder navigateBack()
	{
		this.build.addAction(new NavigateAction(NavigationTarget.back()));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to navigate to the next page in the browsers history.
	 *
	 * @return the current builder
	 * @since 1.0.0
	 */
	@Override
	public ActionOptionsJourneyBuilder navigateForward()
	{
		this.build.addAction(new NavigateAction(NavigationTarget.forward()));
		return new ActionOptionsJourneyBuilder(this.build);
	}

	/**
	 * Adds an action to refresh the current page.
	 *
	 * @return the current builder
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
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
	 * @since 1.0.0
	 */
	@Override
	public BaseJourneyBuilder forEachChildElement(
			  final Class pageClass,
			  final String elementName,
			  String childElementType,
			  IJourney subJourney)
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
	 * @since 1.0.0
	 */
	protected JourneyBuild getBuild()
	{
		return this.build;
	}

	/**
	 * Builds the journey defined by this builder.
	 *
	 * @return A new {@link IJourney} instance.
	 * @since 1.0.0
	 */
	@Override
	public IJourney build()
	{
		SubJourney subJourney = new SubJourney(this.build.buildJourney());
		return subJourney;
	}

	/**
	 * Adds a conditional journey to the current journey. The `ifTrue` journey is executed
	 * if the condition function returns true.
	 *
	 * @param conditionFunction A function that takes an {@link IBrowser} and returns a boolean.
	 *                          If true, the `ifTrue` journey is executed.
	 * @param ifTrue            A function that builds the journey to be executed if the condition is true.
	 * @return The current builder instance.
	 * @since 1.0.0
	 */
	@Override
	public BaseJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction,
		 Function<IJourneyBuilder, IJourney> ifTrue)
	{
		this.build.addAction(new ConditionalAction(conditionFunction, ifTrue));
		return this;
	}

	/**
	 * Adds a conditional journey to the current journey with an alternative path. The `ifTrue` journey is executed
	 * if the condition function returns true, otherwise the `ifFalse` journey is executed.
	 *
	 * @param conditionFunction A function that takes an {@link IBrowser} and returns a boolean.
	 *                          If true, the `ifTrue` journey is executed.
	 * @param ifTrue            A function that builds the journey to be executed if the condition is true.
	 * @param ifFalse           A function that builds the journey to be executed if the condition is false.
	 * @return The current builder instance.
	 * @since 1.0.0
	 */
	@Override
	public BaseJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction,
		 Function<IJourneyBuilder, IJourney> ifTrue, Function<IJourneyBuilder, IJourney> ifFalse)
	{
		this.build.addAction(new ConditionalAction(conditionFunction, ifTrue, ifFalse));
		return this;
	}

	/**
	 * Adds a conditional journey to the current journey. The `ifTrue` journey is executed
	 * if the condition function returns true. This version handles exceptions thrown by the condition 
	 * or journey functions.
	 *
	 * @param conditionFunction A failable function that takes an {@link IBrowser} and returns a boolean.
	 *                          If true, the `ifTrue` journey is executed.
	 * @param ifTrue            A failable function that builds the journey to be executed if the condition is true.
	 * @return The current builder instance.
	 * @throws JourneyException if an error occurs during the condition evaluation or journey execution.
	 * @since 1.0.0
	 */
	@Override
	public BaseJourneyBuilder conditionalJourney(
		 FailableFunction<IBrowser, Boolean, JourneyException> conditionFunction,
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifTrue)
	{
		this.build.addAction(new ConditionalAction(conditionFunction, ifTrue));
		return this;
	}

	/**
	 * Adds a conditional journey to the current journey with an alternative path. The `ifTrue` journey is executed
	 * if the condition function returns true, otherwise the `ifFalse` journey is executed. This version handles
	 * exceptions thrown by the condition or journey functions.
	 *
	 * @param conditionFunction A failable function that takes an {@link IBrowser} and returns a boolean.
	 *                          If true, the `ifTrue` journey is executed.
	 * @param ifTrue            A failable function that builds the journey to be executed if the condition is true.
	 * @param ifFalse           A failable function that builds the journey to be executed if the condition is false.
	 * @return The current builder instance.
	 * @throws JourneyException if an error occurs during the condition evaluation or journey execution.
	 * @since 1.0.0
	 */
	@Override
	public BaseJourneyBuilder conditionalJourney(
		 FailableFunction<IBrowser, Boolean, JourneyException> conditionFunction,
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifTrue, 
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifFalse)
	{
		this.build.addAction(new ConditionalAction(conditionFunction, ifTrue, ifFalse));
		return this;
	}
}
