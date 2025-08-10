package io.github.jamoamo.webjourney.api.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class BrowserArgumentsMergeTest
{
    @Test
    void merge_lastWriterWins_preservesStableOrder()
    {
        List<String> global = List.of("--a=1", "--b=1", "--c");
        List<String> perBrowser = List.of("--b=2", "--d=4");
        List<String> env = List.of("--a=3", "--e=5");
        List<String> perJourney = List.of("--c=9", "--f");

        List<String> merged = BrowserArgumentsMerge.mergeByPrecedence(global, perBrowser, env, perJourney);

        // Expected order: stable order of unique keys as last written, matching implementation
        assertEquals(List.of("--b=2", "--d=4", "--a=3", "--e=5", "--c=9", "--f"), merged);
    }

    @Test
    void canonicalKey_splitsOnEqualsOnlyFirst()
    {
        assertEquals("--x", BrowserArgumentsMerge.canonicalKey("--x=1=2"));
        assertEquals("--y", BrowserArgumentsMerge.canonicalKey("--y"));
    }
}



