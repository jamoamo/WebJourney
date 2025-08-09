# M0.1 Architecture Note — Current Browser Bootstrapping and Config Surfaces

Source PRD: `docs/prd/browser-args/browser-args.md`

## Summary

Today the framework launches Chrome via Selenium with a minimal, hardcoded set of flags and default behaviors. There is no generalized configuration model, no environment ingestion, no per-journey browser-argument API, and no `BrowserPool`. The primary integration point for the proposed Browser Arguments feature is the Chrome options construction path, with additional factories required for Firefox and Edge.

## Where browser options are created

- Chrome is constructed via `ChromeBrowserFactory.createBrowser(IBrowserOptions)` which builds `ChromeOptions` and adds a few hardcoded arguments, then creates a `ChromeDriver` and wraps it in `SeleniumDrivenBrowser`.
  - Options construction and existing flags:
    - Adds: `--no-sandbox`, `--remote-allow-origins=*`, `--disable-dev-shm-usage`
    - Applies headless flag when `IBrowserOptions.isHeadless()` is true (`--headless=new`)
    - Sets unexpected alert behavior based on `IBrowserOptions.acceptUnexpectedAlerts()`

Key files:

- `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/ChromeBrowserFactory.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/IBrowserOptions.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/DefaultBrowserOptions.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/SeleniumDrivenBrowser.java`

Observed gaps:

- No `FirefoxOptions` or `EdgeOptions` creation exists yet. `StandardBrowser` lists non-Chrome entries but their factories are `null`.
- No `RemoteWebDriver` construction path is present; local `ChromeDriver` only.

## How configuration is loaded today

- There is no `AsyncConfiguration` (or similar) and no YAML/properties binding for browser arguments.
- No environment variable ingestion for browser args.
- `DefaultBrowserOptions` is a simple code-defaults holder (headless=true, acceptUnexpectedAlerts=true). There is no dynamic config.

Implication: All configuration surfaces described in the PRD (global/per-browser/env/per-journey) do not yet exist and must be introduced.

## Journey lifecycle and per-journey state

- A journey is executed via `WebTraveller.travelJourney(...)` which:
  1) Creates a browser using an `IPreferredBrowserStrategy` (defaults ultimately to Chrome via `TravelOptions`).
  2) Constructs a `JourneyContext` and stores the created browser and observers.
  3) Invokes `journey.doJourney(context)`.
  4) Always calls `browser.exit()` in a finally block.

- Per-journey state lives in `JourneyContext` which currently holds:
  - The `IBrowser` instance
  - A simple `Map<String, Object>` of inputs
  - Observers and a breadcrumb

Observed gaps:

- No typed per-journey browser-argument API yet (e.g., `getBrowserArguments()` in PRD). The `inputs` map could be used temporarily, but a typed API is preferred per PRD.

Key files:

- `webjourney/src/main/java/io/github/jamoamo/webjourney/WebTraveller.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/JourneyContext.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/IJourneyContext.java`

## Concurrency model (`BrowserPool`)

- No `BrowserPool` implementation is present. `WebTraveller` runs journeys synchronously and manages a single browser lifetime per call.
- PRD references to `BrowserPool` and thread-safety should be interpreted as forward-looking requirements. The new argument provider must be stateless or otherwise safe for parallel usage even though the current runner is single-threaded in practice.

## Integration points for the Browser Arguments feature

Primary:

- `ChromeBrowserFactory.createChromeOptions(...)` — inject resolved arguments by calling `options.addArguments(resolvedArgs)` after the current defaults are applied. This is the first concrete integration.

Additional work required by PRD scope:

- Introduce parallel factories for Firefox and Edge (e.g., `FirefoxBrowserFactory`, `EdgeBrowserFactory`) that mirror the Chrome path and consume the same provider.
- Introduce a `BrowserArgumentsProvider` and a default implementation that resolves args from configuration, environment variables, and per-journey overrides following precedence.
- Extend journey context (`IJourneyContext`) with a typed per-journey browser-arguments holder as per PRD.
- Add configuration surfaces (YAML/properties and env) and a feature flag to bypass provider when disabled.
- If/when remote execution is added, ensure `*Options` and capabilities propagate over `RemoteWebDriver`.

## Risks and gaps

- Missing Firefox/Edge paths: Must be added to meet PRD coverage. Also update `StandardBrowser` so non-Chrome factories are wired.
- No configuration system: Need to introduce config ingestion, precedence, deny-list, and redaction per PRD.
- Thread-safety: Current flow is effectively single-threaded, but the provider should be written as stateless/immutable to be safe under future parallelism or if consumers invoke journeys concurrently.
- Logging and provenance: Current logging does not include argument provenance; will need structured logs with redaction.
- Backward compatibility: Ensure default behavior remains unchanged when feature is disabled or when no args are provided.

## Named classes/files and lifecycle checkpoints

- Browser creation entrypoints
  - `WebTraveller.travelJourney(...)` — journey start; obtains browser via strategy; finally closes it
  - `IPreferredBrowserStrategy` and implementations
    - `PreferredBrowserStrategy` (single factory)
    - `PriorityBrowserStrategy` (iterates candidates)
  - `IBrowserFactory` → `ChromeBrowserFactory` — constructs `ChromeOptions` and `ChromeDriver`

- Options creation and application
  - `ChromeBrowserFactory.createChromeOptions(IBrowserOptions)` — current hardcoded flags and behaviors; injection point for provider output

- Per-journey state
  - `JourneyContext` — holds `IBrowser`, inputs, observers, breadcrumb. New typed browser-args accessor to be added per PRD.

- Driver lifecycle
  - `SeleniumDrivenBrowser` — wraps `RemoteWebDriver`, manages default timeouts, logging, and `exit()` semantics

- Not present but referenced in PRD
  - `BrowserPool` — no implementation today; concurrency to be considered in provider design
  - `AsyncConfiguration` — no implementation today; to be introduced for config ingestion per PRD


