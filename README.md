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

**Refused connections are not retried (since the next release)**
Retrying a refused connection will not fix it, and repeating it against a host that is actively refusing you (a rate limit or an IP block, say) only makes matters worse. So a retry policy built with `RetryPolicyBuilder` never retries a failure that is, or is caused by, a refused connection (see `ConnectionFailures.isConnectionRefused`): it fails on the first attempt, with no retry delay. A refusal is a `ConnectException` whose message contains "refused", or a Selenium `WebDriverException` (other than `UnhandledAlertException` and `JavascriptException`, whose text the page controls) whose message contains Chromium's `ERR_CONNECTION_REFUSED`; a connect timeout, unreachable host or reset connection is still retried. Add your own rules for other failures with `abortOn`, which can be called more than once, and use `clearAbortRules()` to opt out of the default:
```java
IRetryPolicy policy = RetryPolicyBuilder.builder()
    .maxRetries(3)
    .abortOn(failure -> failure instanceof MyFatalException) // in addition to refused connections
    .build();

IRetryPolicy retryEverything = RetryPolicyBuilder.builder()
    .clearAbortRules() // a refused connection is retried like any other failure
    .build();
```
`IJourneyObserver` is told about retries through `actionRetried` and `actionRetryAborted`; both are optional to implement.

## Best-Effort Journeys (since the next release)

`bestEffortJourney` always attempts a sub journey (logging in, say) but does not stop the journey if it fails: the failure is logged and swallowed. Give the step a name, and pass a listener, to find out how it ended and to decide what happens next:

```java
IJourney journey = JourneyBuilder.path()
    .bestEffortJourney("login", login -> login.navigateTo("https://example.com/login")/* ... */.build(),
        outcome -> {
            if (outcome.isSuccess()) {
                return BestEffortDecision.CONTINUE;
            }
            // the original exception; the refusal may be anywhere in its cause chain
            if (ConnectionFailures.isConnectionRefused(outcome.getFailure())) {
                backOff(); // e.g. count it, and wait before the next run
                return BestEffortDecision.ABORT;
            }
            return BestEffortDecision.CONTINUE;
        })
    .navigateTo("https://example.com/matches")
    .build();
```

The listener is given a `BestEffortOutcome`:
- `getName()`: the step's name (`"BestEffort"` when you don't give one).
- `isSuccess()` and `getFailure()`: `getFailure()` is non-null exactly when the sub journey failed. It is the original, unwrapped exception, so inspect its whole cause chain. It can reference the failed action and its inputs, so don't keep the outcome after the callback returns.
- `getElapsed()`: how long the sub journey took.
- `getRetryCount()`: how many times actions within the sub journey were retried.

The listener's `BestEffortDecision` says what to do next:
- `CONTINUE` carries on with the rest of the journey.
- `ABORT` after a failure stops the journey by rethrowing the original failure, cause chain intact. The step is not retried, whatever the retry policy, so the sub journey (and a login) is not run again. `ABORT` after a success is ignored.
- A listener that throws an `Exception` (checked or not) is logged at `ERROR` and treated as `CONTINUE`; an `Error` propagates. Returning `null` is also treated as `CONTINUE`.
- The failure can reference the failed action and its inputs, and its message can include text from the page, so treat it as untrusted and sensitive rather than passing it on unfiltered.

A refused connection fails on the first attempt (see Retries), so a caller that runs whole journeys in a loop should use the listener, and `ABORT`, to back off rather than rely on retries to slow it down. The overloads without a listener never abort the journey.

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
