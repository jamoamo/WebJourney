# M0.2 Design Note — Browser Arguments Provider and Data Model

Source PRD: `docs/prd/browser-args/browser-args.md`

## Summary

Introduce a unified, layered, and safe mechanism to supply additional command-line arguments to browsers (Chrome, Firefox, Edge). The design defines the public API (`BrowserArgumentsProvider`), the default provider (`DefaultBrowserArgumentsProvider`), the per-journey override surface, configuration keys (YAML/properties + feature flag), environment variable names, deny-list validation, redaction rules, precedence/merge semantics, and logging with provenance. Integration begins in `ChromeBrowserFactory` and generalizes to forthcoming Firefox/Edge factories.

## Goals (from PRD, mapped to design)

- Unified configuration API with precedence: per-journey > env > per-browser config > global config
- Correct application to Selenium `*Options.addArguments(List<String>)`
- Observability with provenance and redaction
- Validation with configurable deny-list
- Backward compatible and feature-flag controlled

## Public API Surface

### Provider Interface

```java
package io.github.jamoamo.webjourney.api.web;

import io.github.jamoamo.webjourney.JourneyContext;
import java.util.List;

/** Resolves additional browser arguments for a given browser and journey. */
public interface BrowserArgumentsProvider {
    ResolvedBrowserArguments resolve(StandardBrowser browserType, JourneyContext journeyContext);
}
```

### DTOs and Supporting Types

```java
package io.github.jamoamo.webjourney.api.web;

import java.util.List;

/** Immutable result containing the final argument list and provenance for observability. */
public final class ResolvedBrowserArguments {
    private final List<String> arguments;                 // normalized, validated, merge-resolved
    private final List<ProvenancedArgument> provenance;   // one entry per resulting argument

    public ResolvedBrowserArguments(List<String> arguments, List<ProvenancedArgument> provenance) {
        this.arguments = List.copyOf(arguments);
        this.provenance = List.copyOf(provenance);
    }

    public List<String> arguments() { return arguments; }
    public List<ProvenancedArgument> provenance() { return provenance; }
}

/** A single resulting argument with its key/value decomposition and source. */
public record ProvenancedArgument(
    String key,            // canonical key, e.g., "--headless"
    String value,          // may be null for key-only flags; normalized string when present
    BrowserArgumentSource source // highest-precedence source that supplied the final value
) {}

/** Where an argument (final form) came from. */
public enum BrowserArgumentSource {
    PER_JOURNEY,
    ENVIRONMENT,
    PER_BROWSER_CONFIG,
    GLOBAL_CONFIG
}
```

### Per-journey Override API

Extend `JourneyContext` with a typed, thread-safe holder for overrides. The holder is intentionally simple and mutable only by the owning journey.

```java
package io.github.jamoamo.webjourney.api.web;

import java.util.List;

/** Thread-safe per-journey browser-argument overrides. */
public interface JourneyBrowserArguments {
    /** Add global overrides that apply to any browser type for this journey. */
    void add(List<String> args);

    /** Add overrides specific to a browser type for this journey. */
    void addForBrowser(StandardBrowser browserType, List<String> args);
}
```

Add accessor to `JourneyContext`:

```java
// in JourneyContext (and IJourneyContext if present)
JourneyBrowserArguments getBrowserArguments();
```

Notes:
- This API is additive; users not calling it see no behavior change.
- Internally backed by a concurrent structure to preserve isolation across parallel journeys if/when added.

## Configuration Model

### YAML/Properties Keys

```yaml
browser:
  enableExtraArgs: true            # feature flag (default true)
  args: []                         # global default arguments
  argsDenyList:                    # defaulted, can be extended/overridden
    - --user-data-dir
  chrome:
    args: []
  firefox:
    args: []
  edge:
    args: []
  logging:
    argsLogLevel: DEBUG            # level for resolved-args logging
    redactEnabled: true            # apply redaction in logs
```

Binding target (to be introduced): `AsyncConfiguration` or equivalent configuration holder within `webjourney`.

### Environment Variables

- `WEBJOURNEY_BROWSER_ARGS`  (global)
- `WEBJOURNEY_CHROME_ARGS`
- `WEBJOURNEY_FIREFOX_ARGS`
- `WEBJOURNEY_EDGE_ARGS`

Format: comma-separated string with simple quoting/escaping rules (see Parser).

## Default Provider Responsibilities

`DefaultBrowserArgumentsProvider` must:

- Read sources in precedence order:
  1) Per-journey overrides (`JourneyBrowserArguments`)
  2) Environment variables
  3) Per-browser config
  4) Global config
- Parse strings into argv-like tokens respecting quotes and escaped commas
- Normalize arguments to canonical key/value form
- Merge with de-duplication by canonical key, keeping the value from the highest-precedence source
- Validate against deny-list (configurable) and chosen failure mode
- Produce immutable results and provenance
- Log resolved arguments with provenance at configured level, applying redaction

### Parser and Normalization (utility: `BrowserArgParser`)

- Input: raw strings from env/config or `List<String>` from per-journey
- Output: `List<String>` of normalized tokens
- Rules:
  - Split on commas, supporting escaped commas with `\,`
  - Support quoting: tokens wrapped in single or double quotes are preserved sans quotes
  - Trim surrounding whitespace
  - Canonicalize `--key value` and `--key=value` to `--key=value` where possible
  - Treat single-dash flags as-is (e.g., `-headless` for Firefox) without conversion

### Merge & Precedence Semantics

- Canonical key extraction: substring up to the first `=` if present; otherwise the full token (key-only flag)
- Precedence: last-writer-wins by ordered sources (global → per-browser → env → per-journey)
- Deterministic order: start from lowest layer and overlay higher layers; when an argument’s key is overridden, its position is retained based on the highest-precedence contributor
- Output list preserves stable order of resulting unique keys

### Validation (Deny-list)

- Configured via `browser.argsDenyList` (list of canonical keys)
- Default includes: `--user-data-dir`
- Failure mode: reject (throw) vs warn-and-drop; option key proposal: `browser.argsDenyListReject` (default: true)
- Failure message must include the offending key and its source; do not log sensitive values

### Redaction Rules

- Apply only to logs (never mutate the values passed to Selenium)
- Patterns to redact (case-insensitive where applicable):
  - Credentials in URLs: `([a-zA-Z][a-zA-Z0-9+.-]*://)([^:@/]+):([^@/]+)@` → redact user and password
  - Keyed secrets: if key matches `(password|passwd|token|secret|apikey|api_key)` within `--key=value`, replace value with `***`
- Redaction is enabled by `browser.logging.redactEnabled` (default true)

### Logging with Provenance

- When `browser.enableExtraArgs` is true, log once per browser launch:
  - Resolved, redacted arg list
  - For each argument: key and `BrowserArgumentSource`
- Level default: `browser.logging.argsLogLevel=DEBUG`

## Integration Points

### Immediate: Chrome

In `ChromeBrowserFactory.createChromeOptions(IBrowserOptions)`:

1) If `browser.enableExtraArgs == true`, call the provider:

```java
ResolvedBrowserArguments resolved = browserArgumentsProvider.resolve(StandardBrowser.CHROME, journeyContext);
options = options.addArguments(resolved.arguments());
```

2) Ensure this call happens after existing hardcoded defaults are applied so user-supplied args can override where the browser honors later arguments.

### Forthcoming: Firefox and Edge

- Add analogous factories (e.g., `FirefoxBrowserFactory`, `EdgeBrowserFactory`)
- Apply `resolved.arguments()` via `FirefoxOptions.addArguments` / `EdgeOptions.addArguments`

### Remote/Grid (future-compatible)

- Ensure arguments flow into `Capabilities` for `RemoteWebDriver`; verify serialization in M5.x

## Thread Safety and Immutability

- `DefaultBrowserArgumentsProvider` is stateless; all configuration is injected read-only
- Results (`ResolvedBrowserArguments`) are immutable
- `JourneyBrowserArguments` implementation uses thread-safe structures; each `JourneyContext` owns its instance

## Backward Compatibility

- Default behavior unchanged when:
  - `browser.enableExtraArgs=false`, or
  - No config/env/per-journey args are present
- Existing hardcoded Chrome flags remain unless explicitly overridden by browser behavior

## Open Questions / Assumptions

- Uses `StandardBrowser` (existing enum) as `browserType` to align with current codebase
- Headless toggles currently controlled by `IBrowserOptions`; this design does not change those semantics and only adds extra arguments

## Acceptance (per M0.2)

- Provider interface and DTO sketches defined
- Config keys, env var names, precedence, deny-list, redaction, and provenance model specified
- Integration points identified

