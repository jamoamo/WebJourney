/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.github.jamoamo.webjourney.api;

/**
 *
 * @author James Amoore
 */
public interface IActionOptionsJourneyBuilder extends IJourneyBuilder
{
	/**
	 * Sets the wait, in seconds, before the action. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait before the action is executed.
	 * @return this builder
	 */
	IActionOptionsJourneyBuilder withPostActionWait(int waitTimeSeconds);

	/**
	 * Sets the wait, in seconds, before the action. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait before the action is executed.
	 * @return this builder
	 */
	IActionOptionsJourneyBuilder withPreActionWait(int waitTimeSeconds);
	
}
