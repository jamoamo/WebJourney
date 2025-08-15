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
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Validates browser arguments against a deny-list with configurable failure modes.
 * 
 * <p>Uses {@link BrowserArgumentsMerge#canonicalKey(String)} to normalize argument keys
 * for consistent matching across the system.
 */
public final class BrowserArgumentsValidator
{
    /**
     * Validation mode determining behavior when denied arguments are detected.
     */
    public enum ValidationMode
    {
        /** Throw an exception when denied arguments are found */
        REJECT,
        /** Log a warning and drop denied arguments */
        WARN
    }

    /**
     * Result of validation containing allowed arguments and any violations.
     */
    public static final class ValidationResult
    {
        private final List<String> allowed;
        private final List<String> violations;

        private ValidationResult(List<String> allowed, List<String> violations)
        {
            this.allowed = Collections.unmodifiableList(allowed);
            this.violations = Collections.unmodifiableList(violations);
        }

        /**
         * Gets the list of allowed arguments (those not denied).
         * 
         * @return the allowed arguments
         */
        public List<String> getAllowed()
        {
            return allowed;
        }

        /**
         * Gets the list of original tokens that triggered deny-list violations.
         * 
         * @return the violating arguments
         */
        public List<String> getViolations()
        {
            return violations;
        }

        /**
         * Checks if any violations were detected.
         * 
         * @return true if violations exist, false otherwise
         */
        public boolean hasViolations()
        {
            return !violations.isEmpty();
        }
    }

    private final Set<String> deniedCanonicalKeys;
    private final ValidationMode mode;

    /**
     * Creates a new validator with the specified deny-list and validation mode.
     * 
     * @param deniedCanonicalKeys set of canonical keys to deny
     * @param mode validation mode for handling violations
     */
    public BrowserArgumentsValidator(Set<String> deniedCanonicalKeys, ValidationMode mode)
    {
        this.deniedCanonicalKeys = Set.copyOf(deniedCanonicalKeys);
        this.mode = mode;
    }

    /**
     * Validates the provided tokens against the deny-list.
     * 
     * <p>Canonicalizes each token using {@link BrowserArgumentsMerge#canonicalKey(String)}
     * and checks against the deny-list. Behavior depends on the validation mode:
     * <ul>
     * <li>{@code REJECT}: Throws {@link IllegalArgumentException} with clear message</li>
     * <li>{@code WARN}: Returns allowed tokens without the offending ones</li>
     * </ul>
     * 
     * @param tokens the browser argument tokens to validate
     * @return validation result with allowed tokens and any violations
     * @throws IllegalArgumentException if mode is REJECT and violations are found
     */
    public ValidationResult validate(List<String> tokens)
    {
        if (tokens == null || tokens.isEmpty())
        {
            return new ValidationResult(Collections.emptyList(), Collections.emptyList());
        }

        List<String> allowed = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        for (String token : tokens)
        {
            if (token == null)
            {
                continue;
            }

            String trimmed = token.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }

            String canonicalKey = BrowserArgumentsMerge.canonicalKey(trimmed);
            if (deniedCanonicalKeys.contains(canonicalKey))
            {
                violations.add(token);
            }
            else
            {
                allowed.add(token);
            }
        }

        ValidationResult result = new ValidationResult(allowed, violations);

        if (result.hasViolations() && mode == ValidationMode.REJECT)
        {
            List<String> deniedKeys = violations.stream()
                .map(token -> BrowserArgumentsMerge.canonicalKey(token.trim()))
                .distinct()
                .toList();
            
            throw new IllegalArgumentException(
                "Denied browser arguments detected: " + deniedKeys + 
                ". Configure `browser.args.validation.mode=warn` to drop instead, or remove/override via config."
            );
        }

        return result;
    }

    /**
     * Gets the validation mode.
     * 
     * @return the validation mode
     */
    public ValidationMode getMode()
    {
        return mode;
    }

    /**
     * Gets a copy of the denied canonical keys.
     * 
     * @return the denied keys
     */
    public Set<String> getDeniedCanonicalKeys()
    {
        return deniedCanonicalKeys;
    }
}
