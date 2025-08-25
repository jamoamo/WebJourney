# WebJourney Progress Tracking - Product Requirements Document

## Document Information
- **Feature**: WebJourney Progress Tracking with Journey Hooks
- **Version**: 1.0
- **Date**: 2024
- **Status**: Draft
- **Owner**: Development Team
- **Stakeholders**: Product, Engineering, QA

## Executive Summary

The CricketArchive Scraper library currently lacks real-time progress visibility during web scraping operations. This feature will introduce a comprehensive event-driven progress tracking system that provides users with granular visibility into extraction status, enabling better user experience, debugging capabilities, and support for incremental data processing.

## Problem Statement

### Current State
- Library can only report completion at the end of a full journey
- No hooks to track progress during individual page consumption, entity extraction, or field mapping
- Users have no visibility into ongoing operations
- Difficult to debug extraction failures or monitor long-running journeys
- No support for progress bars or status updates in UI applications

### Impact
- Poor user experience during long scraping operations
- Limited debugging capabilities
- No support for incremental data processing
- Difficult to implement progress indicators in client applications

## Solution Overview

Implement a comprehensive event-driven progress tracking system with the following components:

1. **Journey Lifecycle Events** - Track journey start/end and navigation
2. **Entity Extraction Events** - Monitor entity mapping progress
3. **Field-Level Events** - Track individual field extraction
4. **Collection Processing Events** - Monitor collection item processing
5. **Progress State API** - Provide current state snapshots
6. **Listener Management** - Support multiple listeners with thread safety

## Functional Requirements

### 1. Journey Lifecycle Events

#### 1.1 Journey Start Event
- **Event**: `onJourneyStart(String journeyId)`
- **Trigger**: When a journey begins execution
- **Payload**: `journeyId` - Unique identifier for the journey
- **Use Case**: Initialize progress tracking, start timers, update UI status

#### 1.2 Journey End Event
- **Event**: `onJourneyEnd(String journeyId)`
- **Trigger**: When a journey completes (success or failure)
- **Payload**: `journeyId` - Unique identifier for the journey
- **Use Case**: Finalize progress tracking, cleanup resources, update completion status

#### 1.3 Navigation Event
- **Event**: `onNavigate(String journeyId, String url)`
- **Trigger**: Before navigating to a new URL
- **Payload**: 
  - `journeyId` - Unique identifier for the journey
  - `url` - Target URL being navigated to
- **Use Case**: Track page transitions, update navigation progress

#### 1.4 Page Consumption Events
- **Event**: `onConsumeStart(String journeyId, Class<?> targetType)`
- **Trigger**: Before starting page consumption
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `targetType` - Target entity class being consumed
- **Use Case**: Track page processing start, initialize entity-specific progress

- **Event**: `onConsumeEnd(String journeyId, Class<?> targetType)`
- **Trigger**: After completing page consumption
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `targetType` - Target entity class that was consumed
- **Use Case**: Track page processing completion, update progress metrics

### 2. Entity Extraction Events

#### 2.1 Entity Start Event
- **Event**: `onEntityStart(String journeyId, Class<?> type, String path)`
- **Trigger**: Before mapping an entity
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `type` - Entity class being mapped
  - `path` - Hierarchical path to the element
- **Use Case**: Track entity processing start, initialize entity-specific progress

#### 2.2 Entity End Event
- **Event**: `onEntityEnd(String journeyId, Class<?> type, String path)`
- **Trigger**: After successfully mapping an entity
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `type` - Entity class that was mapped
  - `path` - Hierarchical path to the element
- **Use Case**: Track entity processing completion, update success metrics

#### 2.3 Extraction Error Event
- **Event**: `onExtractError(String journeyId, String path, Throwable error)`
- **Trigger**: When extraction fails
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path where extraction failed
  - `error` - Exception that caused the failure
- **Use Case**: Error tracking, debugging, failure reporting

### 3. Field-Level Events

#### 3.1 Field Extract Start Event
- **Event**: `onFieldExtractStart(String journeyId, String path)`
- **Trigger**: Before extracting a field
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path to the field
- **Use Case**: Track field processing start, initialize field-specific progress

#### 3.2 Field Extract End Event
- **Event**: `onFieldExtractEnd(String journeyId, String path, Object value, boolean success)`
- **Trigger**: After field extraction
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path to the field
  - `value` - Extracted value (may be null if failed)
  - `success` - Whether extraction was successful
- **Use Case**: Track field processing completion, update success metrics, collect extracted values

### 4. Collection Processing Events

#### 4.1 Collection Discovery Event
- **Event**: `onCollectionDiscovered(String journeyId, String path, int expectedSize)`
- **Trigger**: When a collection is found
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path to the collection
  - `expectedSize` - Expected number of items (may be -1 if unknown)
- **Use Case**: Initialize collection progress tracking, set up progress bars

#### 4.2 Collection Item Processing Events
- **Event**: `onCollectionItemStart(String journeyId, String path, int index)`
- **Trigger**: Before processing collection item
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path to the collection
  - `index` - Index of the item being processed
- **Use Case**: Track individual item processing start

- **Event**: `onCollectionItemEnd(String journeyId, String path, int index)`
- **Trigger**: After processing collection item
- **Payload**:
  - `journeyId` - Unique identifier for the journey
  - `path` - Hierarchical path to the collection
  - `index` - Index of the item that was processed
- **Use Case**: Track individual item processing completion, update progress

### 5. Progress State API

#### 5.1 Extraction State Snapshot
- **API**: `ExtractionState snapshot()`
- **Purpose**: Provide current progress state for all active journeys
- **Returns**: `ExtractionState` object containing:
  - Active journey IDs
  - Current progress for each journey
  - Error states and counts
  - Performance metrics

#### 5.2 Work Units API
- **API**: `WorkUnits getWorkUnits(String journeyId)`
- **Purpose**: Provide coarse progress information for UI percentage bars
- **Returns**: `WorkUnits` object containing:
  - Total work units
  - Completed work units
  - Progress percentage
  - Estimated time remaining

### 6. Listener Management

#### 6.1 Multiple Listeners Support
- Support multiple listeners per journey
- Allow listeners to be added/removed dynamically
- Maintain listener order for consistent event processing

#### 6.2 Global Listener Registration
- Support global listeners that receive events from all journeys
- Allow filtering of global listeners by journey ID or path patterns
- Maintain separation between journey-specific and global listeners

#### 6.3 Thread Safety
- Ensure thread-safe event emission
- Support concurrent listener execution
- Handle listener exceptions gracefully

#### 6.4 Backpressure Handling
- Implement backpressure handling for slow listeners
- Support asynchronous event processing
- Provide configurable buffer sizes and overflow strategies

## Non-Functional Requirements

### Performance
- Events should be lightweight and non-blocking
- Support for filtering events by path patterns
- Optional event batching for high-frequency operations
- Memory-efficient state tracking for long-running journeys
- Maximum event processing latency: < 1ms per event

### Scalability
- Support for hundreds of concurrent journeys
- Handle thousands of events per second
- Memory usage should scale linearly with active journeys
- Support for long-running journeys (hours to days)

### Reliability
- Graceful degradation if listeners throw exceptions
- No impact on core scraping functionality if progress tracking fails
- Automatic cleanup of failed listeners
- Support for listener recovery and re-registration

### Compatibility
- Maintain backward compatibility with existing API
- Should be optional (journeys work without listeners)
- Support for both synchronous and asynchronous event handling
- No breaking changes to existing public interfaces

## Technical Design

### Event Structure
All events must include:
- `journeyId` - Unique identifier for the journey
- `path` - Hierarchical path to the element
- `timestamp` - When the event occurred (ISO 8601 format)
- `threadId` - For debugging multi-threaded scenarios
- `eventType` - Type of event for filtering and routing

### Path Convention
Use dot notation with array indices:
- `Scorecard.series_name`
- `Scorecard.team_batting_innings[0].team_name`
- `Scorecard.team_batting_innings[1].player_batting_innings[3].runs`

### Integration Points
- `JourneyBuilder.withExtractionListener(ExtractionListener listener)`
- `WebTraveller.addGlobalListener(ExtractionListener listener)`
- Events emitted during `consumePage()` operations
- Integration with existing `Journey` and `WebTraveller` classes

### Event Flow
1. Journey execution triggers events at various points
2. Events are routed to registered listeners
3. Listeners process events asynchronously (if configured)
4. Progress state is updated based on event processing
5. UI components can query progress state for updates

## API Design

### Core Interfaces

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
    default void onFieldExtractStart(String journeyId, String path) {}
    default void onFieldExtractEnd(String journeyId, String path, Object value, boolean success) {}
    
    // Collection processing events
    default void onCollectionDiscovered(String journeyId, String path, int expectedSize) {}
    default void onCollectionItemStart(String journeyId, String path, int index) {}
    default void onCollectionItemEnd(String journeyId, String path, int index) {}
}
```

### Progress State Classes

```java
public class ExtractionState {
    private Map<String, JourneyProgress> journeys;
    private long totalEvents;
    private long errorCount;
    private Instant lastUpdate;
    
    // Getters and utility methods
}

public class JourneyProgress {
    private String journeyId;
    private JourneyStatus status;
    private Instant startTime;
    private Instant lastActivity;
    private Map<String, Object> metrics;
    private List<ExtractionError> errors;
    
    // Getters and utility methods
}

public class WorkUnits {
    private int total;
    private int completed;
    private double percentage;
    private Duration estimatedTimeRemaining;
    
    // Getters and utility methods
}
```

### Builder Integration

```java
public class JourneyBuilder {
    public JourneyBuilder withExtractionListener(ExtractionListener listener);
    public JourneyBuilder withProgressTracking(boolean enabled);
    public JourneyBuilder withEventFiltering(EventFilter filter);
}
```

## Implementation Plan

### Phase 1: Core Infrastructure (Week 1-2)
- Implement event system infrastructure
- Create `ExtractionListener` interface
- Implement basic event routing
- Add thread-safe listener management

### Phase 2: Event Implementation (Week 3-4)
- Implement all event types
- Add event emission points in existing code
- Create event payload classes
- Implement path generation logic

### Phase 3: Progress Tracking (Week 5-6)
- Implement `ExtractionState` and related classes
- Add progress calculation logic
- Implement work units API
- Add performance metrics collection

### Phase 4: Integration & Testing (Week 7-8)
- Integrate with existing `JourneyBuilder` and `WebTraveller`
- Add comprehensive unit tests
- Performance testing and optimization
- Documentation and examples

## Testing Strategy

### Unit Testing
- Test all event types and payloads
- Verify listener registration and removal
- Test thread safety and concurrency
- Validate path generation logic

### Integration Testing
- Test with real web pages
- Verify event emission during actual scraping
- Test listener performance under load
- Validate progress state accuracy

### Performance Testing
- Measure event processing latency
- Test with high-frequency events
- Validate memory usage patterns
- Test scalability with multiple journeys

### Memory Leak Testing
- Long-running journey tests
- Listener cleanup verification
- Memory profiling and analysis
- Stress testing with resource constraints

## Success Metrics

### Functional Metrics
- All event types are emitted correctly
- Progress tracking accuracy > 99%
- Listener registration/removal works reliably
- Error handling works gracefully

### Performance Metrics
- Event processing latency < 1ms
- Memory overhead < 10% for typical usage
- Support for 100+ concurrent journeys
- Handle 1000+ events/second

### Quality Metrics
- Test coverage > 90%
- Zero breaking changes to existing API
- Backward compatibility maintained
- Comprehensive documentation

## Risks and Mitigation

### Technical Risks
- **Risk**: Performance impact on existing functionality
  - **Mitigation**: Extensive performance testing, optional feature
- **Risk**: Memory leaks in long-running journeys
  - **Mitigation**: Comprehensive memory testing, automatic cleanup
- **Risk**: Thread safety issues
  - **Mitigation**: Thorough concurrency testing, immutable event objects

### Business Risks
- **Risk**: Feature complexity may delay release
  - **Mitigation**: Phased implementation, MVP approach
- **Risk**: User adoption may be low
  - **Mitigation**: Optional feature, backward compatibility

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

## Conclusion

The WebJourney Progress Tracking feature will significantly improve the user experience and debugging capabilities of the CricketArchive Scraper library. By providing real-time visibility into extraction progress, users will be able to monitor long-running operations, track performance, and implement progress indicators in their applications.

The phased implementation approach ensures minimal risk while delivering maximum value. The optional nature of the feature maintains backward compatibility while providing powerful new capabilities for users who need them.

## Appendix

### A. Event Examples
Detailed examples of each event type with sample payloads

### B. Performance Benchmarks
Expected performance characteristics and testing results

### C. Migration Guide
How existing users can adopt the new feature

### D. API Reference
Complete API documentation for all new classes and methods