package io.github.jamoamo.webjourney.api.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BrowserArgumentsValidatorTest
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
}
