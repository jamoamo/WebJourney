# WebJourney Progress Tracking - Technical Specification

## Overview
This document provides the technical implementation details for the WebJourney Progress Tracking feature, including class diagrams, sequence diagrams, and implementation guidelines.

## Architecture Overview

### High-Level Architecture
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Journey      │    │  Event System    │    │   Listeners    │
│   Execution    │───▶│   & Router       │───▶│   & Handlers    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Progress      │    │   Event Store    │    │   State API     │
│  Tracking      │    │   & Buffering    │    │   & Queries     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Core Classes and Interfaces

### 1. Event System

#### ExtractionEvent (Base Class)
```java
public abstract class ExtractionEvent {
    private final String journeyId;
    private final String path;
    private final Instant timestamp;
    private final long threadId;
    private final EventType eventType;
    
    // Constructor, getters, and utility methods
}
```

#### EventType (Enum)
```java
public enum EventType {
    JOURNEY_START,
    JOURNEY_END,
    NAVIGATE,
    CONSUME_START,
    CONSUME_END,
    ENTITY_START,
    ENTITY_END,
    EXTRACT_ERROR,
    FIELD_EXTRACT_START,
    FIELD_EXTRACT_END,
    COLLECTION_DISCOVERED,
    COLLECTION_ITEM_START,
    COLLECTION_ITEM_END
}
```

#### Specific Event Classes
```java
public class JourneyStartEvent extends ExtractionEvent {
    // No additional fields needed
}

public class EntityStartEvent extends ExtractionEvent {
    private final Class<?> entityType;
    
    public EntityStartEvent(String journeyId, String path, Class<?> entityType) {
        super(journeyId, path, EventType.ENTITY_START);
        this.entityType = entityType;
    }
}

public class FieldExtractEndEvent extends ExtractionEvent {
    private final Object value;
    private final boolean success;
    
    public FieldExtractEndEvent(String journeyId, String path, Object value, boolean success) {
        super(journeyId, path, EventType.FIELD_EXTRACT_END);
        this.value = value;
        this.success = success;
    }
}
```

### 2. Listener System

#### ExtractionListener (Interface)
```java
public interface ExtractionListener {
    // Journey lifecycle events
    default void onJourneyStart(String journeyId) {}
    default void onJourneyEnd(String journeyId) {}
    default void onNavigate(String journeyId, String url) {}
    default void onConsumeStart(String journeyId, Class<?> targetType) {}
    default void onConsumeEnd(String journeyId, Class<?> targetType) {}
    
    // Entity extraction events
    default void onEntityStart(String journeyId, Class<?> type, String path) {}
    default void onEntityEnd(String journeyId, Class<?> type, String path) {}
    default void onExtractError(String journeyId, String path, Throwable error) {}
    
    // Field-level events
    default void onEntityStart(String journeyId, String path) {}
    default void onFieldExtractEnd(String journeyId, String path, Object value, boolean success) {}
    
    // Collection processing events
    default void onCollectionDiscovered(String journeyId, String path, int expectedSize) {}
    default void onCollectionItemStart(String journeyId, String path, int index) {}
    default void onCollectionItemEnd(String journeyId, String path, int index) {}
}
```

#### ListenerRegistry
```java
public class ListenerRegistry {
    private final Map<String, List<ExtractionListener>> journeyListeners;
    private final List<ExtractionListener> globalListeners;
    private final ReadWriteLock lock;
    
    public void addJourneyListener(String journeyId, ExtractionListener listener);
    public void removeJourneyListener(String journeyId, ExtractionListener listener);
    public void addGlobalListener(ExtractionListener listener);
    public void removeGlobalListener(ExtractionListener listener);
    public List<ExtractionListener> getListenersForEvent(ExtractionEvent event);
}
```

### 3. Progress Tracking

#### ExtractionState
```java
public class ExtractionState {
    private final Map<String, JourneyProgress> journeys;
    private final long totalEvents;
    private final long errorCount;
    private final Instant lastUpdate;
    private final ReadWriteLock lock;
    
    public void updateJourneyProgress(String journeyId, ExtractionEvent event);
    public JourneyProgress getJourneyProgress(String journeyId);
    public Map<String, JourneyProgress> getAllJourneyProgress();
    public long getTotalEvents();
    public long getErrorCount();
}
```

#### JourneyProgress
```java
public class JourneyProgress {
    private final String journeyId;
    private volatile JourneyStatus status;
    private final Instant startTime;
    private volatile Instant lastActivity;
    private final Map<String, Object> metrics;
    private final List<ExtractionError> errors;
    private final AtomicLong eventCount;
    private final AtomicLong errorCount;
    
    public void recordEvent(ExtractionEvent event);
    public void recordError(ExtractionError error);
    public double getProgressPercentage();
    public Duration getElapsedTime();
    public Duration getEstimatedTimeRemaining();
}
```

#### WorkUnits
```java
public class WorkUnits {
    private final int total;
    private final int completed;
    private final double percentage;
    private final Duration estimatedTimeRemaining;
    
    public static WorkUnits calculate(String journeyId, ExtractionState state);
}
```

### 4. Path Generation

#### PathBuilder
```java
public class PathBuilder {
    private final List<String> segments;
    
    public PathBuilder();
    public PathBuilder addSegment(String segment);
    public PathBuilder addIndex(int index);
    public PathBuilder addField(String fieldName);
    public String build();
    public static PathBuilder fromString(String path);
}
```

#### Path Utilities
```java
public class PathUtils {
    public static String normalizePath(String path);
    public static boolean matchesPattern(String path, String pattern);
    public static String getParentPath(String path);
    public static String getFieldName(String path);
    public static int getArrayIndex(String path);
}
```

## Implementation Details

### 1. Event Emission

#### EventEmitter
```java
public class EventEmitter {
    private final ListenerRegistry listenerRegistry;
    private final ExtractionState extractionState;
    private final ExecutorService eventExecutor;
    
    public void emitEvent(ExtractionEvent event);
    public void emitEventAsync(ExtractionEvent event);
    public void emitEventBatch(List<ExtractionEvent> events);
}
```

#### Integration Points
```java
// In Journey class
public class Journey {
    private final EventEmitter eventEmitter;
    
    public void execute() {
        eventEmitter.emitEvent(new JourneyStartEvent(journeyId));
        try {
            // Journey execution logic
            eventEmitter.emitEvent(new JourneyEndEvent(journeyId));
        } catch (Exception e) {
            eventEmitter.emitEvent(new ExtractErrorEvent(journeyId, "root", e));
            throw e;
        }
    }
}

// In WebTraveller class
public class WebTraveller {
    private final EventEmitter eventEmitter;
    
    public <T> T consumePage(Class<T> targetType, Consumer<T> consumer) {
        eventEmitter.emitEvent(new ConsumeStartEvent(journeyId, targetType));
        try {
            T result = // existing consumption logic
            eventEmitter.emitEvent(new ConsumeEndEvent(journeyId, targetType));
            return result;
        } catch (Exception e) {
            eventEmitter.emitEvent(new ExtractErrorEvent(journeyId, "consume", e));
            throw e;
        }
    }
}
```

### 2. Thread Safety

#### Lock Strategy
- Use `ReadWriteLock` for `ExtractionState` and `ListenerRegistry`
- Use `volatile` for frequently updated fields in `JourneyProgress`
- Use `AtomicLong` for counters
- Use `ConcurrentHashMap` for thread-safe collections

#### Event Processing
```java
public class AsyncEventProcessor {
    private final ExecutorService executor;
    private final BlockingQueue<ExtractionEvent> eventQueue;
    private final int maxQueueSize;
    
    public void processEvent(ExtractionEvent event);
    public void shutdown();
    public boolean isShutdown();
}
```

### 3. Performance Optimizations

#### Event Batching
```java
public class EventBatcher {
    private final List<ExtractionEvent> batch;
    private final int maxBatchSize;
    private final Duration maxBatchDelay;
    private final ScheduledExecutorService scheduler;
    
    public void addEvent(ExtractionEvent event);
    public void flushBatch();
    private void scheduleBatchFlush();
}
```

#### Event Filtering
```java
public class EventFilter {
    private final Set<EventType> allowedEventTypes;
    private final Set<String> allowedPaths;
    private final Set<String> excludedPaths;
    
    public boolean shouldProcess(ExtractionEvent event);
    public static EventFilter acceptAll();
    public static EventFilter byEventType(EventType... types);
    public static EventFilter byPathPattern(String... patterns);
}
```

## Configuration

### System Properties
```properties
# Enable/disable progress tracking
webjourney.progress.enabled=true

# Event processing configuration
webjourney.progress.async.enabled=true
webjourney.progress.async.threads=4
webjourney.progress.async.queue.size=1000

# Batching configuration
webjourney.progress.batching.enabled=true
webjourney.progress.batching.max.size=100
webjourney.progress.batching.max.delay=100ms

# Memory management
webjourney.progress.memory.max.journeys=1000
webjourney.progress.memory.max.events.per.journey=10000
```

### Builder Configuration
```java
JourneyBuilder.path()
    .withProgressTracking(true)
    .withEventFiltering(EventFilter.byEventType(EventType.ENTITY_START, EventType.ENTITY_END))
    .withExtractionListener(new MyListener())
    .navigateTo(url)
    .consumePage(Scorecard.class, consumer);
```

## Error Handling

### Listener Exceptions
```java
public class SafeEventProcessor {
    public void processEventSafely(ExtractionEvent event, List<ExtractionListener> listeners) {
        for (ExtractionListener listener : listeners) {
            try {
                routeEventToListener(event, listener);
            } catch (Exception e) {
                logListenerError(listener, event, e);
                // Continue processing other listeners
            }
        }
    }
}
```

### Recovery Mechanisms
```java
public class ListenerRecovery {
    public void attemptRecovery(ExtractionListener listener, Exception error);
    public boolean isRecoverable(Exception error);
    public void removeFaultyListener(ExtractionListener listener);
}
```

## Testing Strategy

### Unit Tests
- Test all event types and payloads
- Verify listener registration and removal
- Test thread safety and concurrency
- Validate path generation logic

### Integration Tests
- Test with real web pages
- Verify event emission during actual scraping
- Test listener performance under load
- Validate progress state accuracy

### Performance Tests
- Measure event processing latency
- Test with high-frequency events
- Validate memory usage patterns
- Test scalability with multiple journeys

## Monitoring and Debugging

### Metrics Collection
```java
public class ProgressMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordEventProcessed(ExtractionEvent event);
    public void recordListenerLatency(String listenerClass, Duration latency);
    public void recordMemoryUsage(long bytes);
}
```

### Debug Logging
```java
public class ProgressLogger {
    private static final Logger logger = LoggerFactory.getLogger(ProgressLogger.class);
    
    public void logEvent(ExtractionEvent event);
    public void logListenerRegistration(String journeyId, String listenerClass);
    public void logProgressUpdate(String journeyId, double percentage);
}
```

## Migration Guide

### Existing Code
No changes required for existing code. Progress tracking is completely optional.

### Adding Progress Tracking
```java
// Before (existing code)
JourneyBuilder.path()
    .navigateTo(url)
    .consumePage(Scorecard.class, consumer);

// After (with progress tracking)
JourneyBuilder.path()
    .withProgressTracking(true)
    .withExtractionListener(new ProgressListener())
    .navigateTo(url)
    .consumePage(Scorecard.class, consumer);
```

### Global Progress Monitoring
```java
// Add global listener for all journeys
WebTraveller.addGlobalListener(new GlobalProgressListener());

// Monitor all active journeys
ExtractionState state = WebTraveller.getProgressState();
Map<String, JourneyProgress> allProgress = state.getAllJourneyProgress();
```

## Future Enhancements

### Phase 2 Features
- Event persistence and replay
- Advanced filtering and routing
- Performance analytics dashboard
- Integration with monitoring systems

### Long-term Vision
- Machine learning-based progress prediction
- Adaptive event filtering based on usage patterns
- Integration with external monitoring tools
- Support for distributed scraping operations