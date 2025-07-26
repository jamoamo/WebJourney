/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.github.jamoamo.webjourney.api;

/**
 * Represents a journey builder that allows for specifying options for the last added action,
 * such as pre and post-action wait times. This interface extends {@link IJourneyBuilder}
 * to provide a fluent API for building web journeys with fine-grained control over action execution.
 *
 * @author James Amoore
 * @see IJourneyBuilder
 * @since 1.0.0
 */
public interface IActionOptionsJourneyBuilder extends IJourneyBuilder
{
	/**
	 * Sets the wait time in seconds to be performed after the action is executed. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait after the action is executed.
	 * @return this builder instance, allowing for method chaining.
	 * @since 1.0.0
	 */
	IActionOptionsJourneyBuilder withPostActionWait(int waitTimeSeconds);

	/**
	 * Sets the wait time in seconds to be performed before the action is executed. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait before the action is executed.
	 * @return this builder instance, allowing for method chaining.
	 * @since 1.0.0
	 */
	IActionOptionsJourneyBuilder withPreActionWait(int waitTimeSeconds);
	
}
