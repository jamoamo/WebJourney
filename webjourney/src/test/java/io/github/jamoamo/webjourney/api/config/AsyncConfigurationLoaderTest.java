package io.github.jamoamo.webjourney.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AsyncConfigurationLoader} loading from Properties.
 */
public final class AsyncConfigurationLoaderTest
{
    @Test
    public void emptyProperties_yieldsEmptyLists()
    {
        Properties props = new Properties();

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertTrue(conf.getGlobalArguments().isEmpty(), "global args should be empty by default");
        assertTrue(conf.getChromeArguments().isEmpty(), "chrome args should be empty by default");
    }

    @Test
    public void globalOnly_parsesCsvWithTrimmingAndEmptyRemoval()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.GLOBAL_ARGUMENTS, "  --a  ,  ,  --b=c , ");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals(List.of("--a", "--b=c"), conf.getGlobalArguments());
        assertTrue(conf.getChromeArguments().isEmpty());
    }

    @Test
    public void chromeOnly_parsesCsvWithTrimmingAndEmptyRemoval()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.CHROME_ARGUMENTS, " , --headless=new ,   ");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertTrue(conf.getGlobalArguments().isEmpty());
        assertEquals(List.of("--headless=new"), conf.getChromeArguments());
    }

    @Test
    public void bothGlobalAndChrome_areLoadedIndependently()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.GLOBAL_ARGUMENTS, "--foo,--bar=baz");
        props.setProperty(ConfiguationKeys.CHROME_ARGUMENTS, "--chrome-flag");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals(List.of("--foo", "--bar=baz"), conf.getGlobalArguments());
        assertEquals(List.of("--chrome-flag"), conf.getChromeArguments());
    }

    @Test
    public void blankValues_yieldEmptyLists()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.GLOBAL_ARGUMENTS, "   ");
        props.setProperty(ConfiguationKeys.CHROME_ARGUMENTS, "");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertTrue(conf.getGlobalArguments().isEmpty());
        assertTrue(conf.getChromeArguments().isEmpty());
    }

    @Test
    public void returnedLists_areImmutable()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.GLOBAL_ARGUMENTS, "--a");
        props.setProperty(ConfiguationKeys.CHROME_ARGUMENTS, "--c");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertThrows(UnsupportedOperationException.class, () -> conf.getGlobalArguments().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> conf.getChromeArguments().add("y"));
    }

    // M4 Configuration Tests

    @Test
    public void validationMode_loadsCorrectly()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.VALIDATION_MODE, "warn");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals("warn", conf.getValidationMode());
    }

    @Test
    public void validationMode_defaultsToReject()
    {
        Properties props = new Properties();
        // Don't set validation mode

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals("reject", conf.getValidationMode());
    }

    @Test
    public void denyList_parsesCommaSeparated()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.DENY_LIST, "--custom-deny,--another-deny,--third");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals(List.of("--custom-deny", "--another-deny", "--third"), conf.getDenyList());
    }

    @Test
    public void denyList_defaultsToStandardList()
    {
        Properties props = new Properties();
        // Don't set deny list - should use defaults from AsyncConfiguration constructor

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        List<String> denyList = conf.getDenyList();
        assertTrue(denyList.contains("--user-data-dir"), "Should contain --user-data-dir but got: " + denyList);
        assertTrue(denyList.contains("--remote-debugging-port"), "Should contain --remote-debugging-port but got: " + denyList);
        assertTrue(denyList.contains("--disable-web-security"), "Should contain --disable-web-security but got: " + denyList);
    }

    @Test
    public void redactionExtraKeys_parsesCommaSeparated()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.REDACTION_EXTRA_KEYS, "--secret-key,--api-token");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals(List.of("--secret-key", "--api-token"), conf.getRedactionExtraKeys());
    }

    @Test
    public void redactionExtraKeys_defaultsToEmpty()
    {
        Properties props = new Properties();
        // Don't set redaction extra keys

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertTrue(conf.getRedactionExtraKeys().isEmpty());
    }

    @Test
    public void logLevel_loadsCorrectly()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.LOG_LEVEL, "INFO");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals("INFO", conf.getLogLevel());
    }

    @Test
    public void logLevel_defaultsToDebug()
    {
        Properties props = new Properties();
        // Don't set log level

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals("DEBUG", conf.getLogLevel());
    }

    @Test
    public void allM4Properties_loadTogether()
    {
        Properties props = new Properties();
        props.setProperty(ConfiguationKeys.GLOBAL_ARGUMENTS, "--global");
        props.setProperty(ConfiguationKeys.CHROME_ARGUMENTS, "--chrome");
        props.setProperty(ConfiguationKeys.VALIDATION_MODE, "warn");
        props.setProperty(ConfiguationKeys.DENY_LIST, "--custom-deny");
        props.setProperty(ConfiguationKeys.REDACTION_EXTRA_KEYS, "--secret");
        props.setProperty(ConfiguationKeys.LOG_LEVEL, "TRACE");

        AsyncConfiguration conf = AsyncConfigurationLoader.fromProperties(props);

        assertEquals(List.of("--global"), conf.getGlobalArguments());
        assertEquals(List.of("--chrome"), conf.getChromeArguments());
        assertEquals("warn", conf.getValidationMode());
        assertEquals(List.of("--custom-deny"), conf.getDenyList());
        assertEquals(List.of("--secret"), conf.getRedactionExtraKeys());
        assertEquals("TRACE", conf.getLogLevel());
    }
}


