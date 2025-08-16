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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Enhanced tests for BrowserArgumentsValidator with comprehensive validation scenarios.
 * 
 * @author James Amoore
 */
class BrowserArgumentsValidatorTest extends BrowserArgumentsTestBase
{
    private static final Set<String> DEFAULT_DENY_LIST = Set.of(
        "--user-data-dir",
        "--remote-debugging-port",
        "--disable-web-security"
    );

    @Test
    void emptyTokens_returnsEmptyResult()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        var result = validator.validate(List.of());
        
        assertTrue(result.getAllowed().isEmpty());
        assertTrue(result.getViolations().isEmpty());
        assertFalse(result.hasViolations());
    }

    @Test
    void nullTokens_returnsEmptyResult()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        var result = validator.validate(null);
        
        assertTrue(result.getAllowed().isEmpty());
        assertTrue(result.getViolations().isEmpty());
        assertFalse(result.hasViolations());
    }

    @Test
    void allowedTokens_passValidation()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        List<String> tokens = List.of("--headless", "--window-size=1920,1080", "--no-sandbox");
        
        var result = validator.validate(tokens);
        
        assertEquals(tokens, result.getAllowed());
        assertTrue(result.getViolations().isEmpty());
        assertFalse(result.hasViolations());
    }

    @Test
    void deniedKeyOnly_detectsViolation()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        List<String> tokens = List.of("--headless", "--user-data-dir", "--no-sandbox");
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        
        assertTrue(exception.getMessage().contains("--user-data-dir"));
        assertTrue(exception.getMessage().contains("browser.args.validation.mode=warn"));
    }

    @Test
    void deniedKeyValue_detectsViolation()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        List<String> tokens = List.of("--headless", "--user-data-dir=/tmp/chrome", "--no-sandbox");
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        
        assertTrue(exception.getMessage().contains("--user-data-dir"));
    }

    @Test
    void mixedCaseAndWhitespace_normalized()
    {
        var validator = new BrowserArgumentsValidator(Set.of("--TEST-FLAG"), BrowserArgumentsValidator.ValidationMode.REJECT);
        List<String> tokens = List.of("  --TEST-FLAG=value  ", "--allowed");
        
        // Should detect --TEST-FLAG as denied (case-sensitive match)
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        
        assertTrue(exception.getMessage().contains("--TEST-FLAG"));
    }

    @Test
    void warnMode_dropsViolationsAndReturnsAllowed()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.WARN);
        List<String> tokens = List.of("--headless", "--user-data-dir=/tmp", "--window-size=1920,1080", "--remote-debugging-port=9222");
        
        var result = validator.validate(tokens);
        
        assertEquals(List.of("--headless", "--window-size=1920,1080"), result.getAllowed());
        assertEquals(List.of("--user-data-dir=/tmp", "--remote-debugging-port=9222"), result.getViolations());
        assertTrue(result.hasViolations());
    }

    @Test
    void multipleViolations_allDetected()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        List<String> tokens = List.of("--user-data-dir=/tmp", "--remote-debugging-port=9222", "--disable-web-security");
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        
        String message = exception.getMessage();
        assertTrue(message.contains("--user-data-dir"));
        assertTrue(message.contains("--remote-debugging-port"));
        assertTrue(message.contains("--disable-web-security"));
    }

    @Test
    void canonicalizationConsistency_matchesBrowserArgumentsMerge()
    {
        // Test that our canonicalization matches BrowserArgumentsMerge.canonicalKey
        var validator = new BrowserArgumentsValidator(Set.of("--test"), BrowserArgumentsValidator.ValidationMode.REJECT);
        
        // These should all be canonicalized to "--test" and caught
        assertThrows(IllegalArgumentException.class, () -> validator.validate(List.of("--test")));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(List.of("--test=value")));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(List.of("--test=key=value")));
    }

    @Test
    void nullAndEmptyTokens_ignoredSafely()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.WARN);
        List<String> tokens = new ArrayList<>(List.of("--headless", "", "  ", "--user-data-dir=/tmp"));
        tokens.add(1, null); // Add null in the middle
        
        var result = validator.validate(tokens);
        
        assertEquals(List.of("--headless"), result.getAllowed());
        assertEquals(List.of("--user-data-dir=/tmp"), result.getViolations());
    }

    @Test
    void getDeniedCanonicalKeys_returnsImmutableCopy()
    {
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        Set<String> keys = validator.getDeniedCanonicalKeys();
        
        assertEquals(DEFAULT_DENY_LIST, keys);
        assertThrows(UnsupportedOperationException.class, () -> keys.add("--new-key"));
    }

    @Test
    void getMode_returnsCorrectMode()
    {
        var rejectValidator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        var warnValidator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.WARN);
        
        assertEquals(BrowserArgumentsValidator.ValidationMode.REJECT, rejectValidator.getMode());
        assertEquals(BrowserArgumentsValidator.ValidationMode.WARN, warnValidator.getMode());
    }
    
    // M7.1 Enhanced Tests - Additional Validation Scenarios
    
    @Test
    void validate_configuredDenyList_overridesDefaults()
    {
        // Test custom deny list configuration
        Set<String> customDenyList = Set.of("--custom-denied", "--another-denied");
        var validator = new BrowserArgumentsValidator(customDenyList, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> tokens = List.of(
            "--user-data-dir=/tmp", // This should be allowed with custom deny list
            "--custom-denied", // This should be denied
            "--allowed-flag"
        );
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        
        assertTrue(exception.getMessage().contains("--custom-denied"));
        assertFalse(exception.getMessage().contains("--user-data-dir")); // Should not be mentioned since it's allowed
    }
    
    @Test
    void validate_caseSensitivity_strictMatching()
    {
        // Ensure case-sensitive matching works correctly
        Set<String> caseSensitiveDenyList = Set.of("--User-Data-Dir", "--HEADLESS");
        var validator = new BrowserArgumentsValidator(caseSensitiveDenyList, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> allowedTokens = List.of("--user-data-dir", "--headless", "--User-data-dir");
        List<String> deniedTokens = List.of("--User-Data-Dir", "--HEADLESS");
        
        // These should pass (case doesn't match deny list exactly)
        var allowedResult = validator.validate(allowedTokens);
        assertEquals(allowedTokens, allowedResult.getAllowed());
        assertFalse(allowedResult.hasViolations());
        
        // These should fail (exact case match)
        assertThrows(IllegalArgumentException.class, () -> validator.validate(deniedTokens));
    }
    
    @Test
    void validate_partialKeyMatches_notTriggered()
    {
        // Ensure --user-data-dir doesn't block --user-agent
        Set<String> denyList = Set.of("--user-data-dir");
        var validator = new BrowserArgumentsValidator(denyList, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> similarButAllowedTokens = List.of(
            "--user-agent=Mozilla/5.0",
            "--user-data-path=/tmp", // Similar but different
            "--data-dir=/tmp" // Substring but different
        );
        
        var result = validator.validate(similarButAllowedTokens);
        
        assertEquals(similarButAllowedTokens, result.getAllowed());
        assertFalse(result.hasViolations());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "--dangerous-flag",
        "--dangerous-flag=value", 
        "--dangerous-flag=complex=value=with=equals"
    })
    void validate_keyValueVariations_allDetected(String deniedArgument)
    {
        // Test that key variations are all caught
        Set<String> denyList = Set.of("--dangerous-flag");
        var validator = new BrowserArgumentsValidator(denyList, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> tokens = List.of("--safe-flag", deniedArgument);
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        assertTrue(exception.getMessage().contains("--dangerous-flag"));
    }
    
    @Test
    void validate_emptyDenyList_allowsEverything()
    {
        // Test behavior with empty deny list
        var validator = new BrowserArgumentsValidator(Set.of(), BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> tokens = List.of(
            "--user-data-dir=/tmp",
            "--remote-debugging-port=9222",
            "--disable-web-security",
            "--any-flag=any-value"
        );
        
        var result = validator.validate(tokens);
        
        assertEquals(tokens, result.getAllowed());
        assertFalse(result.hasViolations());
    }
    
    @Test
    void validate_firefoxSingleDashArgs_handledCorrectly()
    {
        // Test that Firefox single-dash arguments work with validation
        Set<String> denyList = Set.of("-profile", "--user-data-dir");
        var validator = new BrowserArgumentsValidator(denyList, BrowserArgumentsValidator.ValidationMode.WARN);
        
        List<String> tokens = List.of(
            "-headless", // Should be allowed
            "-profile=/tmp", // Should be denied
            "--user-data-dir=/tmp", // Should be denied
            "--chrome-flag" // Should be allowed
        );
        
        var result = validator.validate(tokens);
        
        assertEquals(List.of("-headless", "--chrome-flag"), result.getAllowed());
        assertEquals(List.of("-profile=/tmp", "--user-data-dir=/tmp"), result.getViolations());
        assertTrue(result.hasViolations());
    }
    
    @Test
    void validate_specialCharactersInKeys_handledCorrectly()
    {
        // Test arguments with special characters
        Set<String> denyList = Set.of("--key-with-dashes", "--key_with_underscores");
        var validator = new BrowserArgumentsValidator(denyList, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> deniedTokens = List.of("--key-with-dashes=value", "--key_with_underscores");
        List<String> allowedTokens = List.of("--key.with.dots", "--key:with:colons");
        
        // Should deny the configured keys
        assertThrows(IllegalArgumentException.class, () -> validator.validate(deniedTokens));
        
        // Should allow the non-configured keys
        var result = validator.validate(allowedTokens);
        assertEquals(allowedTokens, result.getAllowed());
    }
    
    @Test
    void validate_longArgumentList_performsWell()
    {
        // Test performance with large argument list
        Set<String> denyList = Set.of("--denied-1", "--denied-2", "--denied-3");
        var validator = new BrowserArgumentsValidator(denyList, BrowserArgumentsValidator.ValidationMode.WARN);
        
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            tokens.add("--allowed-flag-" + i + "=value" + i);
        }
        tokens.add("--denied-1"); // Add one denied argument
        
        long startTime = System.currentTimeMillis();
        var result = validator.validate(tokens);
        long endTime = System.currentTimeMillis();
        
        // Should complete quickly (< 100ms for 1000 arguments)
        assertTrue(endTime - startTime < 100, "Validation took too long: " + (endTime - startTime) + "ms");
        
        assertEquals(1000, result.getAllowed().size());
        assertEquals(1, result.getViolations().size());
        assertEquals(List.of("--denied-1"), result.getViolations());
    }
    
    @Test
    void validate_messageQuality_providesHelpfulInformation()
    {
        // Test that error messages are helpful
        var validator = new BrowserArgumentsValidator(DEFAULT_DENY_LIST, BrowserArgumentsValidator.ValidationMode.REJECT);
        
        List<String> tokens = List.of("--user-data-dir=/tmp", "--remote-debugging-port=9222");
        
        var exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(tokens));
        String message = exception.getMessage();
        
        // Should contain the denied arguments
        assertTrue(message.contains("--user-data-dir"));
        assertTrue(message.contains("--remote-debugging-port"));
        
        // Should suggest the warning mode
        assertTrue(message.contains("browser.args.validation.mode=warn"));
        
        // Should mention it's a security concern
        assertTrue(message.toLowerCase().contains("denied") || message.toLowerCase().contains("security"));
    }
}
