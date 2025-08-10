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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic merge and de-duplication of browser arguments using precedence.
 *
 * <p>Canonical key is the substring up to the first '=' if present; otherwise the token itself.
 * Later lists override earlier ones (last-writer-wins) while preserving stable order.
 */
public final class BrowserArgumentsMerge
{
    private BrowserArgumentsMerge()
    {
    }

    /**
     * Merge multiple layers of argument lists with increasing precedence.
     *
     * <p>Example order: global, per-browser, env, per-journey.
     *
     * @param layers lists in ascending precedence (lowest first, highest last)
     * @return merged list with de-duplicated canonical keys
     */
    @SafeVarargs
    public static List<String> mergeByPrecedence(final List<String>... layers)
    {
        final Map<String, String> keyToToken = new LinkedHashMap<>();
        if (layers == null)
        {
            return List.of();
        }

        for (List<String> layer : layers)
        {
            applyLayer(keyToToken, layer);
        }

        return new ArrayList<>(keyToToken.values());
    }

    private static void applyLayer(final Map<String, String> keyToToken, final List<String> layer)
    {
        if (layer == null)
        {
            return;
        }
        for (String token : layer)
        {
            if (token == null)
            {
                continue;
            }
            final String trimmed = token.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }
            final String key = canonicalKey(trimmed);
            // Override value and update order to reflect highest-precedence contributor.
            if (keyToToken.containsKey(key))
            {
                keyToToken.remove(key);
            }
            keyToToken.put(key, trimmed);
        }
    }

    /**
     * Extract canonical key from a token.
     * If the token contains '=', take substring up to the first '='; otherwise, the token itself.
     *
     * @param token the token to analyze
     * @return the canonical key for de-duplication
     */
    public static String canonicalKey(final String token)
    {
        if (token == null)
        {
            return null;
        }
        final int eq = token.indexOf('=');
        if (eq >= 0)
        {
            return token.substring(0, eq);
        }
        return token;
    }
}


