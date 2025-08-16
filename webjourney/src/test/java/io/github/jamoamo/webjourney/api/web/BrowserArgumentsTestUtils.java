/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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
package io.github.jamoamo.webjourney.api.web;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility methods for browser arguments testing.
 * Provides common assertion helpers and test data manipulation.
 *
 * @author James Amoore
 */
public final class BrowserArgumentsTestUtils
{
    private BrowserArgumentsTestUtils()
    {
        // Utility class
    }
    
    /**
     * Asserts that resolved arguments contain all expected arguments.
     */
    public static void assertArgumentsContain(ResolvedBrowserArguments resolved, String... expectedArgs)
    {
        List<String> actualArgs = resolved.getArguments();
        for (String expected : expectedArgs)
        {
            assertTrue(actualArgs.contains(expected), 
                "Expected argument not found: " + expected + ". Actual: " + actualArgs);
        }
    }
    
    /**
     * Asserts that resolved arguments do not contain any of the specified arguments.
     */
    public static void assertArgumentsDoNotContain(ResolvedBrowserArguments resolved, String... forbiddenArgs)
    {
        List<String> actualArgs = resolved.getArguments();
        for (String forbidden : forbiddenArgs)
        {
            assertFalse(actualArgs.contains(forbidden), 
                "Forbidden argument found: " + forbidden + ". Actual: " + actualArgs);
        }
    }
    
    /**
     * Asserts that an argument has the correct provenance source.
     */
    public static void assertProvenanceCorrect(ResolvedBrowserArguments resolved, String key, BrowserArgumentSource expectedSource)
    {
        ProvenancedArgument arg = findArgumentByKey(resolved, key);
        assertNotNull(arg, "Argument not found: " + key);
        assertEquals(expectedSource, arg.source(), "Wrong provenance for " + key);
    }
    
    /**
     * Finds an argument by its canonical key in the resolved arguments.
     */
    public static ProvenancedArgument findArgumentByKey(ResolvedBrowserArguments resolved, String key)
    {
        return resolved.getProvenance()
            .stream()
            .filter(arg -> key.equals(arg.key()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Asserts that arguments are in the expected order.
     */
    public static void assertArgumentOrder(ResolvedBrowserArguments resolved, String... expectedOrder)
    {
        List<String> actualArgs = resolved.getArguments();
        assertEquals(expectedOrder.length, actualArgs.size(), 
            "Expected " + expectedOrder.length + " arguments, but got " + actualArgs.size());
        
        for (int i = 0; i < expectedOrder.length; i++)
        {
            assertEquals(expectedOrder[i], actualArgs.get(i), 
                "Argument at position " + i + " does not match. Expected: " + 
                expectedOrder[i] + ", Actual: " + actualArgs.get(i));
        }
    }
    
    /**
     * Asserts that an argument with the specified key has the expected value.
     */
    public static void assertArgumentValue(ResolvedBrowserArguments resolved, String key, String expectedValue)
    {
        ProvenancedArgument arg = findArgumentByKey(resolved, key);
        assertNotNull(arg, "Argument not found: " + key);
        assertEquals(expectedValue, arg.value(), "Wrong value for argument " + key);
    }
    
    /**
     * Asserts that an argument is a key-only flag (no value).
     */
    public static void assertArgumentIsKeyOnly(ResolvedBrowserArguments resolved, String key)
    {
        ProvenancedArgument arg = findArgumentByKey(resolved, key);
        assertNotNull(arg, "Argument not found: " + key);
        assertNull(arg.value(), "Expected key-only argument, but found value: " + arg.value());
    }
    
    /**
     * Counts the number of arguments with the specified source.
     */
    public static long countArgumentsBySource(ResolvedBrowserArguments resolved, BrowserArgumentSource source)
    {
        return resolved.getProvenance()
            .stream()
            .filter(arg -> source.equals(arg.source()))
            .count();
    }
    
    /**
     * Asserts that the resolution contains exactly the expected number of arguments from each source.
     */
    public static void assertSourceCounts(ResolvedBrowserArguments resolved, 
                                         long expectedGlobal, 
                                         long expectedPerBrowser, 
                                         long expectedEnvironment, 
                                         long expectedPerJourney)
    {
        assertEquals(expectedGlobal, countArgumentsBySource(resolved, BrowserArgumentSource.GLOBAL_CONFIG),
            "Wrong number of global config arguments");
        assertEquals(expectedPerBrowser, countArgumentsBySource(resolved, BrowserArgumentSource.PER_BROWSER_CONFIG),
            "Wrong number of per-browser config arguments");
        assertEquals(expectedEnvironment, countArgumentsBySource(resolved, BrowserArgumentSource.ENVIRONMENT),
            "Wrong number of environment arguments");
        assertEquals(expectedPerJourney, countArgumentsBySource(resolved, BrowserArgumentSource.PER_JOURNEY),
            "Wrong number of per-journey arguments");
    }
    
    /**
     * Extracts the canonical key from an argument string.
     * Handles both key-only and key=value forms.
     */
    public static String extractCanonicalKey(String argument)
    {
        if (argument == null)
        {
            return null;
        }
        
        int equalsIndex = argument.indexOf('=');
        return equalsIndex >= 0 ? argument.substring(0, equalsIndex) : argument;
    }
    
    /**
     * Extracts the value from a key=value argument, or returns null for key-only.
     */
    public static String extractValue(String argument)
    {
        if (argument == null)
        {
            return null;
        }
        
        int equalsIndex = argument.indexOf('=');
        return equalsIndex >= 0 ? argument.substring(equalsIndex + 1) : null;
    }
    
    /**
     * Creates a test ResolvedBrowserArguments for testing purposes.
     */
    public static ResolvedBrowserArguments createTestResolvedArguments(String... arguments)
    {
        List<String> argList = List.of(arguments);
        List<ProvenancedArgument> provenance = argList.stream()
            .map(arg -> new ProvenancedArgument(
                extractCanonicalKey(arg),
                extractValue(arg),
                BrowserArgumentSource.GLOBAL_CONFIG
            ))
            .toList();
        
        return new ResolvedBrowserArguments(argList, provenance);
    }
    
    /**
     * Verifies that redaction was applied correctly to sensitive arguments.
     */
    public static void assertRedactionApplied(String original, String redacted)
    {
        assertNotEquals(original, redacted, "Redaction should have been applied");
        assertTrue(redacted.contains("***"), "Redacted value should contain ***");
        
        // Verify the key is preserved
        String originalKey = extractCanonicalKey(original);
        String redactedKey = extractCanonicalKey(redacted);
        assertEquals(originalKey, redactedKey, "Key should be preserved during redaction");
    }
    
    /**
     * Verifies that no redaction was applied to non-sensitive arguments.
     */
    public static void assertNoRedactionApplied(String original, String processed)
    {
        assertEquals(original, processed, "No redaction should have been applied to non-sensitive argument");
    }
}
