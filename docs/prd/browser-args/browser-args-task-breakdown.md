# Implementation Task Breakdown: Additional Browser Arguments Support

> Source PRD: [browser-args.md](./browser-args.md)

## Purpose
This document breaks the feature into sized, independently verifiable tasks. Each task is intended to take no longer than 4–5 days and includes scope, deliverables, acceptance criteria, effort, and dependencies.

## Milestone 0 — Discovery and Design

### M0.1: Discovery of current browser bootstrapping and config surfaces
- **Scope**: Identify where `ChromeOptions`/`FirefoxOptions`/`EdgeOptions` are created, how configuration is loaded, how `BrowserPool` manages concurrency, and where per-journey state lives.
- **Deliverables**: Short architecture note with integration points, gaps, and risks. The archirtecture note should be created in a file called M0-1_architecture_note.md in docs/prd/browser-args directory.
- **Acceptance**: Named classes/files and lifecycle points confirmed; risks noted.
- **Effort**: 0.5–1 day
- **Dependencies**: None

### M0.2: Public API and data model design
- **Scope**: Define `BrowserArgumentsProvider` interface; `DefaultBrowserArgumentsProvider` responsibilities; updates to `AsyncConfiguration` and `AsyncJourneyContext`; feature flag key; environment variable names; deny-list config; log redaction rules and provenance model.
- **Deliverables**: Design note + interface and DTO sketches (no implementation). The design should be created in a file named M0-2_design_note.md.
- **Acceptance**: Reviewed and approved by maintainers.
- **Effort**: 1–2 days
- **Dependencies**: M0.1

## Milestone 1 — Configuration Surfaces

### M1.1: YAML/properties config ingestion
- **Scope**: Extend `AsyncConfiguration` to load:
  - Global: `browser.args: []`
  - Per-browser: `browser.chrome.args`, `browser.firefox.args`, `browser.edge.args`
- **Deliverables**: Config bindings with safe defaults.
- **Acceptance**: Unit tests prove values load; default to empty lists when unspecified.
- **Effort**: 1–2 days
- **Dependencies**: M0.2

### M1.2: Environment variable ingestion
- **Scope**: Read as comma-separated strings: `WEBJOURNEY_BROWSER_ARGS`, `WEBJOURNEY_CHROME_ARGS`, `WEBJOURNEY_FIREFOX_ARGS`, `WEBJOURNEY_EDGE_ARGS`; normalize whitespace/quotes minimally.
- **Deliverables**: Env source module with parsing helpers.
- **Acceptance**: Unit tests cover parsing common cases and empty values.
- **Effort**: 1–1.5 days
- **Dependencies**: M0.2

### M1.3: Programmatic per-journey override API
- **Scope**: Add `journeyContext.getBrowserArguments().addForBrowser(BrowserType, List<String>)` and global `add(List<String>)`; thread-safe holder.
- **Deliverables**: API surface with javadoc.
- **Acceptance**: Unit tests for add/override semantics; isolation across journeys.
- **Effort**: 1–2 days
- **Dependencies**: M0.2

## Milestone 2 — Argument Parsing and Normalization

### M2.1: Robust argument parser utility
- **Scope**: Split strings into argv-like tokens respecting quotes, escaped commas, Windows/Unix nuances; trim; validate characters.
- **Deliverables**: `BrowserArgParser` utility.
- **Acceptance**: Unit tests for edge cases (quoted values, spaces, empty tokens, duplicates, mixed quotes, escaped separators).
- **Effort**: 2–3 days
- **Dependencies**: M1.1, M1.2

### M2.2: Argument de-duplication and precedence merge
- **Scope**: Canonicalize arguments to detect conflicts (key-only vs key=value); implement merge that preserves highest-precedence value with stable order.
- **Deliverables**: Merge helper with deterministic output.
- **Acceptance**: Unit tests for conflicts, ordering, mixed key forms.
- **Effort**: 1–2 days
- **Dependencies**: M2.1

## Milestone 3 — Provider and Precedence Logic

### M3.1: Define `BrowserArgumentsProvider` interface
- **Scope**: Interface with `resolve(browserType, journeyContext)` returning resolved list plus optional provenance metadata.
- **Deliverables**: Interface and metadata type.
- **Acceptance**: Compiles; javadoc complete.
- **Effort**: 0.5 day
- **Dependencies**: M0.2

### M3.2: Implement `DefaultBrowserArgumentsProvider`
- **Scope**: Read from configuration (global + per-browser), env vars, per-journey overrides; apply precedence: (1) Per-journey, (2) Env vars, (3) Per-browser config, (4) Global config; apply de-duplication.
- **Deliverables**: Implementation and unit tests.
- **Acceptance**: Tests prove precedence and determinism.
- **Effort**: 2–3 days
- **Dependencies**: M1.1–M1.3, M2.2, M3.1

## Milestone 4 — Validation, Sanitization, Logging

### M4.1: Deny-list validation
- **Scope**: Default deny-list (e.g., `--user-data-dir`, dangerous debugging flags), configurable via config; failure mode selection (reject vs warn) with messaging.
- **Deliverables**: Validator module + config keys.
- **Acceptance**: Unit tests; defaults documented.
- **Effort**: 1–2 days
- **Dependencies**: M3.2

### M4.2: Sensitive value redaction
- **Scope**: Redact credentials and secrets (e.g., `user:pass@` in proxy URLs, tokens in key=value args) while preserving readability.
- **Deliverables**: Redactor utility.
- **Acceptance**: Unit tests; logs show redacted values.
- **Effort**: 1–1.5 days
- **Dependencies**: M3.2

### M4.3: Observability/logging with provenance
- **Scope**: Log final resolved arguments and per-argument source; configurable log level; ensure redaction is applied.
- **Deliverables**: Logging integrated into provider or integration point.
- **Acceptance**: Unit tests (snapshot logs) and manual verification.
- **Effort**: 1–2 days
- **Dependencies**: M3.2, M4.2

## Milestone 5 — Selenium Integration

### M5.1: Apply arguments to `*Options`
- **Scope**: Update driver factory/`BrowserPool` integration to call the provider and pass `addArguments(resolvedArgs)` for Chrome, Edge, Firefox.
- **Deliverables**: Integration code and tests.
- **Acceptance**: Unit/integration tests verify `*Options` receive args.
- **Effort**: 1–2 days
- **Dependencies**: M3.2

### M5.2: Remote Grid compatibility
- **Scope**: Ensure arguments propagate using `RemoteWebDriver`/Grid; set capabilities appropriately; verify serialization.
- **Deliverables**: Remote path handling tests.
- **Acceptance**: Integration test against local Grid or mock.
- **Effort**: 1–2 days
- **Dependencies**: M5.1

### M5.3: Feature flag `browser.enableExtraArgs`
- **Scope**: Wire feature flag to bypass provider entirely when false; uphold backward compatibility.
- **Deliverables**: Flag plumbing; default decided and documented.
- **Acceptance**: Unit tests; no behavior change when disabled.
- **Effort**: 0.5–1 day
- **Dependencies**: M5.1

## Milestone 6 — Concurrency, Performance, Safety

### M6.1: Thread-safety hardening
- **Scope**: Ensure provider is stateless or synchronized; per-journey structures isolated; immutable results at boundaries.
- **Deliverables**: Concurrency review and fixes.
- **Acceptance**: Concurrency tests simulating parallel journeys; no shared-state races.
- **Effort**: 1–2 days
- **Dependencies**: M3.2, M5.1

### M6.2: Performance validation
- **Scope**: Micro-benchmarks to validate <1ms overhead when no custom args; optimize hot paths.
- **Deliverables**: Benchmark harness + results.
- **Acceptance**: Results documented, threshold met.
- **Effort**: 0.5–1 day
- **Dependencies**: M3.2

## Milestone 7 — Testing

### M7.1: Unit tests for parsing/merging/denylists/redaction
- **Scope**: Comprehensive unit test matrix for M2–M4 modules.
- **Deliverables**: Test suite with high coverage.
- **Acceptance**: >90% coverage for these modules; green CI.
- **Effort**: 2–3 days
- **Dependencies**: M2–M4

### M7.2: Integration tests for local drivers
Descoped
- **Scope**: Launch Chrome, Firefox, Edge locally with custom flags and verify reflection in `Capabilities` and runtime behavior where feasible (e.g., headless).
- **Deliverables**: Integration tests + fixtures.
- **Acceptance**: All browser paths validated on CI-supported OS.
- **Effort**: 2–4 days
- **Dependencies**: M5.1

### M7.3: Integration tests for Remote Grid
- **Scope**: Verify flags over `RemoteWebDriver`; ensure no serialization loss.
- **Deliverables**: Grid test or mock.
- **Acceptance**: Stable integration test results.
- **Effort**: 1–2 days
- **Dependencies**: M5.2

### M7.4: E2E matrix tests across config sources
- **Scope**: Matrix of YAML-only, env-only, journey-only, combined precedence; Windows and Linux runners.
- **Deliverables**: E2E scenarios + scripts.
- **Acceptance**: Matrix green in CI.
- **Effort**: 2–3 days
- **Dependencies**: M1–M5

## Milestone 8 — Documentation and Samples

### M8.1: User-facing docs
- **Scope**: Update docs with YAML examples, env var usage (PowerShell, POSIX), per-journey API examples, deny-list and redaction notes, feature flag.
- **Deliverables**: `docs/` sections and examples.
- **Acceptance**: Reviewed docs; examples runnable.
- **Effort**: 1–2 days
- **Dependencies**: M1–M5

### M8.2: Example projects/configs
- **Scope**: Add sample YAML and a demo journey showing per-journey overrides.
- **Deliverables**: `examples/` assets.
- **Acceptance**: Manual run succeeds locally.
- **Effort**: 1–2 days
- **Dependencies**: M5.1

## Milestone 9 — CI/CD and Rollout

### M9.1: CI updates
- **Scope**: Add env var injection for tests; add Windows + Linux jobs; cache browsers/drivers as needed.
- **Deliverables**: CI config changes.
- **Acceptance**: CI green with new jobs; reasonable runtime.
- **Effort**: 1–2 days
- **Dependencies**: M7.x

### M9.2: Backward compatibility verification
- **Scope**: Run representative journeys with feature disabled/enabled and no custom args; confirm no behavior change when unused.
- **Deliverables**: Brief report.
- **Acceptance**: No regressions found.
- **Effort**: 0.5–1 day
- **Dependencies**: M5.3

### M9.3: Release notes and upgrade guidance
- **Scope**: Prepare CHANGELOG, upgrade guidance (opt-in nature), security caveats about flags.
- **Deliverables**: Release notes.
- **Acceptance**: Stakeholder approval.
- **Effort**: 0.5 day
- **Dependencies**: M8.1

## Risk-Driven Spikes (Optional)

### R1: Windows quoting edge cases
- **Scope**: Validate parser under PowerShell and CMD semantics; add tests.
- **Effort**: 0.5–1 day
- **Dependencies**: M2.1

### R2: Selenium Grid vendor quirks
- **Scope**: Validate flags with common Grid setups (Selenium Grid, Selenoid, Moon); document differences.
- **Effort**: 1–2 days
- **Dependencies**: M5.2

## Cross-Cutting Acceptance Criteria
- Precedence order exactly matches PRD.
- Logging shows resolved args and sources with redaction.
- Deny-list is enforced and configurable.
- Thread-safe under concurrent journeys.
- <1ms overhead without custom args.
- Works for local and remote drivers.
- 100% backward compatible when unused or feature-flagged off.

## Suggested Sequencing and Parallelization
- Primary order: M0 → M1 → M2 → M3 → M4 → M5 → M6 → M7 → M8 → M9.
- Parallelizable work:
  - M1.1–M1.3 in parallel once M0.2 is approved.
  - M4.1–M4.3 once M3.2 shape is known.
  - M7.2 and M7.3 can overlap after M5.
  - M8.1 can progress alongside M7 test work.

## Out of Scope
This document is planning only. No implementation is included.
