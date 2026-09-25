/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.JourneyException;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.net.URL;
import java.util.function.Function;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;

/**
 *
 * @author James Amoore
 */
public interface IJourneyBuilder
{
	/**
	 * Builds an instance of WebJourney.
	 *
	 * @return a built WebJourney instance.
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourney build() throws JourneyBuilderException;
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @return this journey builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction, 
		 Function<IJourneyBuilder, IJourney> ifTrue) throws JourneyException;
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @param ifFalse a function providing the sub journey to follow if the condition is false.
	 * @return this journey builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction, 
		 Function<IJourneyBuilder, IJourney> ifTrue, Function<IJourneyBuilder, IJourney> ifFalse) 
		 throws JourneyBuilderException;
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @return this journey builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder conditionalJourney(FailableFunction<IBrowser, Boolean, JourneyException> conditionFunction, 
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifTrue) throws JourneyBuilderException;
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @param ifFalse a function providing the sub journey to follow if the condition is false.
	 * @return this journey builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder conditionalJourney(FailableFunction<IBrowser, Boolean, JourneyException> conditionFunction,
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifTrue,
		 FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifFalse) throws JourneyBuilderException;

	/**
	 * Always attempts a sub journey, but unlike {@link #conditionalJourney(Function, Function)} does not
	 * abort the rest of the journey if it fails -- the failure is logged and swallowed instead. Intended
	 * for steps worth attempting on every run (e.g. logging in) whose own unreliability shouldn't take
	 * down journeys that don't strictly depend on them succeeding. The step is named "BestEffort" in logs.
	 * <p>
	 * Note that a refused connection (see {@link ConnectionFailures#isConnectionRefused(Throwable)}) is not retried by
	 * default, so it fails on the first attempt with no retry delay. A caller that runs whole journeys in a loop should
	 * use the listener overload, and its {@link BestEffortDecision#ABORT} decision, to back off when that happens.
	 *
	 * @param subJourney a function providing the sub journey to attempt.
	 * @return this journey builder
	 * @throws IllegalArgumentException if the sub journey function is null.
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder bestEffortJourney(FailableFunction<IJourneyBuilder, IJourney, JourneyException> subJourney)
		 throws JourneyBuilderException;

	/**
	 * Always attempts a sub journey, but does not abort the rest of the journey if it fails, just like
	 * {@link #bestEffortJourney(FailableFunction)}. The listener is additionally told how the sub journey
	 * ended, including the swallowed exception on failure, so callers can react to it (e.g. back off when
	 * {@link ConnectionFailures#isConnectionRefused(Throwable)} returns true), and decides what happens next:
	 * <ul>
	 * <li>{@link BestEffortDecision#CONTINUE} carries on with the rest of the journey.</li>
	 * <li>{@link BestEffortDecision#ABORT} after a failure stops the journey by rethrowing the original failure, cause
	 * chain intact. The step is not retried. After a success it is ignored.</li>
	 * <li>A listener that throws an {@link Exception} (checked or not) is logged and treated as {@code CONTINUE}; an
	 * {@link Error} propagates. A listener that returns null is also treated as {@code CONTINUE}.</li>
	 * </ul>
	 *
	 * @param subJourney a function providing the sub journey to attempt.
	 * @param outcomeListener a listener notified of how the sub journey ended, and deciding whether to carry on.
	 * @return this journey builder
	 * @throws IllegalArgumentException if the sub journey function or the listener is null.
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder bestEffortJourney(FailableFunction<IJourneyBuilder, IJourney, JourneyException> subJourney,
		 IBestEffortOutcomeListener outcomeListener) throws JourneyBuilderException;

	/**
	 * As {@link #bestEffortJourney(FailableFunction, IBestEffortOutcomeListener)}, but the step has a name. The name
	 * is used in the step's log messages, as its action name (so in the MDC label and the journey breadcrumb) and is
	 * given to the listener in {@link BestEffortOutcome#getName()}. The overloads without a name call the step
	 * "BestEffort".
	 *
	 * @param name the name of the step, e.g. "login".
	 * @param subJourney a function providing the sub journey to attempt.
	 * @param outcomeListener a listener notified of how the sub journey ended, and deciding whether to carry on.
	 * @return this journey builder
	 * @throws IllegalArgumentException if the name is null or blank, or the sub journey function or the listener is null.
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder bestEffortJourney(String name, FailableFunction<IJourneyBuilder, IJourney, JourneyException> subJourney,
		 IBestEffortOutcomeListener outcomeListener) throws JourneyBuilderException;

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageObject An object representing the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder clickButton(Object pageObject, String buttonName) throws JourneyBuilderException;

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageClass  The class representation of the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */ 
	IJourneyBuilder clickButton(Class pageClass, String buttonName) throws JourneyBuilderException;

	/**
	 * Adds an action to complete a form on the page using the provided object. It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param formObject The object to complete the form using
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder completeForm(Object formObject) throws JourneyBuilderException;

	/**
	 * Adds an action to complete a form on the page and submit it using the provided object.
	 * It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param pageObject The object to complete the form using
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder completeFormAndSubmit(Object pageObject) throws JourneyBuilderException;

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
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	<T> IJourneyBuilder consumePage(Class<T> pageClass,
		 FailableConsumer<T, ? extends PageConsumerException> pageConsumer) throws JourneyBuilderException;

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
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder forEachChildElement(Class pageClass, String elementName, String childElementType,
		 IJourney subJourney) throws JourneyBuilderException;

	/**
	 * Adds an action to navigate to the previous page in the browsers history.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder navigateBack() throws JourneyBuilderException;

	/**
	 * Adds an action to navigate to the next page in the browsers history.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder navigateForward() throws JourneyBuilderException;

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IJourneyBuilder navigateTo(URL url) throws JourneyBuilderException;

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder navigateTo(String url)
		 throws JourneyBuilderException;

	/**
	 * Adds an action to refresh the current page.
	 *
	 * @return the current builder
	 * @throws io.github.jamoamo.webjourney.api.JourneyBuilderException if an error occurs
	 */
	IActionOptionsJourneyBuilder refreshPage() throws JourneyBuilderException;
	
}
