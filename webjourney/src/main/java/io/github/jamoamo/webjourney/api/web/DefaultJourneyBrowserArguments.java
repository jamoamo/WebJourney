package io.github.jamoamo.webjourney.api.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link IJourneyBrowserArguments}.
 * This implementation is thread-safe and can be safely accessed from multiple threads.
 */
public final class DefaultJourneyBrowserArguments implements IJourneyBrowserArguments
{
	private final List<String> globalArguments = new CopyOnWriteArrayList<>();
	private final Map<StandardBrowser, List<String>> browserArguments 
		= new ConcurrentHashMap<>();

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
			.computeIfAbsent(browserType, k -> new CopyOnWriteArrayList<>())
			.addAll(arguments);
	}

	/**
	 * Gets the global arguments.
	 * 
	 * @return An immutable snapshot of the global arguments.
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
	 * @return An immutable snapshot of the arguments for the browser.
	 */
	@Override
	public List<String> snapshotForBrowser(StandardBrowser browserType)
	{
		List<String> args = this.browserArguments.get(browserType);
		if(args == null)
		{
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(args);
	}
}
