/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.github.jamoamo.webjourney.api;

import java.util.List;

/**
 * The journey path to follow.
 * 
 * @author James Amoore
 */
public interface IWebJourneyPath
{
	/**
	 * 
	 * @return The list of actions.
	 */
	List<AWebAction> getActions();
}
