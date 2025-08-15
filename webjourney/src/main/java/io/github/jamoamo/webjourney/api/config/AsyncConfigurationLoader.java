package io.github.jamoamo.webjourney.api.config;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Loads the async configuration from the configuration.
 */
public final class AsyncConfigurationLoader 
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private AsyncConfigurationLoader() 
	{
	}

	/**
	 * Loads the async configuration from the properties.
	 * 
	 * @param props The properties to load the configuration from.
	 * @return The async configuration.
	 */
	public static AsyncConfiguration fromProperties(Properties props) 
	{
		return new AsyncConfiguration(
				splitList(props.getProperty(ConfiguationKeys.GLOBAL_ARGUMENTS)),
				splitList(props.getProperty(ConfiguationKeys.CHROME_ARGUMENTS)),
				splitList(props.getProperty(ConfiguationKeys.FIREFOX_ARGUMENTS)),
				splitList(props.getProperty(ConfiguationKeys.EDGE_ARGUMENTS)),
				Boolean.parseBoolean(props.getProperty(ConfiguationKeys.ENABLE_EXTRA_ARGS, "true")),
				props.getProperty(ConfiguationKeys.VALIDATION_MODE),
				splitList(props.getProperty(ConfiguationKeys.DENY_LIST)),
				splitList(props.getProperty(ConfiguationKeys.REDACTION_EXTRA_KEYS)),
				props.getProperty(ConfiguationKeys.LOG_LEVEL));
	}

	/**
	 * Splits a comma-separated string into a list of strings.
	 * 
	 * @param csvOrNull The comma-separated string to split.
	 * @return The list of strings.
	 */
	private static List<String> splitList(String csvOrNull)
   {
		if(csvOrNull == null || csvOrNull.isBlank())
		{
			return Collections.emptyList();
		}
		return Stream.of(csvOrNull.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toList();
   }
}
