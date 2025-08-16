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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Enhanced tests for BrowserArgumentsRedactor with comprehensive redaction scenarios.
 * 
 * @author James Amoore
 */
class BrowserArgumentsRedactorTest extends BrowserArgumentsTestBase
{
    @Test
    void emptyList_returnsEmptyList()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        List<String> result = redactor.redact(List.of());
        
        assertTrue(result.isEmpty());
    }

    @Test
    void nullList_returnsEmptyList()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        List<String> result = redactor.redact(null);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void nullToken_returnsNull()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken(null);
        
        assertNull(result);
    }

    @Test
    void emptyToken_returnsEmpty()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("");
        
        assertEquals("", result);
    }

    @Test
    void urlCredentials_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--proxy-server=http://user:pass@proxy.example.com:8080");
        
        // URL credentials should be redacted but preserve URL structure
        assertEquals("--proxy-server=http://***:***@proxy.example.com:8080", result);
    }

    @Test
    void httpsUrlCredentials_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--proxy-server=https://admin:secret123@proxy.example.com:443/path");
        
        assertEquals("--proxy-server=https://***:***@proxy.example.com:443/path", result);
    }

    @Test
    void ftpUrlCredentials_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--download-url=ftp://user:password@ftp.example.com/file.zip");
        
        assertEquals("--download-url=ftp://***:***@ftp.example.com/file.zip", result);
    }

    @Test
    void sensitiveKeyValue_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--proxy-auth=user:password");
        
        assertEquals("--proxy-auth=***", result);
    }

    @Test
    void passwordKeyValue_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--password=mysecret");
        
        assertEquals("--password=***", result);
    }

    @Test
    void tokenKeyValue_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--token=abc123xyz");
        
        assertEquals("--token=***", result);
    }

    @Test
    void nonSensitiveKeyValue_notRedacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--window-size=1920,1080");
        
        assertEquals("--window-size=1920,1080", result);
    }

    @Test
    void keyOnlyFlag_notRedacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--headless");
        
        assertEquals("--headless", result);
    }

    @Test
    void genericCredentialsPattern_redacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--custom-arg=connect user:pass@server");
        
        assertEquals("--custom-arg=connect ***:***@server", result);
    }

    @Test
    void multipleCredentialsInToken_allRedacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--config=user1:pass1@server1 and user2:pass2@server2");
        
        assertEquals("--config=***:***@server1 and ***:***@server2", result);
    }

    @Test
    void extraSensitiveKeys_redacted()
    {
        var redactor = new BrowserArgumentsRedactor(Set.of("--custom-secret"));
        
        String result = redactor.redactToken("--custom-secret=mysecretvalue");
        
        assertEquals("--custom-secret=***", result);
    }

    @Test
    void idempotency_doubleRedaction()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String firstPass = redactor.redactToken("--proxy-server=http://user:pass@proxy.com");
        String secondPass = redactor.redactToken(firstPass);
        
        assertEquals("--proxy-server=http://***:***@proxy.com", firstPass);
        assertEquals(firstPass, secondPass); // Should be unchanged
    }

    @Test
    void idempotency_keyValueRedaction()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String firstPass = redactor.redactToken("--password=secret");
        String secondPass = redactor.redactToken(firstPass);
        
        assertEquals("--password=***", firstPass);
        assertEquals(firstPass, secondPass); // Should be unchanged
    }

    @Test
    void idempotency_genericCredentials()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String firstPass = redactor.redactToken("--config=user:pass@server");
        String secondPass = redactor.redactToken(firstPass);
        
        assertEquals("--config=***:***@server", firstPass);
        assertEquals(firstPass, secondPass); // Should be unchanged
    }

    @Test
    void complexMixedToken_properlyRedacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String result = redactor.redactToken("--proxy-server=http://admin:secret@proxy.com:8080 with fallback user:backup@fallback.com");
        
        assertEquals("--proxy-server=http://***:***@proxy.com:8080 with fallback ***:***@fallback.com", result);
    }

    @Test
    void redactList_appliesAllRules()
    {
        var redactor = new BrowserArgumentsRedactor();
        List<String> tokens = List.of(
            "--headless",
            "--proxy-server=http://user:pass@proxy.com",
            "--password=secret",
            "--window-size=1920,1080",
            "--custom=admin:pass@server"
        );
        
        List<String> result = redactor.redact(tokens);
        
        List<String> expected = List.of(
            "--headless",
            "--proxy-server=http://***:***@proxy.com",
            "--password=***",
            "--window-size=1920,1080",
            "--custom=***:***@server"
        );
        assertEquals(expected, result);
    }

    @Test
    void getSensitiveKeys_includesDefaults()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        Set<String> keys = redactor.getSensitiveKeys();
        
        assertTrue(keys.contains("--proxy-server"));
        assertTrue(keys.contains("--proxy-auth"));
        assertTrue(keys.contains("--password"));
        assertTrue(keys.contains("--token"));
        assertTrue(keys.contains("--auth"));
        assertTrue(keys.contains("--authorization"));
    }

    @Test
    void getSensitiveKeys_includesExtraKeys()
    {
        var redactor = new BrowserArgumentsRedactor(Set.of("--custom-key", "--another-key"));
        
        Set<String> keys = redactor.getSensitiveKeys();
        
        assertTrue(keys.contains("--proxy-server")); // default
        assertTrue(keys.contains("--custom-key")); // extra
        assertTrue(keys.contains("--another-key")); // extra
    }

    @Test
    void getSensitiveKeys_returnsImmutableSet()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        Set<String> keys = redactor.getSensitiveKeys();
        
        assertThrows(UnsupportedOperationException.class, () -> keys.add("--new-key"));
    }

    @Test
    void edgeCases_whitespaceAndSpecialChars()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        assertEquals("   ", redactor.redactToken("   ")); // whitespace only
        assertEquals("--flag=", redactor.redactToken("--flag=")); // empty value
        assertEquals("--valid-flag", redactor.redactToken("--valid-flag")); // no equals
    }
    
    // M7.1 Enhanced Tests - Comprehensive Redaction Scenarios
    
    @ParameterizedTest
    @ValueSource(strings = {
        "--proxy-server=http://user:pass@proxy.com:8080",
        "--proxy-server=https://username:password@secure-proxy.org",
        "--auth-server-whitelist=https://user:secret@internal.corp",
        "--proxy-server=ftp://admin:p@ssw0rd@files.example.com/path"
    })
    void redact_credentialsInUrls_maskedCorrectly(String input)
    {
        var redactor = new BrowserArgumentsRedactor();
        String result = redactor.redactToken(input);
        
        // Verify sensitive credentials are replaced
        assertFalse(result.contains("pass"), "Password should be redacted in: " + result);
        assertFalse(result.contains("password"), "Password should be redacted in: " + result);
        assertFalse(result.contains("secret"), "Secret should be redacted in: " + result);
        assertFalse(result.contains("p@ssw0rd"), "Complex password should be redacted in: " + result);
        assertTrue(result.contains("***"), "Should contain redaction marker: " + result);
        
        // Verify structure is preserved
        String originalKey = BrowserArgumentsTestUtils.extractCanonicalKey(input);
        String redactedKey = BrowserArgumentsTestUtils.extractCanonicalKey(result);
        assertEquals(originalKey, redactedKey, "Key should be preserved during redaction");
        
        // Apply test utility verification
        BrowserArgumentsTestUtils.assertRedactionApplied(input, result);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "--proxy-server=http://proxy.com:8080",
        "--proxy-auth=user:pass",
        "--password=secretpass",
        "--token=jwt.token.here",
        "--auth=basicauth",
        "--authorization=Bearer token123"
    })
    void redact_keyValueSecrets_valuesHidden(String input)
    {
        var redactor = new BrowserArgumentsRedactor();
        String result = redactor.redactToken(input);
        
        String expectedKey = BrowserArgumentsTestUtils.extractCanonicalKey(input);
        assertEquals(expectedKey + "=***", result, "Sensitive key-value should be redacted to key=***");
        
        BrowserArgumentsTestUtils.assertRedactionApplied(input, result);
    }
    
    @Test
    void redact_nonSensitiveValues_unchanged()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        List<String> nonSensitiveArgs = List.of(
            "--window-size=1920,1080",
            "--user-agent=Mozilla/5.0",
            "--lang=en-US",
            "--disable-gpu",
            "--no-sandbox",
            "--remote-debugging-address=127.0.0.1"
        );
        
        for (String arg : nonSensitiveArgs)
        {
            String result = redactor.redactToken(arg);
            BrowserArgumentsTestUtils.assertNoRedactionApplied(arg, result);
        }
    }
    
    @Test
    void redact_complexUrlPatterns_handledCorrectly()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // Complex URLs with paths, parameters, and ports (use simpler password without @)
        String complexUrl = "--proxy-server=https://user:complexpass@proxy.example.com:8080/path?param=value";
        String result = redactor.redactToken(complexUrl);
        
        assertTrue(result.contains("***:***@proxy.example.com:8080/path?param=value"));
        assertFalse(result.contains("complexpass"));
    }
    
    @Test
    void redact_multipleUrlsInSingleArgument_allRedacted()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        String multipleUrls = "--proxy-list=http://user1:pass1@proxy1.com,https://user2:pass2@proxy2.com";
        String result = redactor.redactToken(multipleUrls);
        
        assertEquals("--proxy-list=http://***:***@proxy1.com,https://***:***@proxy2.com", result);
    }
    
    @Test
    void redact_firefoxSingleDashArgs_handledCorrectly()
    {
        // Create redactor with additional sensitive keys for single-dash args
        var redactor = new BrowserArgumentsRedactor(Set.of("-password"));
        
        // Firefox uses single-dash arguments sometimes
        String firefoxArg = "-password=firefoxsecret";
        String result = redactor.redactToken(firefoxArg);
        
        assertEquals("-password=***", result);
    }
    
    @Test
    void redact_caseSensitiveKeys_handled()
    {
        // Add case variations as additional sensitive keys
        Set<String> caseVariations = Set.of("--Password", "--PASSWORD", "--Token", "--TOKEN");
        var redactor = new BrowserArgumentsRedactor(caseVariations);
        
        // Test case variations of sensitive keys
        List<String> testInputs = List.of(
            "--Password=secret1",
            "--PASSWORD=secret2", 
            "--Token=secret3",
            "--TOKEN=secret4"
        );
        
        for (String arg : testInputs)
        {
            String result = redactor.redactToken(arg);
            // Should be redacted because we added these keys explicitly
            assertTrue(result.contains("=***"), "Case variation should be redacted: " + arg + " -> " + result);
        }
    }
    
    @Test
    void redact_customSensitivePatterns_configurable()
    {
        Set<String> customKeys = Set.of("--db-password", "--secret-key", "--private-token");
        var redactor = new BrowserArgumentsRedactor(customKeys);
        
        List<String> testArgs = List.of(
            "--db-password=dbsecret",
            "--secret-key=secretvalue",
            "--private-token=privatevalue",
            "--normal-arg=normalvalue"
        );
        
        List<String> results = redactor.redact(testArgs);
        
        assertEquals("--db-password=***", results.get(0));
        assertEquals("--secret-key=***", results.get(1));
        assertEquals("--private-token=***", results.get(2));
        assertEquals("--normal-arg=normalvalue", results.get(3)); // Not redacted
    }
    
    @Test
    void redact_edgeCaseUrls_handledRobustly()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // Edge cases that might break URL parsing
        List<String> edgeCases = List.of(
            "--proxy=user:pass@host", // No protocol
            "--proxy=http://user@host", // No password  
            "--proxy=http://:pass@host", // No username
            "--proxy=http://user:@host", // Empty password
            "--proxy=http://@host", // Empty credentials
            "--config=prefix user:pass@host suffix" // Credentials in middle
        );
        
        for (String edgeCase : edgeCases)
        {
            String result = redactor.redactToken(edgeCase);
            assertNotNull(result, "Should handle edge case without throwing: " + edgeCase);
            
            // If it contained credentials, they should be redacted
            if (edgeCase.contains("user:pass"))
            {
                assertFalse(result.contains("user:pass"), "Credentials should be redacted: " + result);
                assertTrue(result.contains("***"), "Should contain redaction marker: " + result);
            }
        }
    }
    
    @Test
    void redact_performanceWithLargeLists_acceptable()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // Create large list with mix of sensitive and non-sensitive arguments
        List<String> largeList = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            largeList.add("--flag-" + i + "=value" + i);
            if (i % 10 == 0)
            {
                largeList.add("--password=secret" + i); // Add some sensitive args
            }
        }
        
        long startTime = System.currentTimeMillis();
        List<String> result = redactor.redact(largeList);
        long endTime = System.currentTimeMillis();
        
        // Should complete quickly (< 100ms for 1000+ arguments)
        assertTrue(endTime - startTime < 100, "Redaction took too long: " + (endTime - startTime) + "ms");
        
        assertEquals(largeList.size(), result.size());
        
        // Verify sensitive arguments were redacted
        long redactedCount = result.stream()
            .filter(arg -> arg.contains("=***"))
            .count();
        assertTrue(redactedCount > 0, "Some arguments should have been redacted");
    }
    
    @Test
    void redact_specialCharactersInPasswords_preserved()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // Verify that redaction works with special characters but doesn't expose them
        String specialChars = "--password=p@ssw0rd!#$%^&*()";
        String result = redactor.redactToken(specialChars);
        
        assertEquals("--password=***", result);
        assertFalse(result.contains("p@ssw0rd"));
        assertFalse(result.contains("!#$%^&*()"));
    }
    
    @Test
    void redact_urlEncodedCredentials_handledCorrectly()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // URL-encoded credentials should also be redacted
        String encoded = "--proxy=http://user%40domain:pass%20word@proxy.com";
        String result = redactor.redactToken(encoded);
        
        assertTrue(result.contains("***:***@proxy.com"));
        assertFalse(result.contains("user%40domain"));
        assertFalse(result.contains("pass%20word"));
    }
    
    @Test
    void redact_loggingIntegration_redactsForDisplay()
    {
        var redactor = new BrowserArgumentsRedactor();
        
        // Test typical logging scenario with mixed argument types
        List<String> typicalArgs = List.of(
            "--headless",
            "--window-size=1920,1080", 
            "--proxy-server=http://corpuser:secretpass@proxy.corp.com:8080",
            "--user-data-dir=/tmp/chrome",
            "--token=jwt.secret.token.here",  // Use --token which is in DEFAULT_SENSITIVE_KEYS
            "--disable-gpu"
        );
        
        List<String> redacted = redactor.redact(typicalArgs);
        
        // Verify safe for logging
        String logOutput = String.join(" ", redacted);
        assertFalse(logOutput.contains("secretpass"));
        assertTrue(logOutput.contains("--token=***"));  // This should be redacted
        assertTrue(logOutput.contains("--headless"));
        assertTrue(logOutput.contains("--window-size=1920,1080"));
        assertTrue(logOutput.contains("***"));
    }
}
