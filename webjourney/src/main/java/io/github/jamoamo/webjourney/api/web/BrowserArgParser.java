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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 * Utility to parse and normalize browser argument strings.
 *
 * <p>Responsibilities:
 * - Split comma-separated input honoring quotes and escaped commas
 * - Trim tokens and drop empties
 * - Remove surrounding matching quotes from tokens
 * - Canonicalize "--key value" to "--key=value" (only for double-dash keys)
 * - Leave single-dash flags (e.g., "-headless", "-P profile") as-is
 */
public final class BrowserArgParser
{
    private BrowserArgParser()
    {
    }

    /**
     * Parse a raw comma-separated string of arguments into a normalized list of tokens.
     *
     * <p>The input supports quoting (single and double quotes) and escaped commas (\,).
     *
     * @param raw the raw string (comma-separated, quotes allowed). May be null/blank
     * @return immutable list of normalized tokens
     */
    public static List<String> parse(final String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return List.of();
        }

        final List<String> tokens = splitCsvLike(raw);
        return Collections.unmodifiableList(normalize(tokens));
    }

    /**
     * Normalize tokens produced from any source (env/config/per-journey).
     *
     * <p>Applies trimming, quote stripping, and canonicalization of "--key value".
     *
     * @param rawTokens tokens to normalize
     * @return normalized tokens
     */
    public static List<String> normalize(final List<String> rawTokens)
    {
        if (rawTokens == null || rawTokens.isEmpty())
        {
            return List.of();
        }

        final List<String> cleaned = cleanTokens(rawTokens);
        return canonicalizeKeyValuePairs(cleaned);
    }

    private static List<String> cleanTokens(final List<String> rawTokens)
    {
        final List<String> cleaned = new ArrayList<>();
        for (String token : rawTokens)
        {
            if (token == null)
            {
                continue;
            }
            String t = token.trim();
            if (t.isEmpty())
            {
                continue;
            }
            t = stripMatchingQuotes(t);
            if (!t.isEmpty())
            {
                cleaned.add(t);
            }
        }
        return cleaned;
    }

    private static List<String> canonicalizeKeyValuePairs(final List<String> cleaned)
    {
        final List<String> result = new ArrayList<>();
        int i = 0;
        while (i < cleaned.size())
        {
            final String current = cleaned.get(i);
            if (looksLikeDoubleDashKey(current) && !containsEquals(current) && i + 1 < cleaned.size())
            {
                final String next = cleaned.get(i + 1);
                if (isValueToken(next))
                {
                    result.add(current + "=" + next);
                    i += 2;
                    continue;
                }
            }

            result.add(current);
            i += 1;
        }
        return result;
    }

    private static boolean containsEquals(final String s)
    {
        return s.indexOf('=') >= 0;
    }

    private static boolean looksLikeDoubleDashKey(final String token)
    {
        return token.startsWith("--") && token.length() > 2 && !Character.isWhitespace(token.charAt(2));
    }

    private static boolean isValueToken(final String token)
    {
        // Value tokens do not start with a dash and are not empty
        return !token.isEmpty() && token.charAt(0) != '-';
    }

    private static String stripMatchingQuotes(final String s)
    {
        if (s.length() >= 2)
        {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if (first == '"' && last == '"')
            {
                return s.substring(1, s.length() - 1);
            }
            if (first == '\'' && last == '\'')
            {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Split a comma-separated string honoring quotes and escaped commas (\\,).
     * Supports both single and double quotes. Quotes can contain commas and escaped characters.
     *
     * @param input the raw string to split
     * @return list of raw tokens (not yet normalized)
     */
    private static List<String> splitCsvLike(final String input)
    {
        if (input == null || input.isBlank())
        {
            return List.of();
        }

        final CSVFormat format = CSVFormat.DEFAULT
            .builder()
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .setEscape('\\')
            .build();

        final List<String> out = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(input, format))
        {
            if (parser.iterator().hasNext())
            {
                var record = parser.iterator().next();
                for (String value : record)
                {
                    if (value == null)
                    {
                        continue;
                    }
                    String token = value.trim();
                    if (!token.isEmpty())
                    {
                        out.add(token);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            for (String token : input.split(","))
            {
                String t = token.trim();
                if (!t.isEmpty())
                {
                    out.add(t);
                }
            }
        }

        return out;
    }
}


