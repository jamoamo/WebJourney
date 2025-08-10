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
}


