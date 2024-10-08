/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.net.MalformedURLException;
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
	 */
	IJourney build();
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @return this journey builder
	 */
	IJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction, 
		 Function<IJourneyBuilder, IJourney> ifTrue);
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @param ifFalse a function providing the sub journey to follow if the condition is false.
	 * @return this journey builder
	 */
	IJourneyBuilder conditionalJourney(Function<IBrowser, Boolean> conditionFunction, 
		 Function<IJourneyBuilder, IJourney> ifTrue, Function<IJourneyBuilder, IJourney> ifFalse);
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @return this journey builder
	 */
	IJourneyBuilder conditionalJourney(FailableFunction<IBrowser, Boolean, Exception> conditionFunction, 
		 FailableFunction<IJourneyBuilder, IJourney, Exception> ifTrue);
	
	/**
	 * Conditionally follow a sub journey.
	 * 
	 * @param conditionFunction a function indicating if the sub journey should be followed.
	 * @param ifTrue a function providing the sub journey to follow if the condition is true.
	 * @param ifFalse a function providing the sub journey to follow if the condition is false.
	 * @return this journey builder
	 */
	IJourneyBuilder conditionalJourney(FailableFunction<IBrowser, Boolean, Exception> conditionFunction, 
		 FailableFunction<IJourneyBuilder, IJourney, Exception> ifTrue, 
		 FailableFunction<IJourneyBuilder, IJourney, Exception> ifFalse);

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageObject An object representing the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 */
	IJourneyBuilder clickButton(Object pageObject, String buttonName);

	/**
	 * Adds an action to click a button on the page.
	 *
	 * @param pageClass  The class representation of the page.
	 * @param buttonName The name of the button in the page representation that should be clicked.
	 *
	 * @return the current builder
	 */
	IJourneyBuilder clickButton(Class pageClass, String buttonName);

	/**
	 * Adds an action to complete a form on the page using the provided object. It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param formObject The object to complete the form using
	 *
	 * @return the current builder
	 */
	IActionOptionsJourneyBuilder completeForm(Object formObject);

	/**
	 * Adds an action to complete a form on the page and submit it using the provided object.
	 * It is expected that the object is
	 * suitably annotated to describe the elements that should be completed in the form.
	 *
	 * @param pageObject The object to complete the form using
	 *
	 * @return the current builder
	 */
	IActionOptionsJourneyBuilder completeFormAndSubmit(Object pageObject);

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
	<T> IJourneyBuilder consumePage(Class<T> pageClass,
		 FailableConsumer<T, ? extends PageConsumerException> pageConsumer);

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
	IJourneyBuilder forEachChildElement(Class pageClass, String elementName, String childElementType,
		 IJourney subJourney);

	/**
	 * Adds an action to navigate to the previous page in the browsers history.
	 *
	 * @return the current builder
	 */
	IActionOptionsJourneyBuilder navigateBack();

	/**
	 * Adds an action to navigate to the next page in the browsers history.
	 *
	 * @return the current builder
	 */
	IActionOptionsJourneyBuilder navigateForward();

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 */
	IJourneyBuilder navigateTo(URL url);

	/**
	 * Adds an action to the journey that navigates to the provided url.
	 *
	 * @param url The url to navigate to.
	 *
	 * @return the current builder
	 *
	 * @throws java.net.MalformedURLException if the url is malformed
	 */
	IActionOptionsJourneyBuilder navigateTo(String url)
		 throws MalformedURLException;

	/**
	 * Adds an action to refresh the current page.
	 *
	 * @return the current builder
	 */
	IActionOptionsJourneyBuilder refreshPage();
	
}
