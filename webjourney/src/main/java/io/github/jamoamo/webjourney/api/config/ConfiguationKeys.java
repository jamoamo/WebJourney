package io.github.jamoamo.webjourney.api.config;

/**
 * Keys for the configuration.
 */
public final class ConfiguationKeys 
{
	public static final String GLOBAL_ARGUMENTS = "browser.args";
	public static final String CHROME_ARGUMENTS = "browser.chrome.args";
	
	// Validation configuration
	public static final String VALIDATION_MODE = "browser.args.validation.mode";
	public static final String DENY_LIST = "browser.args.denyList";
	
	// Redaction configuration  
	public static final String REDACTION_EXTRA_KEYS = "browser.args.redaction.extraKeys";
	
	// Logging configuration
	public static final String LOG_LEVEL = "browser.args.logLevel";
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private ConfiguationKeys()
	{
	}
}
