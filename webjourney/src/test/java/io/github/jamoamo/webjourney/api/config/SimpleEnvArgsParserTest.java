package io.github.jamoamo.webjourney.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SimpleEnvArgsParser} using Apache Commons CSV semantics.
 */
public final class SimpleEnvArgsParserTest
{
    @Test
    public void nullOrBlank_returnsEmptyList()
    {
        assertTrue(SimpleEnvArgsParser.parseCsv(null).isEmpty());
        assertTrue(SimpleEnvArgsParser.parseCsv("   ").isEmpty());
    }

    @Test
    public void trimmingAndEmptyRemoval_basic()
    {
        List<String> parsed = SimpleEnvArgsParser.parseCsv("  --a  ,  ,  --b=c , ");
        assertEquals(List.of("--a", "--b=c"), parsed);
    }

    @Test
    public void quotedField_preservesCommas()
    {
        List<String> parsed = SimpleEnvArgsParser.parseCsv("--a,\"--b=c,d\",e");
        assertEquals(List.of("--a", "--b=c,d", "e"), parsed);
    }

    @Test
    public void escapedQuote_insideQuotedField_isUnescaped()
    {
        // CSV escaping for quotes is doubled quotes within a quoted field
        List<String> parsed = SimpleEnvArgsParser.parseCsv("\"a\"\"b\",c");
        assertEquals(List.of("a\"b", "c"), parsed);
    }

    @Test
    public void emptyTokens_areDropped()
    {
        List<String> parsed = SimpleEnvArgsParser.parseCsv(" , x , , y , ");
        assertEquals(List.of("x", "y"), parsed);
    }
}


