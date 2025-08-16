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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Enhanced tests for BrowserArgParser with comprehensive edge case coverage.
 * 
 * @author James Amoore
 */
class BrowserArgParserTest extends BrowserArgumentsTestBase
{
    @Test
    void parse_empty_returnsEmpty()
    {
        assertEquals(List.of(), BrowserArgParser.parse(null));
        assertEquals(List.of(), BrowserArgParser.parse("   "));
        assertEquals(List.of(), BrowserArgParser.parse(""));
    }

    @Test
    void parse_simpleCsv_trimsAndKeepsOrder()
    {
        List<String> out = BrowserArgParser.parse("--headless,  --foo=bar ,baz");
        assertEquals(List.of("--headless", "--foo=bar", "baz"), out);
    }

    @Test
    void parse_quotes_preservedInsideThenStripped()
    {
        List<String> out = BrowserArgParser.parse("\"--profile=My Profile\", '--flag', value");
        // --flag value should canonicalize to --flag=value per design
        assertEquals(List.of("--profile=My Profile", "--flag=value"), out);
    }

    @Test
    void parse_escapedComma_insideToken()
    {
        List<String> out = BrowserArgParser.parse("--proxy=host\\,port, --x");
        assertEquals(List.of("--proxy=host,port", "--x"), out);
    }

    @Test
    void normalize_spaceSeparatedToEquals_forDoubleDashOnly()
    {
        List<String> out = BrowserArgParser.normalize(List.of("--key", "value", "-k", "v", "--a=b"));
        assertEquals(List.of("--key=value", "-k", "v", "--a=b"), out);
    }
    
    // M7.1 Enhanced Tests - Edge Cases and Platform Handling
    
    @ParameterizedTest
    @ValueSource(strings = {
        "\"--flag with spaces\"",
        "'--single-quoted'", 
        "--key=\"value with spaces\"",
        "--proxy=host\\,port:8080",
        "\"--user-data-dir=C:\\Program Files\\Chrome\"",
        "'--proxy-server=https://proxy.com:8080'"
    })
    void parse_quotingEdgeCases_handledCorrectly(String input)
    {
        List<String> result = BrowserArgParser.parse(input);
        
        // Verify we get exactly one token
        assertEquals(1, result.size(), "Should parse single quoted argument correctly");
        
        // Verify quotes are stripped but content preserved
        String parsed = result.get(0);
        if (input.startsWith("\"--flag with spaces\""))
        {
            assertEquals("--flag with spaces", parsed);
        }
        else if (input.startsWith("'--single-quoted'"))
        {
            assertEquals("--single-quoted", parsed);
        }
        else if (input.startsWith("--key=\"value with spaces\""))
        {
            assertEquals("--key=\"value with spaces\"", parsed);
        }
        else if (input.startsWith("--proxy=host\\,port:8080"))
        {
            assertEquals("--proxy=host,port:8080", parsed);
        }
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void parse_windowsPathSeparators_preservedCorrectly()
    {
        // Test Windows-specific path handling
        String input = "--user-data-dir=C:\\Users\\Test\\Chrome";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--user-data-dir=C:\\Users\\Test\\Chrome"), result);
        
        // Test with quotes
        String quotedInput = "\"--user-data-dir=C:\\Program Files\\Google\\Chrome\"";
        List<String> quotedResult = BrowserArgParser.parse(quotedInput);
        assertEquals(List.of("--user-data-dir=C:\\Program Files\\Google\\Chrome"), quotedResult);
    }
    
    @Test
    void parse_unixPathSeparators_preservedCorrectly()
    {
        // Test Unix-style paths
        String input = "--user-data-dir=/home/user/.config/chrome";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--user-data-dir=/home/user/.config/chrome"), result);
        
        // Test with spaces requiring quotes
        String quotedInput = "\"--user-data-dir=/home/user/My Chrome Profile\"";
        List<String> quotedResult = BrowserArgParser.parse(quotedInput);
        assertEquals(List.of("--user-data-dir=/home/user/My Chrome Profile"), quotedResult);
    }
    
    @Test
    void normalize_firefoxSingleDash_preservedAsIs()
    {
        // Test Firefox single-dash arguments are not converted to double-dash
        List<String> input = List.of("-headless", "--chrome-flag", "-safe-mode", "--window-size=1024,768");
        List<String> result = BrowserArgParser.normalize(input);
        assertEquals(List.of("-headless", "--chrome-flag", "-safe-mode", "--window-size=1024,768"), result);
    }
    
    @Test
    void parse_mixedQuotingStyles_handledCorrectly()
    {
        String input = "\"--flag1=value1\", '--flag2=value2', --flag3=value3, \"--flag4\"";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--flag1=value1", "--flag2=value2", "--flag3=value3", "--flag4"), result);
    }
    
    @Test
    void parse_emptyTokensInSequence_filtered()
    {
        String input = "--flag1,,  ,--flag2";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--flag1", "--flag2"), result);
    }
    
    @Test
    void parse_complexEscaping_handledCorrectly()
    {
        // Test escaped commas and quotes together
        String input = "--proxy=user\\,name:pass@host\\,backup:8080, --other";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--proxy=user,name:pass@host,backup:8080", "--other"), result);
    }
    
    @Test
    void normalize_consecutiveSpaceSeparatedArgs_pairedCorrectly()
    {
        // Test that space-separated args are paired correctly
        List<String> input = List.of("--key1", "value1", "--key2", "value2", "--key3");
        List<String> result = BrowserArgParser.normalize(input);
        assertEquals(List.of("--key1=value1", "--key2=value2", "--key3"), result);
    }
    
    @Test
    void normalize_mixedFormats_handledConsistently()
    {
        // Test mixing equals and space-separated formats
        List<String> input = List.of("--key1=value1", "--key2", "value2", "-k", "v", "--key3");
        List<String> result = BrowserArgParser.normalize(input);
        assertEquals(List.of("--key1=value1", "--key2=value2", "-k", "v", "--key3"), result);
    }
    
    @Test
    void parse_specialCharacters_preservedInValues()
    {
        String input = "--proxy-server=https://user:p@ss!w0rd@proxy.com:8080";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--proxy-server=https://user:p@ss!w0rd@proxy.com:8080"), result);
    }
    
    @Test
    void parse_unicodeCharacters_preservedCorrectly()
    {
        String input = "--lang=zh-CN, --user-agent=\"Mozilla Firefox 中文版\"";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--lang=zh-CN", "--user-agent=\"Mozilla Firefox 中文版\""), result);
    }
    
    @Test
    void normalize_edgeCaseSpacing_handledRobustly()
    {
        // Test edge cases with spacing around equals
        List<String> input = List.of("--key1", " = ", "value1", "--key2", "value2");
        List<String> result = BrowserArgParser.normalize(input);
        // The normalize function treats " = " as a non-value and "value1" as a separate key=value
        assertEquals(List.of("--key1==", "value1", "--key2=value2"), result);
    }
}



