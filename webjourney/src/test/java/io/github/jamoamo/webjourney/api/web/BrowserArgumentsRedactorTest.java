package io.github.jamoamo.webjourney.api.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BrowserArgumentsRedactorTest
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
}
