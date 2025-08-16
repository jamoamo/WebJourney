# M6 Implementation Plan — Concurrency, Performance, Safety

> Source PRD: [browser-args.md](./browser-args.md)  
> Task Breakdown: [browser-args-task-breakdown.md](./browser-args-task-breakdown.md)  
> Design Note: [M0-2_design_note.md](./M0-2_design_note.md)

## Overview

This document provides a detailed implementation plan for Milestone 6, focusing on thread-safety hardening and performance validation of the browser arguments feature. The milestone ensures the system can safely handle concurrent journeys while maintaining performance targets.

## Prerequisites

- **Dependencies**: M3.2 (DefaultBrowserArgumentsProvider), M5.1 (Selenium Integration)
- **Current State**: Browser arguments provider is implemented and integrated with Selenium `*Options`
- **Assumptions**: Based on analysis of current codebase, journeys are currently single-threaded per instance but the framework should support concurrent execution

## M6.1: Thread-Safety Hardening

### Scope Analysis

Based on current codebase analysis, the following components need thread-safety review:

1. **JourneyContext per-journey isolation**
   - `DefaultJourneyBrowserArguments` uses non-thread-safe `ArrayList` and `EnumMap`
   - `JourneyContext.inputs` uses `HashMap` (non-thread-safe)
   - `JourneyContext.journeyObservers` uses `ArrayList` (non-thread-safe)

2. **DefaultBrowserArgumentsProvider statelessness**
   - Provider holds configuration and validator/redactor instances
   - Must ensure these dependencies are immutable or thread-safe

3. **Configuration objects immutability**
   - `AsyncConfiguration` returned lists must be immutable
   - Environment variable access must be consistent

### Implementation Tasks

#### Task M6.1.1: Harden DefaultJourneyBrowserArguments (0.5 days)

**Problem**: Current implementation uses non-thread-safe collections.

**Solution**: Replace with thread-safe alternatives while maintaining API compatibility.

```java
// Current implementation uses:
private final List<String> globalArguments = new ArrayList<>();
private final Map<StandardBrowser, List<String>> browserArguments = new EnumMap<>(StandardBrowser.class);

// Replace with:
private final List<String> globalArguments = new CopyOnWriteArrayList<>();
private final Map<StandardBrowser, List<String>> browserArguments = new ConcurrentHashMap<>();
```

**Files to modify**:
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/DefaultJourneyBrowserArguments.java`

**Implementation details**:
1. Replace `ArrayList` with `CopyOnWriteArrayList` for thread-safe writes
2. Replace `EnumMap` with `ConcurrentHashMap<StandardBrowser, List<String>>`
3. Use `CopyOnWriteArrayList` for individual browser argument lists
4. Update `computeIfAbsent` calls to handle concurrent access properly
5. Ensure snapshot methods return truly immutable views

#### Task M6.1.2: Harden JourneyContext thread-safety (0.5 days)

**Problem**: `JourneyContext` uses non-thread-safe collections for inputs and observers.

**Solution**: Use thread-safe collections and defensive copying.

```java
// Replace:
private final Map<String, Object> inputs = new HashMap<>(2);
private final List<IJourneyObserver> journeyObservers = new ArrayList<>();

// With:
private final Map<String, Object> inputs = new ConcurrentHashMap<>(2);
private final List<IJourneyObserver> journeyObservers = new CopyOnWriteArrayList<>();
```

**Files to modify**:
- `webjourney/src/main/java/io/github/jamoamo/webjourney/JourneyContext.java`

#### Task M6.1.3: Validate provider statelessness (0.5 days)

**Problem**: Ensure `DefaultBrowserArgumentsProvider` and its dependencies are truly stateless.

**Solution**: Review and validate immutability of dependencies.

**Files to analyze/modify**:
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/DefaultBrowserArgumentsProvider.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/BrowserArgumentsValidator.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/BrowserArgumentsRedactor.java`

**Validation points**:
1. Confirm validator and redactor hold only immutable configuration
2. Ensure `AsyncConfiguration` instances are immutable
3. Verify no shared mutable state in utility classes like `BrowserArgParser` and `BrowserArgumentsMerge`

### Testing Strategy for M6.1

#### Concurrency Test Suite

Create comprehensive concurrent execution tests to validate thread-safety:

**Test file**: `webjourney/src/test/java/io/github/jamoamo/webjourney/api/web/ConcurrentJourneyTest.java`

```java
@Test
public void parallelJourneys_isolatedBrowserArguments() throws InterruptedException {
    int threadCount = 10;
    int journeysPerThread = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
    
    for (int t = 0; t < threadCount; t++) {
        final int threadId = t;
        executor.submit(() -> {
            try {
                for (int j = 0; j < journeysPerThread; j++) {
                    JourneyContext context = new JourneyContext();
                    String uniqueArg = "--thread-" + threadId + "-journey-" + j;
                    context.getBrowserArguments().addGlobal(List.of(uniqueArg));
                    
                    // Verify isolation
                    List<String> snapshot = context.getBrowserArguments().snapshotGlobal();
                    assertTrue(snapshot.contains(uniqueArg));
                    assertEquals(1, snapshot.size());
                }
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();
    
    if (!exceptions.isEmpty()) {
        fail("Concurrent execution failed: " + exceptions.get(0).getMessage());
    }
}
```

**Additional test scenarios**:
1. Concurrent modifications to same journey context
2. Provider resolution under concurrent load
3. Environment variable access race conditions
4. Configuration object sharing

**Test files to create**:
- `ConcurrentJourneyTest.java` - Journey context isolation tests
- `ConcurrentProviderTest.java` - Provider thread-safety tests
- `StressTestSuite.java` - High-load concurrent execution

## M6.2: Performance Validation

### Scope

Implement micro-benchmarks to validate the <1ms overhead requirement when no custom arguments are provided.

### Implementation Tasks

#### Task M6.2.1: Benchmark Infrastructure (0.25 days)

**Setup**: Create benchmarking infrastructure using JMH (Java Microbenchmark Harness).

**Files to create**:
- `webjourney/src/test/java/io/github/jamoamo/webjourney/benchmarks/BrowserArgumentsBenchmark.java`
- `webjourney/pom.xml` - Add JMH dependency

**Maven dependency addition**:
```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.37</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.37</version>
    <scope>test</scope>
</dependency>
```

#### Task M6.2.2: Core Performance Benchmarks (0.5 days)

**Benchmark scenarios**:

1. **Baseline**: Provider resolution with no custom arguments
2. **Loaded**: Provider resolution with full argument sets
3. **Parsing**: Argument parsing performance
4. **Merging**: Argument merging performance

**Example benchmark structure**:

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class BrowserArgumentsBenchmark {

    private DefaultBrowserArgumentsProvider provider;
    private JourneyContext emptyContext;
    private JourneyContext loadedContext;

    @Setup
    public void setup() {
        provider = new DefaultBrowserArgumentsProvider();
        emptyContext = createEmptyJourneyContext();
        loadedContext = createLoadedJourneyContext();
    }

    @Benchmark
    public ResolvedBrowserArguments benchmarkEmptyArguments() {
        return provider.resolve(StandardBrowser.CHROME, emptyContext);
    }

    @Benchmark  
    public ResolvedBrowserArguments benchmarkLoadedArguments() {
        return provider.resolve(StandardBrowser.CHROME, loadedContext);
    }
}
```

**Performance targets**:
- Empty arguments resolution: <1ms (1000μs)
- Loaded arguments resolution: <5ms (5000μs)
- Argument parsing: <100μs per operation
- Argument merging: <500μs per operation

#### Task M6.2.3: Optimization Implementation (0.25 days)

**Hot path optimizations** (if benchmarks reveal issues):

1. **Lazy initialization**: Defer expensive operations until needed
2. **Caching**: Cache parsed environment variables and configuration
3. **Collection sizing**: Pre-size collections to avoid resizing
4. **String operations**: Optimize string parsing and manipulation

**Example optimization patterns**:

```java
// Lazy environment variable parsing
private volatile Map<String, List<String>> cachedEnvArgs;

private Map<String, List<String>> getEnvironmentArgs() {
    if (cachedEnvArgs == null) {
        synchronized (this) {
            if (cachedEnvArgs == null) {
                cachedEnvArgs = parseEnvironmentVariables();
            }
        }
    }
    return cachedEnvArgs;
}
```

### Performance Test Integration

**Test automation**:
1. Add benchmark execution to CI pipeline (with performance regression detection)
2. Create performance baseline documentation
3. Set up monitoring for performance regressions

**CI integration** (GitHub Actions example):
```yaml
- name: Run Performance Benchmarks
  run: mvn test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test
```

## Deliverables

### M6.1 Deliverables
1. **Updated thread-safe implementations**:
   - `DefaultJourneyBrowserArguments.java` with thread-safe collections
   - `JourneyContext.java` with concurrent-safe state management
   
2. **Concurrency test suite**:
   - `ConcurrentJourneyTest.java` - Journey isolation tests
   - `ConcurrentProviderTest.java` - Provider thread-safety tests
   - `StressTestSuite.java` - High-load scenarios

3. **Thread-safety documentation**:
   - Concurrency review report
   - Updated Javadoc with thread-safety guarantees

### M6.2 Deliverables
1. **Benchmark infrastructure**:
   - JMH integration in build system
   - `BrowserArgumentsBenchmark.java` suite
   
2. **Performance results**:
   - Benchmark execution report
   - Performance baseline documentation
   - Optimization recommendations (if needed)

3. **CI integration**:
   - Automated benchmark execution
   - Performance regression detection

## Acceptance Criteria

### M6.1 Acceptance
- [ ] All concurrency tests pass without race conditions
- [ ] Stress tests with 100+ concurrent journeys complete successfully
- [ ] No shared mutable state identified in static analysis
- [ ] Thread-safety guarantees documented in Javadoc
- [ ] Memory safety verified under concurrent load

### M6.2 Acceptance  
- [ ] Empty arguments resolution averages <1ms (1000μs)
- [ ] Benchmark results documented and baseline established
- [ ] Performance tests integrated into CI pipeline
- [ ] No performance regressions detected in subsequent builds
- [ ] Hot path optimizations implemented if targets not met

## Risk Mitigation

### Thread-Safety Risks
1. **Existing code assumptions**: Some existing code may assume single-threaded access
   - *Mitigation*: Comprehensive testing and gradual rollout
   
2. **Performance impact of thread-safety**: Thread-safe collections may impact performance
   - *Mitigation*: Benchmark before/after to quantify impact

### Performance Risks
1. **Benchmark reliability**: Microbenchmarks can be unreliable
   - *Mitigation*: Multiple runs, statistical analysis, real-world validation
   
2. **Environment dependencies**: Performance may vary across environments
   - *Mitigation*: Test on multiple platforms, document environment factors

## Success Metrics

1. **Thread Safety**: Zero race conditions in stress tests
2. **Performance**: <1ms overhead for empty arguments (99th percentile)
3. **Stability**: No memory leaks under extended concurrent load
4. **Maintainability**: Clear documentation of thread-safety guarantees

This implementation plan ensures the browser arguments feature is production-ready for concurrent usage while maintaining the performance characteristics required for a web automation framework.
