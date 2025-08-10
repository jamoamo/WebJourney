package io.github.jamoamo.webjourney.api.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class BrowserArgParserTest
{
    @Test
    void parse_empty_returnsEmpty()
    {
        assertEquals(List.of(), BrowserArgParser.parse(null));
        assertEquals(List.of(), BrowserArgParser.parse("   "));
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
}



