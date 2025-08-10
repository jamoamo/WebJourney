package io.github.jamoamo.webjourney.api.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IJourneyBrowserArguments}.
 */
public final class DefaultJourneyBrowserArguments implements IJourneyBrowserArguments
{
	private final List<String> globalArguments = new ArrayList<>();
	private final Map<StandardBrowser, List<String>> browserArguments 
		= new EnumMap<>(StandardBrowser.class);

	/**
	 * Adds global arguments.
	 * 
	 * @param arguments The arguments to add.
	 */
	@Override
	public void addGlobal(List<String> arguments)
	{
		this.globalArguments.addAll(arguments);
	}

	/**
	 * Adds arguments for a specific browser.
	 * 
	 * @param browserType The browser type.
	 * @param arguments The arguments to add.
	 */
	@Override
	public void addForBrowser(StandardBrowser browserType, List<String> arguments)
	{
		this.browserArguments
			.computeIfAbsent(browserType, k -> new ArrayList<>())
			.addAll(arguments);
	}

	/**
	 * Gets the global arguments.
	 * 
	 * @return The global arguments.
	 */
	@Override
	public List<String> snapshotGlobal()
	{
		return Collections.unmodifiableList(this.globalArguments);
	}

	/**
	 * Gets the arguments for a specific browser.
	 * 
	 * @param browserType The browser type.
	 * @return The arguments for the browser.
	 */
	@Override
	public List<String> snapshotForBrowser(StandardBrowser browserType)
	{
		return Collections.unmodifiableList(this.browserArguments.get(browserType));
	}
}
