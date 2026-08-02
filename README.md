![webjourney maven](https://img.shields.io/maven-central/v/io.github.jamoamo/webjourney)Includes a test utility (WIP) to test web interactions without a real browser/web page.![webjourney-test maven](https://img.shields.io/maven-central/v/io.github.jamoamo/webjourney-test)
# Web Journey

## Overview
Java library for automating web interactions via the concept of a sequence of actions on the web, defined as a web journey. Define the Path and interactions and let it run.

Built on top of Selenium for interacting with web pages.
## [WIP] Test Utility

Includes a test utility (WIP) to test web interactions without a real browser/web page.
## Retries

WebJourney includes robust, customizable retry capabilities via `failsafe` to handle transient network errors during navigation or action execution.

**Global Retry Policy via Builder**
```java
IJourney journey = JourneyBuilder.builder()
    .options()
        .retryPolicy()
            .maxRetries(3)
            .delay(Duration.ofSeconds(1)) // Failsafe retry delay
            .build()
        .apply()
    .build();
```

**Operation-Specific Retry Annotations**
For finer control when extracting nested entities, apply the `@Retry` annotation to `@ExtractFromUrl` fields. This overrides the default policy just for that navigation action:
```java
public class MyEntity {
    @ExtractFromUrl
    @Retry(maxRetries = 5, delayMs = 2000)
    private NestedEntity details;
}
```

## Text Node Extraction (since 0.8.0)

`@ExtractValue` resolves its XPath to an element. Some content, however, is a bare text node sitting
between sibling elements (for example a trailing line of text directly after a `</table>`), which cannot
be represented as a web element. `@ExtractTextValue` fills this gap: its `path` is expected to resolve
to one or more DOM text nodes (an XPath that resolves to elements instead simply matches nothing - it
does not fall back to returning element text).

```java
public class Over {
    @ExtractValue(path = "tbody/tr")
    private List<Row> rows;

    @ExtractTextValue(path = "following-sibling::text()[1]", optional = true)
    private String endOfOverLine;
}
```

Given `</table>\n    End of over 1: (4 runs scored) Sri Lanka 4-0\n<table>...`, `endOfOverLine` above is
`"End of over 1: (4 runs scored) Sri Lanka 4-0"` - the surrounding whitespace/newlines that real HTML
formatting puts around a text node are stripped, because extracted values are trimmed by default
(`trim = true`). Set `trim = false` to get the raw, untrimmed text node value instead.

Bind to a `String` to capture the first matching text node, or to a `List<String>` to capture every
matching text node in document order. As with `@ExtractValue`, `optional = true` means extraction
resolves to `null` (or an empty list) rather than throwing when nothing matches.

To extract a sub-string of a text node (for example just `"4"` out of
`"End of over 1: (4 runs scored) Sri Lanka 4-0"`), compose with `@RegexExtractValue` via its
`extractTextValue` attribute, as an alternative to `extractValue`:

```java
@RegexExtractValue(
    extractTextValue = @ExtractTextValue(path = "following-sibling::text()[1]"),
    regexes = {".*\\((?<runs>\\d+) runs scored\\).*"},
    groupName = "runs")
private String runsScoredThisOver;
```

Exactly one of `extractValue`/`extractTextValue` must be set on a `@RegexExtractValue`. This composition
is not (yet) available on `@ConditionalExtractValue`/`@ConditionalExtractFromUrl` - those still only
accept `@ExtractValue` as their `if`/`then` source. Generalizing them the same way is a larger, separate
change (every `if`/`then` attribute would need a text-node counterpart); raise it if you need it.

Note: text node extraction requires a parent element context - it is only supported on fields of an
entity that is itself extracted from a repeated or nested element (as `Over` is above), not on the root
entity read directly from the page (e.g. the argument to `consumePage`, or an `@ExtractFromUrl` target).
Using `@ExtractTextValue` on such a page-level entity fails fast with a clear error as soon as that
entity is defined, rather than only when the field is actually scraped.

For offline testing, `webjourney-test`'s `MockElement` can represent text nodes via
`MockElement.textNode("some text")`, added as a child alongside ordinary element children. Its XPath
support is intentionally minimal - `text()` and `following-sibling::text()[n]` - and it throws
`UnsupportedOperationException` for anything else, rather than silently returning no match.
