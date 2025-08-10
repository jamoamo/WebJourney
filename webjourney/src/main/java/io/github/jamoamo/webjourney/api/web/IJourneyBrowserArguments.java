package io.github.jamoamo.webjourney.api.web;

import java.util.List;

/**
 * Interface for browser arguments.
 */
public interface IJourneyBrowserArguments 
{
	/**
	 * Adds global arguments.
	 * 
	 * @param arguments The arguments to add.
	 */
	void addGlobal(List<String> arguments);

	/**
	 * Adds arguments for a specific browser.
	 * 
	 * @param browserType The browser type.
	 * @param arguments The arguments to add.
	 */
	void addForBrowser(StandardBrowser browserType, List<String> arguments);

	/**
	 * Gets the global arguments.
	 * 
	 * @return The global arguments.
	 */
	List<String> snapshotGlobal();

	/**
	 * Gets the arguments for a specific browser.
	 * 
	 * @param browserType The browser type.
	 * @return The arguments for the browser.
	 */
	List<String> snapshotForBrowser(StandardBrowser browserType);
}
