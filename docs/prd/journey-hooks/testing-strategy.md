# WebJourney Progress Tracking - Testing Strategy

## Overview
This document outlines the comprehensive testing strategy for the WebJourney Progress Tracking feature, covering unit testing, integration testing, performance testing, and quality assurance approaches.

## Testing Objectives

### Primary Goals
- Ensure all event types are emitted correctly
- Verify progress tracking accuracy > 99%
- Validate thread safety in concurrent environments
- Maintain backward compatibility with existing API
- Achieve > 90% test coverage

### Quality Metrics
- Zero breaking changes to existing API
- Event processing latency < 1ms
- Memory overhead < 10% for typical usage
- Support for 100+ concurrent journeys
- Handle 1000+ events/second

## Testing Levels

### 1. Unit Testing

#### 1.1 Event System Testing

**Test Classes:**
- `ExtractionEventTest`
- `EventTypeTest`
- `PathBuilderTest`
- `PathUtilsTest`

**Test Scenarios:**
```java
@Test
public void testJourneyStartEvent() {
    String journeyId = "test-journey-123";
    JourneyStartEvent event = new JourneyStartEvent(journeyId);
    
    assertEquals(journeyId, event.getJourneyId());
    assertEquals(EventType.JOURNEY_START, event.getEventType());
    assertNotNull(event.getTimestamp());
    assertEquals(Thread.currentThread().getId(), event.getThreadId());
}

@Test
public void testPathBuilder() {
    PathBuilder builder = new PathBuilder()
        .addSegment("Scorecard")
        .addField("team_batting_innings")
        .addIndex(0)
        .addField("player_batting_innings")
        .addIndex(3)
        .addField("runs");
    
    String expected = "Scorecard.team_batting_innings[0].player_batting_innings[3].runs";
    assertEquals(expected, builder.build());
}

@Test
public void testPathUtils() {
    String path = "Scorecard.team_batting_innings[1].player_batting_innings[2].runs";
    
    assertEquals("Scorecard.team_batting_innings[1].player_batting_innings[2]", 
                PathUtils.getParentPath(path));
    assertEquals("runs", PathUtils.getFieldName(path));
    assertEquals(2, PathUtils.getArrayIndex("player_batting_innings[2]"));
    assertTrue(PathUtils.matchesPattern(path, "Scorecard.*"));
}
```

#### 1.2 Listener System Testing

**Test Classes:**
- `ExtractionListenerTest`
- `ListenerRegistryTest`
- `EventEmitterTest`

**Test Scenarios:**
```java
@Test
public void testListenerRegistration() {
    ListenerRegistry registry = new ListenerRegistry();
    ExtractionListener listener = new TestListener();
    
    registry.addJourneyListener("journey-1", listener);
    List<ExtractionListener> listeners = registry.getListenersForEvent(
        new JourneyStartEvent("journey-1"));
    
    assertEquals(1, listeners.size());
    assertTrue(listeners.contains(listener));
}

@Test
public void testGlobalListenerRegistration() {
    ListenerRegistry registry = new ListenerRegistry();
    ExtractionListener globalListener = new GlobalTestListener();
    
    registry.addGlobalListener(globalListener);
    
    // Test that global listener receives events from any journey
    List<ExtractionListener> listeners = registry.getListenersForEvent(
        new JourneyStartEvent("any-journey"));
    
    assertTrue(listeners.contains(globalListener));
}

@Test
public void testEventEmission() {
    ListenerRegistry registry = new ListenerRegistry();
    TestListener listener = new TestListener();
    EventEmitter emitter = new EventEmitter(registry, null);
    
    registry.addJourneyListener("journey-1", listener);
    
    JourneyStartEvent event = new JourneyStartEvent("journey-1");
    emitter.emitEvent(event);
    
    assertTrue(listener.receivedEvents.contains(event));
}
```

#### 1.3 Progress Tracking Testing

**Test Classes:**
- `ExtractionStateTest`
- `JourneyProgressTest`
- `WorkUnitsTest`

**Test Scenarios:**
```java
@Test
public void testJourneyProgressTracking() {
    ExtractionState state = new ExtractionState();
    String journeyId = "test-journey";
    
    // Start journey
    state.updateJourneyProgress(journeyId, new JourneyStartEvent(journeyId));
    JourneyProgress progress = state.getJourneyProgress(journeyId);
    
    assertNotNull(progress);
    assertEquals(JourneyStatus.IN_PROGRESS, progress.getStatus());
    
    // Process entities
    state.updateJourneyProgress(journeyId, new EntityStartEvent(journeyId, Scorecard.class, "Scorecard"));
    state.updateJourneyProgress(journeyId, new EntityEndEvent(journeyId, Scorecard.class, "Scorecard"));
    
    progress = state.getJourneyProgress(journeyId);
    assertEquals(2, progress.getEventCount());
    
    // Complete journey
    state.updateJourneyProgress(journeyId, new JourneyEndEvent(journeyId));
    progress = state.getJourneyProgress(journeyId);
    assertEquals(JourneyStatus.COMPLETED, progress.getStatus());
}

@Test
public void testWorkUnitsCalculation() {
    ExtractionState state = new ExtractionState();
    String journeyId = "test-journey";
    
    // Simulate journey with known work units
    state.updateJourneyProgress(journeyId, new JourneyStartEvent(journeyId));
    state.updateJourneyProgress(journeyId, new CollectionDiscoveredEvent(journeyId, "items", 10));
    
    for (int i = 0; i < 5; i++) {
        state.updateJourneyProgress(journeyId, new CollectionItemEndEvent(journeyId, "items", i));
    }
    
    WorkUnits workUnits = WorkUnits.calculate(journeyId, state);
    assertEquals(10, workUnits.getTotal());
    assertEquals(5, workUnits.getCompleted());
    assertEquals(50.0, workUnits.getPercentage(), 0.1);
}
```

### 2. Integration Testing

#### 2.1 Journey Integration Testing

**Test Classes:**
- `JourneyProgressIntegrationTest`
- `WebTravellerProgressIntegrationTest`

**Test Scenarios:**
```java
@Test
public void testJourneyWithProgressTracking() {
    TestProgressListener listener = new TestProgressListener();
    
    Journey journey = JourneyBuilder.path()
        .withExtractionListener(listener)
        .withProgressTracking(true)
        .navigateTo("https://example.com/test")
        .consumePage(TestEntity.class, entity -> {
            // Simulate entity processing
        })
        .build();
    
    journey.execute();
    
    // Verify events were emitted
    assertTrue(listener.receivedJourneyStart);
    assertTrue(listener.receivedJourneyEnd);
    assertTrue(listener.receivedEntityEvents);
    assertTrue(listener.receivedFieldEvents);
}

@Test
public void testMultipleJourneysConcurrently() {
    int journeyCount = 10;
    CountDownLatch latch = new CountDownLatch(journeyCount);
    List<TestProgressListener> listeners = new ArrayList<>();
    
    for (int i = 0; i < journeyCount; i++) {
        TestProgressListener listener = new TestProgressListener();
        listeners.add(listener);
        
        Journey journey = JourneyBuilder.path()
            .withExtractionListener(listener)
            .withProgressTracking(true)
            .navigateTo("https://example.com/test" + i)
            .consumePage(TestEntity.class, entity -> {
                // Simulate processing
                latch.countDown();
            })
            .build();
        
        // Execute in separate thread
        new Thread(journey::execute).start();
    }
    
    // Wait for all journeys to complete
    assertTrue(latch.await(30, TimeUnit.SECONDS));
    
    // Verify all listeners received events
    for (TestProgressListener listener : listeners) {
        assertTrue(listener.receivedJourneyStart);
        assertTrue(listener.receivedJourneyEnd);
    }
}
```

#### 2.2 Real Web Page Testing

**Test Classes:**
- `RealPageProgressTest`
- `CricketArchiveProgressTest`

**Test Scenarios:**
```java
@Test
public void testRealScorecardExtraction() {
    TestProgressListener listener = new TestProgressListener();
    
    Journey journey = JourneyBuilder.path()
        .withExtractionListener(listener)
        .withProgressTracking(true)
        .navigateTo("https://cricketarchive.com/scorecard/12345")
        .consumePage(Scorecard.class, scorecard -> {
            // Verify scorecard was extracted
            assertNotNull(scorecard);
            assertNotNull(scorecard.getSeriesName());
        })
        .build();
    
    journey.execute();
    
    // Verify progress events for real page structure
    assertTrue(listener.hasEventWithPath("Scorecard.series_name"));
    assertTrue(listener.hasEventWithPath("Scorecard.team_batting_innings"));
    assertTrue(listener.hasEventWithPath("Scorecard.team_batting_innings[0]"));
}

@Test
public void testErrorHandlingWithRealPages() {
    ErrorTrackingListener errorListener = new ErrorTrackingListener();
    
    Journey journey = JourneyBuilder.path()
        .withExtractionListener(errorListener)
        .withProgressTracking(true)
        .navigateTo("https://example.com/invalid-page")
        .consumePage(Scorecard.class, scorecard -> {
            // This should fail
        })
        .build();
    
    try {
        journey.execute();
        fail("Expected exception");
    } catch (Exception e) {
        // Expected
    }
    
    // Verify error events were captured
    assertTrue(errorListener.getErrorCount() > 0);
}
```

### 3. Performance Testing

#### 3.1 Event Processing Performance

**Test Classes:**
- `EventPerformanceTest`
- `ListenerPerformanceTest`

**Test Scenarios:**
```java
@Test
public void testHighFrequencyEventProcessing() {
    int eventCount = 10000;
    PerformanceTestListener listener = new PerformanceTestListener();
    
    EventEmitter emitter = new EventEmitter(new ListenerRegistry(), null);
    
    long startTime = System.nanoTime();
    
    for (int i = 0; i < eventCount; i++) {
        emitter.emitEvent(new FieldExtractEndEvent("journey-" + i, "field", "value", true));
    }
    
    long endTime = System.nanoTime();
    long duration = endTime - startTime;
    
    double eventsPerSecond = (double) eventCount / (duration / 1_000_000_000.0);
    
    // Should handle at least 1000 events/second
    assertTrue("Events per second: " + eventsPerSecond, eventsPerSecond >= 1000);
    
    // Verify all events were processed
    assertEquals(eventCount, listener.getProcessedEventCount());
}

@Test
public void testConcurrentJourneyPerformance() {
    int journeyCount = 100;
    int eventsPerJourney = 100;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(journeyCount);
    
    List<Future<Long>> futures = new ArrayList<>();
    
    for (int i = 0; i < journeyCount; i++) {
        final int journeyIndex = i;
        Future<Long> future = executor.submit(() -> {
            long startTime = System.nanoTime();
            
            TestProgressListener listener = new TestProgressListener();
            EventEmitter emitter = new EventEmitter(new ListenerRegistry(), null);
            
            for (int j = 0; j < eventsPerJourney; j++) {
                emitter.emitEvent(new EntityStartEvent("journey-" + journeyIndex, 
                                                     TestEntity.class, "entity[" + j + "]"));
            }
            
            long endTime = System.nanoTime();
            latch.countDown();
            return endTime - startTime;
        });
        
        futures.add(future);
    }
    
    assertTrue(latch.await(60, TimeUnit.SECONDS));
    
    // Calculate average processing time
    long totalTime = futures.stream()
        .mapToLong(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return 0;
            }
        })
        .sum();
    
    double avgTimePerJourney = (double) totalTime / journeyCount;
    double avgTimePerEvent = avgTimePerJourney / eventsPerJourney;
    
    // Each event should take less than 1ms
    assertTrue("Average time per event: " + avgTimePerEvent + "ns", 
               avgTimePerEvent < 1_000_000);
}
```

#### 3.2 Memory Usage Testing

**Test Classes:**
- `MemoryUsageTest`
- `MemoryLeakTest`

**Test Scenarios:**
```java
@Test
public void testMemoryUsageWithLongRunningJourney() {
    Runtime runtime = Runtime.getRuntime();
    long initialMemory = runtime.totalMemory() - runtime.freeMemory();
    
    TestProgressListener listener = new TestProgressListener();
    EventEmitter emitter = new EventEmitter(new ListenerRegistry(), null);
    
    // Simulate long-running journey with many events
    for (int i = 0; i < 100000; i++) {
        emitter.emitEvent(new FieldExtractEndEvent("long-journey", "field[" + i + "]", 
                                                  "value", true));
    }
    
    // Force garbage collection
    System.gc();
    Thread.sleep(1000);
    
    long finalMemory = runtime.totalMemory() - runtime.freeMemory();
    long memoryIncrease = finalMemory - initialMemory;
    
    // Memory increase should be reasonable (less than 100MB)
    assertTrue("Memory increase: " + memoryIncrease + " bytes", 
               memoryIncrease < 100 * 1024 * 1024);
}

@Test
public void testMemoryLeakPrevention() {
    WeakReference<TestProgressListener> listenerRef = new WeakReference<>(
        new TestProgressListener());
    
    // Create and destroy many journeys
    for (int i = 0; i < 1000; i++) {
        EventEmitter emitter = new EventEmitter(new ListenerRegistry(), null);
        TestProgressListener listener = listenerRef.get();
        if (listener != null) {
            emitter.emitEvent(new JourneyStartEvent("journey-" + i));
        }
    }
    
    // Force garbage collection
    System.gc();
    Thread.sleep(1000);
    
    // Listener should be garbage collected
    assertNull("Listener was not garbage collected", listenerRef.get());
}
```

### 4. Thread Safety Testing

#### 4.1 Concurrency Testing

**Test Classes:**
- `ThreadSafetyTest`
- `ConcurrentAccessTest`

**Test Scenarios:**
```java
@Test
public void testConcurrentListenerRegistration() {
    ListenerRegistry registry = new ListenerRegistry();
    int threadCount = 10;
    int operationsPerThread = 1000;
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    List<Thread> threads = new ArrayList<>();
    
    for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i;
        Thread thread = new Thread(() -> {
            try {
                for (int j = 0; j < operationsPerThread; j++) {
                    String journeyId = "journey-" + threadIndex + "-" + j;
                    TestProgressListener listener = new TestProgressListener();
                    
                    registry.addJourneyListener(journeyId, listener);
                    registry.removeJourneyListener(journeyId, listener);
                }
            } finally {
                latch.countDown();
            }
        });
        
        threads.add(thread);
        thread.start();
    }
    
    assertTrue(latch.await(60, TimeUnit.SECONDS));
    
    // Verify no exceptions occurred
    for (Thread thread : threads) {
        assertFalse("Thread " + thread.getName() + " threw exception", 
                   thread.isAlive() && thread.getState() == Thread.State.TERMINATED);
    }
}

@Test
public void testConcurrentEventEmission() {
    ListenerRegistry registry = new ListenerRegistry();
    EventEmitter emitter = new EventEmitter(registry, null);
    
    // Add listener that counts events
    AtomicInteger eventCount = new AtomicInteger(0);
    ExtractionListener countingListener = new ExtractionListener() {
        @Override
        public void onJourneyStart(String journeyId) {
            eventCount.incrementAndGet();
        }
    };
    
    registry.addGlobalListener(countingListener);
    
    int threadCount = 10;
    int eventsPerThread = 1000;
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    List<Thread> threads = new ArrayList<>();
    
    for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i;
        Thread thread = new Thread(() -> {
            try {
                for (int j = 0; j < eventsPerThread; j++) {
                    emitter.emitEvent(new JourneyStartEvent("journey-" + threadIndex + "-" + j));
                }
            } finally {
                latch.countDown();
            }
        });
        
        threads.add(thread);
        thread.start();
    }
    
    assertTrue(latch.await(60, TimeUnit.SECONDS));
    
    // Verify all events were processed
    int expectedEvents = threadCount * eventsPerThread;
    assertEquals("Expected " + expectedEvents + " events, got " + eventCount.get(), 
                expectedEvents, eventCount.get());
}
```

### 5. Error Handling Testing

#### 5.1 Exception Handling Testing

**Test Classes:**
- `ErrorHandlingTest`
- `RecoveryTest`

**Test Scenarios:**
```java
@Test
public void testListenerExceptionHandling() {
    ListenerRegistry registry = new ListenerRegistry();
    EventEmitter emitter = new EventEmitter(registry, null);
    
    // Add listener that throws exceptions
    ExtractionListener faultyListener = new ExtractionListener() {
        @Override
        public void onJourneyStart(String journeyId) {
            throw new RuntimeException("Simulated error");
        }
    };
    
    // Add working listener
    TestProgressListener workingListener = new TestProgressListener();
    
    registry.addGlobalListener(faultyListener);
    registry.addGlobalListener(workingListener);
    
    // Emit event - should not fail
    emitter.emitEvent(new JourneyStartEvent("test-journey"));
    
    // Working listener should still receive events
    assertTrue(workingListener.receivedJourneyStart);
}

@Test
public void testRecoveryFromListenerFailures() {
    ListenerRegistry registry = new ListenerRegistry();
    EventEmitter emitter = new EventEmitter(registry, null);
    
    // Add listener that fails initially but recovers
    AtomicInteger failureCount = new AtomicInteger(0);
    ExtractionListener recoveringListener = new ExtractionListener() {
        @Override
        public void onJourneyStart(String journeyId) {
            if (failureCount.getAndIncrement() < 3) {
                throw new RuntimeException("Temporary failure");
            }
        }
    };
    
    registry.addGlobalListener(recoveringListener);
    
    // First 3 events should fail
    for (int i = 0; i < 3; i++) {
        emitter.emitEvent(new JourneyStartEvent("journey-" + i));
    }
    
    // 4th event should succeed
    emitter.emitEvent(new JourneyStartEvent("journey-4"));
    
    assertEquals(4, failureCount.get());
}
```

## Test Data and Fixtures

### 1. Mock Data

**Test Entities:**
```java
public class TestEntity {
    private String name;
    private List<String> items;
    private Map<String, Object> metadata;
    
    // Constructor, getters, setters
}

public class TestScorecard {
    private String seriesName;
    private List<TestInnings> teamInnings;
    
    // Constructor, getters, setters
}
```

**Test Listeners:**
```java
public class TestProgressListener implements ExtractionListener {
    public boolean receivedJourneyStart = false;
    public boolean receivedJourneyEnd = false;
    public boolean receivedEntityEvents = false;
    public boolean receivedFieldEvents = false;
    public List<ExtractionEvent> receivedEvents = new ArrayList<>();
    
    @Override
    public void onJourneyStart(String journeyId) {
        receivedJourneyStart = true;
        receivedEvents.add(new JourneyStartEvent(journeyId));
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        receivedJourneyEnd = true;
        receivedEvents.add(new JourneyEndEvent(journeyId));
    }
    
    // Implement other methods...
    
    public boolean hasEventWithPath(String path) {
        return receivedEvents.stream()
            .anyMatch(event -> event.getPath().equals(path));
    }
}
```

### 2. Test Utilities

**Test Helpers:**
```java
public class TestUtils {
    public static ExtractionEvent createTestEvent(EventType type, String journeyId, String path) {
        switch (type) {
                return new EntityStartEvent(journeyId, TestEntity.class, path);
            case FIELD_EXTRACT_END:
                return new FieldExtractEndEvent(journeyId, path, "test-value", true);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }
    
    public static void waitForCondition(Callable<Boolean> condition, long timeoutMs) 
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition.call()) {
                return;
            }
            Thread.sleep(10);
        }
        throw new TimeoutException("Condition not met within " + timeoutMs + "ms");
    }
}
```

## Test Execution

### 1. Test Categories

**Fast Tests (Unit Tests):**
- Event system tests
- Path utilities tests
- Basic listener tests
- Progress tracking tests

**Medium Tests (Integration Tests):**
- Journey integration tests
- Real page tests
- Error handling tests

**Slow Tests (Performance Tests):**
- High-frequency event tests
- Memory usage tests
- Concurrent access tests

### 2. Test Execution Strategy

**CI/CD Pipeline:**
```yaml
# .github/workflows/test.yml
name: Test WebJourney Progress Tracking

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run Fast Tests
      run: mvn test -Dtest="*Test" -DexcludedGroups="slow,integration"
    
    - name: Run Medium Tests
      run: mvn test -Dtest="*Test" -Dgroups="integration" -DexcludedGroups="slow"
    
    - name: Run Performance Tests (Nightly)
      if: github.event_name == 'schedule'
      run: mvn test -Dtest="*Test" -Dgroups="slow"
```

**Local Development:**
```bash
# Run all tests
mvn test

# Run only fast tests
mvn test -DexcludedGroups="slow,integration"

# Run specific test category
mvn test -Dgroups="integration"

# Run performance tests
mvn test -Dgroups="slow"

# Run with coverage
mvn test jacoco:report
```

### 3. Test Environment

**Required Dependencies:**
```xml
<dependencies>
    <!-- Testing dependencies -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Performance testing -->
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Quality Assurance

### 1. Code Coverage Requirements

**Minimum Coverage:**
- Line coverage: > 90%
- Branch coverage: > 85%
- Method coverage: > 95%

**Coverage Exclusions:**
- Generated code
- Main method
- Exception handling paths that are difficult to test

### 2. Static Analysis

**Tools:**
- SonarQube for code quality analysis
- SpotBugs for bug detection
- Checkstyle for code style enforcement

**Quality Gates:**
- No critical or blocker issues
- Code duplication < 3%
- Maintainability rating A
- Security rating A

### 3. Performance Benchmarks

**Baseline Metrics:**
- Event processing: < 1ms per event
- Memory overhead: < 10%
- Concurrent journeys: 100+
- Events per second: 1000+

**Regression Testing:**
- Automated performance regression detection
- Performance trend analysis
- Alerting on performance degradation

## Continuous Improvement

### 1. Test Maintenance

**Regular Activities:**
- Update test data and fixtures
- Refactor tests for better maintainability
- Add tests for new edge cases
- Remove obsolete tests

**Metrics Tracking:**
- Test execution time trends
- Flaky test identification
- Coverage improvement tracking
- Bug detection rate

### 2. Test Automation

**Automated Testing:**
- Nightly performance tests
- Automated regression testing
- Continuous integration testing
- Automated test result reporting

**Monitoring:**
- Test execution metrics
- Performance trend analysis
- Coverage reporting
- Quality gate enforcement

## Conclusion

This comprehensive testing strategy ensures the WebJourney Progress Tracking feature meets all quality requirements while maintaining backward compatibility and performance standards. The multi-level testing approach covers unit, integration, performance, and thread safety aspects, providing confidence in the feature's reliability and robustness.

Regular execution of this testing strategy will help maintain code quality, detect regressions early, and ensure the feature continues to meet user expectations in production environments.