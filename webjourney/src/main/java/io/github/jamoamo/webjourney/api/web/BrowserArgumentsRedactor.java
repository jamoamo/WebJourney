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
package io.github.jamoamo.webjourney.api.web;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redacts sensitive values from browser arguments for safe logging.
 * 
 * <p>Applies redaction rules to URLs with credentials, key-value pairs with sensitive keys,
 * and generic user:pass patterns. Ensures idempotency so double redaction doesn't cause issues.
 */
public final class BrowserArgumentsRedactor
{
    /** Default set of canonical keys whose values should be redacted */
    private static final Set<String> DEFAULT_SENSITIVE_KEYS = Set.of(
        "--proxy-server",
        "--proxy-auth", 
        "--password",
        "--token",
        "--auth",
        "--authorization"
    );

    /** Pattern to match URL credentials: scheme://user:pass@host */
    private static final Pattern URL_CREDENTIALS_PATTERN = 
        Pattern.compile("(\\w+://)([^:/@]+):([^@]+)@");

    /** Pattern to match generic user:pass@ patterns - exclude URLs that are already handled */
    private static final Pattern GENERIC_CREDENTIALS_PATTERN = 
        Pattern.compile("(?<!://)(\\w+):(\\w+)@");

    /** Redaction replacement for credentials */
    private static final String CREDENTIALS_REPLACEMENT = "***:***@";

    /** Redaction replacement for values */
    private static final String VALUE_REPLACEMENT = "***";

    private final Set<String> sensitiveKeys;

    /**
     * Creates a redactor with default sensitive keys.
     */
    public BrowserArgumentsRedactor()
    {
        this(Set.of());
    }

    /**
     * Creates a redactor with additional sensitive keys beyond the defaults.
     * 
     * @param extraSensitiveKeys additional canonical keys to treat as sensitive
     */
    public BrowserArgumentsRedactor(Set<String> extraSensitiveKeys)
    {
        Set<String> combined = new java.util.HashSet<>(DEFAULT_SENSITIVE_KEYS);
        combined.addAll(extraSensitiveKeys);
        this.sensitiveKeys = Set.copyOf(combined);
    }

    /**
     * Redacts sensitive values from a list of tokens.
     * 
     * @param tokens the tokens to redact
     * @return new list with redacted tokens
     */
    public List<String> redact(List<String> tokens)
    {
        if (tokens == null)
        {
            return List.of();
        }

        return tokens.stream()
            .map(this::redactToken)
            .toList();
    }

    /**
     * Redacts sensitive values from a single token.
     * 
     * <p>Applies redaction rules in order:
     * <ol>
     * <li>URL credentials: scheme://user:pass@ → scheme://***:***@</li>
     * <li>Generic user:pass@ patterns: replace with ***:***@</li>
     * <li>Key-value sensitive pairs: if canonical key is sensitive and token contains =value, replace value with ***</li>
     * </ol>
     * 
     * <p>Ensures idempotency - already redacted values won't be modified further.
     * 
     * @param token the token to redact
     * @return redacted token
     */
    public String redactToken(String token)
    {
        if (token == null || token.trim().isEmpty())
        {
            return token;
        }

        String result = token;

        // 1. Redact URL credentials: scheme://user:pass@host
        result = redactUrlCredentials(result);

        // 2. Redact generic user:pass@ patterns
        result = redactGenericCredentials(result);

        // 3. Redact key-value pairs for sensitive keys (only if no specific credentials found)
        result = redactSensitiveKeyValue(result);

        return result;
    }

    /**
     * Redacts URL credentials from a token.
     */
    private String redactUrlCredentials(String token)
    {
        Matcher matcher = URL_CREDENTIALS_PATTERN.matcher(token);
        if (matcher.find())
        {
            // Don't re-redact already redacted credentials
            String userPart = matcher.group(2);
            String passPart = matcher.group(3);
            if ("***".equals(userPart) && "***".equals(passPart))
            {
                return token;
            }
            
            return matcher.replaceAll("$1" + CREDENTIALS_REPLACEMENT);
        }
        return token;
    }

    /**
     * Redacts values for sensitive key-value pairs.
     * Only redacts if the value doesn't already contain redacted credentials.
     */
    private String redactSensitiveKeyValue(String token)
    {
        int equalsIndex = token.indexOf('=');
        if (equalsIndex < 0 || equalsIndex == token.length() - 1)
        {
            return token; // No value part to redact
        }

        String canonicalKey = BrowserArgumentsMerge.canonicalKey(token.trim());
        if (!sensitiveKeys.contains(canonicalKey))
        {
            return token; // Key is not sensitive
        }

        String value = token.substring(equalsIndex + 1);
        if (VALUE_REPLACEMENT.equals(value))
        {
            return token; // Already redacted
        }

        // Don't redact if value already contains redacted credentials (***:***@)
        if (value.contains("***:***@"))
        {
            return token; // Already has redacted credentials, don't replace entire value
        }

        return token.substring(0, equalsIndex + 1) + VALUE_REPLACEMENT;
    }

    /**
     * Redacts generic user:pass@ patterns.
     */
    private String redactGenericCredentials(String token)
    {
        Matcher matcher = GENERIC_CREDENTIALS_PATTERN.matcher(token);
        if (matcher.find())
        {
            // Don't re-redact already redacted credentials
            String userPart = matcher.group(1);
            String passPart = matcher.group(2);
            if ("***".equals(userPart) && "***".equals(passPart))
            {
                return token;
            }
            
            return matcher.replaceAll(CREDENTIALS_REPLACEMENT);
        }
        return token;
    }

    /**
     * Gets a copy of the sensitive keys used by this redactor.
     * 
     * @return the sensitive keys
     */
    public Set<String> getSensitiveKeys()
    {
        return sensitiveKeys;
    }
}
