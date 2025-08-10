package io.github.jamoamo.webjourney.api.config;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for the async browser pool.
 */
public final class AsyncConfiguration 
{	
	private final List<String> globalArguments;
	private final List<String> chromeArguments;

	/**
	 * Creates a new AsyncConfiguration.
	 * 
	 * @param globalArguments The global arguments to use for all browsers.
	 * @param chromeArguments The arguments to use for all chrome browsers.
	 */
	public AsyncConfiguration(List<String> globalArguments, List<String> chromeArguments)
	{
		this.globalArguments = List.copyOf(globalArguments == null ? Collections.emptyList() : globalArguments);
		this.chromeArguments = List.copyOf(chromeArguments == null ? Collections.emptyList() : chromeArguments);
	}

	/**
	 * Gets the global arguments.
	 * 
	 * @return The global arguments.
	 */
	public List<String> getGlobalArguments()
	{
		return this.globalArguments;
	}

	/**
	 * Gets the chrome arguments.
	 * 
	 * @return The chrome arguments.
	 */
	public List<String> getChromeArguments()
	{
		return this.chromeArguments;
	}
}
