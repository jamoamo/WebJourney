package io.github.jamoamo.webjourney.api.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultBrowserArgumentsProviderTest
{
    private final Map<String, String> fakeEnv = new HashMap<>();

    @BeforeEach
    void setup()
    {
        fakeEnv.clear();
    }

    @AfterEach
    void teardown()
    {
        fakeEnv.clear();
    }

    @Test
    void resolve_precedence_lastWriterWins_andStableOrder()
    {
        // env: global + chrome
        fakeEnv.put("WEBJOURNEY_BROWSER_ARGS", "--a=2, --d=4");
        fakeEnv.put("WEBJOURNEY_CHROME_ARGS", "--e=5, --c=7");

        // per-journey: global + chrome-specific
        DefaultJourneyBrowserArguments args = new DefaultJourneyBrowserArguments();
        args.addGlobal(List.of("--a=1", "--b=1"));
        args.addForBrowser(StandardBrowser.CHROME, List.of("--c", "--b=9"));

        TestJourneyContext ctx = new TestJourneyContext(args);
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(fakeEnv::get);

        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, ctx);

        // Expected order derived from merge algorithm
        List<String> expected = List.of("--d=4", "--e=5", "--a=1", "--c", "--b=9");
        assertEquals(expected, resolved.getArguments());

        // Provenance
        assertEquals(expected.size(), resolved.getProvenance().size());
        Map<String, BrowserArgumentSource> srcByKey = new HashMap<>();
        for (ProvenancedArgument pa : resolved.getProvenance())
        {
            srcByKey.put(pa.key(), pa.source());
        }
        assertEquals(BrowserArgumentSource.ENVIRONMENT, srcByKey.get("--d"));
        assertEquals(BrowserArgumentSource.ENVIRONMENT, srcByKey.get("--e"));
        assertEquals(BrowserArgumentSource.PER_JOURNEY, srcByKey.get("--a"));
        assertEquals(BrowserArgumentSource.PER_JOURNEY, srcByKey.get("--c"));
        assertEquals(BrowserArgumentSource.PER_JOURNEY, srcByKey.get("--b"));
    }

    @Test
    void resolve_normalizes_spaceSeparated_toEquals()
    {
        DefaultJourneyBrowserArguments args = new DefaultJourneyBrowserArguments();
        args.addGlobal(List.of("--key", "value"));

        TestJourneyContext ctx = new TestJourneyContext(args);
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(fakeEnv::get);

        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, ctx);
        assertEquals(List.of("--key=value"), resolved.getArguments());
        assertEquals(1, resolved.getProvenance().size());
        assertEquals(BrowserArgumentSource.PER_JOURNEY, resolved.getProvenance().get(0).source());
        assertEquals("--key", resolved.getProvenance().get(0).key());
        assertEquals("value", resolved.getProvenance().get(0).value());
    }

    @Test
    void resolve_empty_when_noInputs()
    {
        DefaultJourneyBrowserArguments args = new DefaultJourneyBrowserArguments();
        TestJourneyContext ctx = new TestJourneyContext(args);
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(fakeEnv::get);

        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, ctx);
        assertTrue(resolved.getArguments().isEmpty());
        assertTrue(resolved.getProvenance().isEmpty());
    }

    @Test
    void resolve_keyOnly_overriddenByEquals_fromHigherPrecedence()
    {
        // env provides key-only; per-journey overrides with value
        fakeEnv.put("WEBJOURNEY_CHROME_ARGS", "--flag");

        DefaultJourneyBrowserArguments args = new DefaultJourneyBrowserArguments();
        args.addForBrowser(StandardBrowser.CHROME, List.of("--flag=value"));

        TestJourneyContext ctx = new TestJourneyContext(args);
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(fakeEnv::get);

        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, ctx);
        assertEquals(List.of("--flag=value"), resolved.getArguments());
        assertEquals(1, resolved.getProvenance().size());
        assertEquals(BrowserArgumentSource.PER_JOURNEY, resolved.getProvenance().get(0).source());
        assertEquals("--flag", resolved.getProvenance().get(0).key());
        assertEquals("value", resolved.getProvenance().get(0).value());
    }

    // No direct env mutation; provider is constructed with a getenv Function

    /** Minimal IJourneyContext stub for provider tests. */
    private static final class TestJourneyContext implements IJourneyContext
    {
        private final IJourneyBrowserArguments args;

        TestJourneyContext(IJourneyBrowserArguments args)
        {
            this.args = args;
        }

        @Override
        public IBrowser getBrowser()
        {
            return null;
        }

        @Override
        public void setJourneyInput(String inputType, Object inputValue)
        {
        }

        @Override
        public Object getJourneyInput(String inputType)
        {
            return null;
        }

        @Override
        public List<IJourneyObserver> getJourneyObservers()
        {
            return Collections.emptyList();
        }

        @Override
        public void setJourneyObservers(List<IJourneyObserver> observers)
        {
        }

        @Override
        public IJourneyBreadcrumb getJourneyBreadcrumb()
        {
            return null;
        }

        @Override
        public IJourneyBrowserArguments getBrowserArguments()
        {
            return this.args;
        }
    }
}


