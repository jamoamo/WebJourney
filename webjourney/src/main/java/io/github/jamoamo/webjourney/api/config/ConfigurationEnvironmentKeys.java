package io.github.jamoamo.webjourney.api.config;

/**
 * Keys for the environment variable keys.
 */
public final class ConfigurationEnvironmentKeys 
{
	public static final String BROWSER_ARGS = "WEBJOURNEY_BROWSER_ARGS";
    public static final String CHROME_ARGS = "WEBJOURNEY_CHROME_ARGS";
    // Reserved for future browsers (not wired yet):
    public static final String FIREFOX_ARGS = "WEBJOURNEY_FIREFOX_ARGS";
    public static final String EDGE_ARGS = "WEBJOURNEY_EDGE_ARGS";

	private ConfigurationEnvironmentKeys()
	{
	}
}
