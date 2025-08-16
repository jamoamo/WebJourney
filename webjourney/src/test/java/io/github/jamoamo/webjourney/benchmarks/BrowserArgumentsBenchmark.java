/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.benchmarks;

import io.github.jamoamo.webjourney.JourneyContext;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import io.github.jamoamo.webjourney.api.web.BrowserArgParser;
import io.github.jamoamo.webjourney.api.web.BrowserArgumentsMerge;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.ResolvedBrowserArguments;
import io.github.jamoamo.webjourney.api.web.StandardBrowser;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * JMH benchmarks for browser arguments performance validation.
 * 
 * <p>Validates the <1ms overhead requirement for empty arguments and
 * measures performance of argument parsing, merging, and resolution.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class BrowserArgumentsBenchmark
{
    private DefaultBrowserArgumentsProvider emptyProvider;
    private DefaultBrowserArgumentsProvider loadedProvider;
    private JourneyContext emptyContext;
    private JourneyContext lightContext;
    private JourneyContext heavyContext;
    
    // Test data for parsing benchmarks
    private String simpleArgs;
    private String complexArgs;
    private String quotedArgs;
    
    // Test data for merging benchmarks
    private List<String> globalArgs;
    private List<String> chromeArgs;
    private List<String> envArgs;
    private List<String> journeyArgs;

    @Setup
    public void setup()
    {
        // Empty provider (baseline case)
        emptyProvider = new DefaultBrowserArgumentsProvider();
        
        // Loaded provider with configuration and environment
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBJOURNEY_BROWSER_ARGS", "--global-env,--headless");
        envVars.put("WEBJOURNEY_CHROME_ARGS", "--chrome-env,--disable-gpu");
        Function<String, String> envFunction = envVars::get;
        
        AsyncConfiguration config = new AsyncConfiguration(
            List.of("--global-config", "--window-size=1920,1080"),
            List.of("--chrome-config", "--disable-extensions")
        );
        loadedProvider = new DefaultBrowserArgumentsProvider(envFunction, config);
        
        // Journey contexts with different argument loads
        emptyContext = createEmptyJourneyContext();
        lightContext = createLightJourneyContext();
        heavyContext = createHeavyJourneyContext();
        
        // Test data for parsing benchmarks
        simpleArgs = "--headless,--disable-gpu";
        complexArgs = "--proxy-server=\"http://proxy:8080\",--user-agent=\"Mozilla/5.0 Test\",--window-size=1920x1080";
        quotedArgs = "--arg=\"quoted value with spaces\",--proxy=\"user:pass@proxy.com:8080\"";
        
        // Test data for merging benchmarks
        globalArgs = List.of("--global1", "--global2=value");
        chromeArgs = List.of("--chrome1", "--chrome2=value");
        envArgs = List.of("--env1", "--env2=value");
        journeyArgs = List.of("--journey1", "--journey2=value");
    }

    // ========================================
    // Core Provider Benchmarks (Target: <1ms)
    // ========================================

    @Benchmark
    public ResolvedBrowserArguments benchmarkEmptyArguments()
    {
        return emptyProvider.resolve(StandardBrowser.CHROME, emptyContext);
    }

    @Benchmark
    public ResolvedBrowserArguments benchmarkLightArguments()
    {
        return loadedProvider.resolve(StandardBrowser.CHROME, lightContext);
    }

    @Benchmark
    public ResolvedBrowserArguments benchmarkHeavyArguments()
    {
        return loadedProvider.resolve(StandardBrowser.CHROME, heavyContext);
    }

    // ========================================
    // Argument Parsing Benchmarks
    // ========================================

    @Benchmark
    public List<String> benchmarkParseSimpleArgs()
    {
        return BrowserArgParser.parse(simpleArgs);
    }

    @Benchmark
    public List<String> benchmarkParseComplexArgs()
    {
        return BrowserArgParser.parse(complexArgs);
    }

    @Benchmark
    public List<String> benchmarkParseQuotedArgs()
    {
        return BrowserArgParser.parse(quotedArgs);
    }

    @Benchmark
    public List<String> benchmarkNormalizeArgs()
    {
        return BrowserArgParser.normalize(List.of("--arg", "value", "--key=value", "--flag"));
    }

    // ========================================
    // Argument Merging Benchmarks
    // ========================================

    @Benchmark
    public List<String> benchmarkMergeByPrecedence()
    {
        return BrowserArgumentsMerge.mergeByPrecedence(
            globalArgs, chromeArgs, envArgs, List.of(), journeyArgs, List.of()
        );
    }

    @Benchmark
    public List<String> benchmarkMergeWithDuplicates()
    {
        // Test merging with overlapping keys
        return BrowserArgumentsMerge.mergeByPrecedence(
            List.of("--headless", "--window-size=800x600"),
            List.of("--headless", "--window-size=1920x1080"),
            List.of("--headless=true", "--window-size=1440x900"),
            List.of(),
            List.of("--window-size=1024x768"),
            List.of()
        );
    }

    // ========================================
    // Different Browser Types
    // ========================================

    @Benchmark
    public ResolvedBrowserArguments benchmarkFirefoxResolution()
    {
        return loadedProvider.resolve(StandardBrowser.FIREFOX, lightContext);
    }

    @Benchmark
    public ResolvedBrowserArguments benchmarkEdgeResolution()
    {
        return loadedProvider.resolve(StandardBrowser.EDGE, lightContext);
    }

    // ========================================
    // Scaling Benchmarks
    // ========================================

    @Benchmark
    public ResolvedBrowserArguments benchmarkManyArguments()
    {
        JourneyContext context = new JourneyContext();
        for (int i = 0; i < 50; i++)
        {
            context.getBrowserArguments().addGlobal(List.of("--arg" + i + "=value" + i));
        }
        return loadedProvider.resolve(StandardBrowser.CHROME, context);
    }

    // ========================================
    // Helper Methods
    // ========================================

    private JourneyContext createEmptyJourneyContext()
    {
        return new JourneyContext();
    }

    private JourneyContext createLightJourneyContext()
    {
        JourneyContext context = new JourneyContext();
        context.getBrowserArguments().addGlobal(List.of("--test-arg"));
        context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, List.of("--chrome-test"));
        return context;
    }

    private JourneyContext createHeavyJourneyContext()
    {
        JourneyContext context = new JourneyContext();
        
        // Add multiple global arguments
        context.getBrowserArguments().addGlobal(List.of(
            "--user-agent=Heavy Test Browser",
            "--window-size=1920x1080",
            "--disable-background-timer-throttling",
            "--disable-renderer-backgrounding",
            "--disable-backgrounding-occluded-windows"
        ));
        
        // Add browser-specific arguments
        context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, List.of(
            "--disable-extensions",
            "--disable-plugins",
            "--disable-images",
            "--disable-javascript",
            "--no-sandbox"
        ));
        
        return context;
    }

    // ========================================
    // Main Method for Standalone Execution
    // ========================================

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(BrowserArgumentsBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
