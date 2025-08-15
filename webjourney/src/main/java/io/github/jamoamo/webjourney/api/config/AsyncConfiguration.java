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
	private final List<String> firefoxArguments;
	private final List<String> edgeArguments;
	private final boolean enableExtraArgs;
	private final String validationMode;
	private final List<String> denyList;
	private final List<String> redactionExtraKeys;
	private final String logLevel;

	/**
	 * Creates a new AsyncConfiguration.
	 * 
	 * @param globalArguments The global arguments to use for all browsers.
	 * @param chromeArguments The arguments to use for all chrome browsers.
	 */
	public AsyncConfiguration(List<String> globalArguments, List<String> chromeArguments)
	{
		this(globalArguments, chromeArguments, Collections.emptyList(), Collections.emptyList(), 
			 true, "reject", getDefaultDenyList(), Collections.emptyList(), "DEBUG");
	}

	/**
	 * Creates a new AsyncConfiguration with full configuration options.
	 * 
	 * @param globalArguments The global arguments to use for all browsers.
	 * @param chromeArguments The arguments to use for all chrome browsers.
	 * @param firefoxArguments The arguments to use for all firefox browsers.
	 * @param edgeArguments The arguments to use for all edge browsers.
	 * @param enableExtraArgs Feature flag to enable extra browser arguments.
	 * @param validationMode The validation mode for browser arguments (reject or warn).
	 * @param denyList The list of denied browser argument keys.
	 * @param redactionExtraKeys Additional keys to redact beyond defaults.
	 * @param logLevel The log level for browser arguments logging.
	 */
	public AsyncConfiguration(List<String> globalArguments, List<String> chromeArguments, 
							  List<String> firefoxArguments, List<String> edgeArguments,
							  boolean enableExtraArgs, String validationMode, List<String> denyList, 
							  List<String> redactionExtraKeys, String logLevel)
	{
		this.globalArguments = List.copyOf(globalArguments == null ? Collections.emptyList() : globalArguments);
		this.chromeArguments = List.copyOf(chromeArguments == null ? Collections.emptyList() : chromeArguments);
		this.firefoxArguments = List.copyOf(firefoxArguments == null ? Collections.emptyList() : firefoxArguments);
		this.edgeArguments = List.copyOf(edgeArguments == null ? Collections.emptyList() : edgeArguments);
		this.enableExtraArgs = enableExtraArgs;
		this.validationMode = validationMode != null ? validationMode : "reject";
		this.denyList = List.copyOf(denyList == null || denyList.isEmpty() ? getDefaultDenyList() : denyList);
		this.redactionExtraKeys = List.copyOf(redactionExtraKeys == null ? Collections.emptyList() : redactionExtraKeys);
		this.logLevel = logLevel != null ? logLevel : "DEBUG";
	}

	/**
	 * Gets the default deny-list for browser arguments.
	 * 
	 * @return the default deny-list
	 */
	private static List<String> getDefaultDenyList()
	{
		return List.of(
			"--user-data-dir",
			"--remote-debugging-port", 
			"--remote-debugging-address",
			"--disable-web-security",
			"--proxy-bypass-list",
			"--disable-extensions-except",
			"--load-extension"
		);
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

	/**
	 * Gets the firefox arguments.
	 * 
	 * @return The firefox arguments.
	 */
	public List<String> getFirefoxArguments()
	{
		return this.firefoxArguments;
	}

	/**
	 * Gets the edge arguments.
	 * 
	 * @return The edge arguments.
	 */
	public List<String> getEdgeArguments()
	{
		return this.edgeArguments;
	}

	/**
	 * Gets the enable extra args feature flag.
	 * 
	 * @return true if extra arguments are enabled, false otherwise.
	 */
	public boolean isEnableExtraArgs()
	{
		return this.enableExtraArgs;
	}

	/**
	 * Gets the validation mode for browser arguments.
	 * 
	 * @return The validation mode (reject or warn).
	 */
	public String getValidationMode()
	{
		return this.validationMode;
	}

	/**
	 * Gets the deny-list of browser argument keys.
	 * 
	 * @return The deny-list of keys.
	 */
	public List<String> getDenyList()
	{
		return this.denyList;
	}

	/**
	 * Gets the additional redaction keys beyond defaults.
	 * 
	 * @return The additional redaction keys.
	 */
	public List<String> getRedactionExtraKeys()
	{
		return this.redactionExtraKeys;
	}

	/**
	 * Gets the log level for browser arguments logging.
	 * 
	 * @return The log level.
	 */
	public String getLogLevel()
	{
		return this.logLevel;
	}
}
