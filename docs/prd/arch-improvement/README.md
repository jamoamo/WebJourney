# Architecture & Usability Improvements – Task List

Status: Draft
Owner: Core Team

## Milestone 1 – Module boundaries and internal API
- [ ] Extract modules: core, browser-api, selenium-adapter, entity, args-config, runner, testkit (skeleton poms, package moves) (4d)
- [ ] Introduce `internal.*` namespace and add API Guardian annotations for internal classes (2d)
- [ ] Publish BOM (bill of materials) to align dependency versions across modules (2d)

## Milestone 2 – Execution model and SPI hooks
- [ ] Replace int waits with `Duration` across public APIs; keep backwards-compatible overloads (3d)
- [ ] Add `ActionInterceptor` SPI (pre/post around actions) + wire into `SubJourney` (3d)
- [ ] Introduce `WaitStrategy` and `RetryPolicy` abstractions (2d)
- [ ] Integrate Failsafe-based default retry/wait policies (2d)

## Milestone 3 – Browser arguments and configuration
- [ ] Extract args provider and validator into `args-config` module (1d)
- [ ] Add `BrowserArgumentsContributor` SPI for env/config/per-journey sources (3d)
- [ ] Document precedence and provenance with examples (1d)

## Milestone 4 – Grid/remote decoupling
- [ ] Move hub strategy and health monitor to `selenium-adapter-grid` submodule (2d)
- [ ] Make fallback purely policy-driven; configurable via `TravelOptions` (2d)
- [ ] Add in-memory hub simulator for tests (1d)

## Milestone 5 – Entity pipeline public API
- [ ] Promote `reserved.entity.*` to `entity` module with stable facades (3d)
- [ ] Add annotation processor to generate mappers/transformers (Proof of Concept) (4d)
- [ ] Provide JSON/YAML mapping support and loader (3d)

## Milestone 6 – Selector ergonomics and page helpers
- [ ] Add `Selector` abstraction (CSS and XPath) and adapter support (3d)
- [ ] Extend `IWebPage` to accept selectors; update Selenium adapter (2d)
- [ ] Add cross-frame/shadow DOM helpers (2d)

## Milestone 7 – Observability and reporting
- [ ] Structured journey report model (JSON) with step timings and provenance (3d)
- [ ] Exporters: JUnit XML and Allure adapters (3d)
- [ ] Failure artifacts capture (screenshots, DOM, console logs) policy (3d)
- [ ] Micrometer metrics: step timers, retries, failures (2d)

## Milestone 8 – Smart waits and utilities
- [ ] Provide `untilVisible/Clickable/Text/Url` waits with timeouts (3d)
- [ ] Download/upload helpers and file chooser support (2d)
- [ ] Cookie jar import/export utilities (2d)

## Milestone 9 – Async runner and parallelism
- [ ] `JourneyExecutor` with CompletableFuture, cancellation, timeouts (4d)
- [ ] Parallel sub-journeys with bounded concurrency (3d)
- [ ] Session pool with TTL for parallel runs (4d)

## Milestone 10 – Testkit evolution
- [ ] JUnit 5 extension `@JourneyTest` + parameter resolvers (4d)
- [ ] Fake DOM builder (Jsoup) with CSS/XPath and deterministic clock (4d)
- [ ] Fault injection (timeouts, stale elements, intercepted clicks) (3d)
- [ ] Record/replay of Selenium sessions into mocks (4d)

## Milestone 11 – Documentation & DX
- [ ] Revise README with module map and quickstart (1d)
- [ ] Add end-to-end examples (journey Domain Specific Language, smart waits, entities) (3d)
- [ ] Migration guide from `reserved.*` to new modules (2d)
- [ ] CLI starter to run YAML journeys locally/CI (4d)

## Acceptance
- [ ] CI green across split modules; baseline coverage maintained or improved
- [ ] Backwards-compatible public APIs where feasible; deprecations documented
- [ ] Example projects run against both Selenium local and remote grid