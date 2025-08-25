# WebJourney Progress Tracking - Usage Examples

## Overview
This document provides comprehensive examples of how to use the WebJourney Progress Tracking feature in various scenarios, from basic progress monitoring to advanced custom implementations.

## Basic Usage

### 1. Simple Progress Listener

```java
public class SimpleProgressListener implements ExtractionListener {
    
    @Override
    public void onJourneyStart(String journeyId) {
        System.out.println("Journey started: " + journeyId);
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        System.out.println("Journey completed: " + journeyId);
    }
    
    @Override
    public void onEntityStart(String journeyId, Class<?> type, String path) {
        System.out.println("Processing entity: " + type.getSimpleName() + " at " + path);
    }
    
    @Override
    public void onEntityEnd(String journeyId, Class<?> type, String path) {
        System.out.println("Completed entity: " + type.getSimpleName() + " at " + path);
    }
}

// Usage
JourneyBuilder.path()
    .withExtractionListener(new SimpleProgressListener())
    .navigateTo("https://example.com/scorecard")
    .consumePage(Scorecard.class, scorecard -> {
        // Process scorecard
    });
```

### 2. Progress Bar Implementation

```java
public class ProgressBarListener implements ExtractionListener {
    private final Map<String, ProgressTracker> trackers = new ConcurrentHashMap<>();
    
    @Override
    public void onJourneyStart(String journeyId) {
        trackers.put(journeyId, new ProgressTracker());
        System.out.println("Starting journey: " + journeyId);
    }
    
    @Override
    public void onCollectionDiscovered(String journeyId, String path, int expectedSize) {
        ProgressTracker tracker = trackers.get(journeyId);
        if (tracker != null) {
            tracker.addCollection(path, expectedSize);
            updateProgressBar(journeyId);
        }
    }
    
    @Override
    public void onCollectionItemEnd(String journeyId, String path, int index) {
        ProgressTracker tracker = trackers.get(journeyId);
        if (tracker != null) {
            tracker.incrementProgress(path);
            updateProgressBar(journeyId);
        }
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        ProgressTracker tracker = trackers.remove(journeyId);
        if (tracker != null) {
            System.out.println("Journey completed: " + journeyId + " - 100%");
        }
    }
    
    private void updateProgressBar(String journeyId) {
        ProgressTracker tracker = trackers.get(journeyId);
        if (tracker != null) {
            double percentage = tracker.getOverallProgress();
            System.out.printf("Journey %s: %.1f%%%n", journeyId, percentage);
        }
    }
    
    private static class ProgressTracker {
        private final Map<String, Integer> collections = new HashMap<>();
        private final Map<String, Integer> progress = new HashMap<>();
        
        public void addCollection(String path, int size) {
            collections.put(path, size);
            progress.put(path, 0);
        }
        
        public void incrementProgress(String path) {
            progress.merge(path, 1, Integer::sum);
        }
        
        public double getOverallProgress() {
            if (collections.isEmpty()) return 0.0;
            
            int totalItems = collections.values().stream().mapToInt(Integer::intValue).sum();
            int completedItems = progress.values().stream().mapToInt(Integer::intValue).sum();
            
            return (double) completedItems / totalItems * 100;
        }
    }
}
```

## Advanced Usage

### 3. Real-time Dashboard Updates

```java
public class DashboardListener implements ExtractionListener {
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;
    
    public DashboardListener(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void onJourneyStart(String journeyId) {
        sendUpdate(journeyId, "STARTED", 0.0, "Journey started");
    }
    
    @Override
    public void onEntityStart(String journeyId, Class<?> type, String path) {
        sendUpdate(journeyId, "PROCESSING", null, "Processing " + type.getSimpleName());
    }
    
    @Override
    public void onCollectionItemEnd(String journeyId, String path, int index) {
        // Calculate progress based on collection completion
        double progress = calculateProgress(journeyId, path, index);
        sendUpdate(journeyId, "IN_PROGRESS", progress, "Processed item " + (index + 1));
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        sendUpdate(journeyId, "COMPLETED", 100.0, "Journey completed successfully");
    }
    
    private void sendUpdate(String journeyId, String status, Double progress, String message) {
        try {
            DashboardUpdate update = new DashboardUpdate(journeyId, status, progress, message);
            String json = objectMapper.writeValueAsString(update);
            webSocketService.broadcast("progress-updates", json);
        } catch (Exception e) {
            // Log error but don't fail the journey
            System.err.println("Failed to send dashboard update: " + e.getMessage());
        }
    }
    
    private double calculateProgress(String journeyId, String path, int index) {
        // Implementation to calculate progress percentage
        // This would integrate with the ExtractionState API
        return 0.0; // Placeholder
    }
    
    private static class DashboardUpdate {
        private final String journeyId;
        private final String status;
        private final Double progress;
        private final String message;
        private final Instant timestamp;
        
        // Constructor, getters, etc.
    }
}
```

### 4. Error Tracking and Reporting

```java
public class ErrorTrackingListener implements ExtractionListener {
    private final Map<String, List<ExtractionError>> journeyErrors = new ConcurrentHashMap<>();
    private final ErrorReportingService errorService;
    
    public ErrorTrackingListener(ErrorReportingService errorService) {
        this.errorService = errorService;
    }
    
    @Override
    public void onExtractError(String journeyId, String path, Throwable error) {
        ExtractionError extractionError = new ExtractionError(journeyId, path, error);
        
        journeyErrors.computeIfAbsent(journeyId, k -> new ArrayList<>())
                    .add(extractionError);
        
        // Log error locally
        System.err.printf("Extraction error in journey %s at %s: %s%n", 
                         journeyId, path, error.getMessage());
        
        // Report to external service
        errorService.reportError(extractionError);
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        List<ExtractionError> errors = journeyErrors.get(journeyId);
        if (errors != null && !errors.isEmpty()) {
            System.out.printf("Journey %s completed with %d errors%n", 
                            journeyId, errors.size());
            
            // Generate error report
            ErrorReport report = generateErrorReport(journeyId, errors);
            errorService.submitReport(report);
        }
        
        journeyErrors.remove(journeyId);
    }
    
    private ErrorReport generateErrorReport(String journeyId, List<ExtractionError> errors) {
        return new ErrorReport(journeyId, errors, Instant.now());
    }
    
    private static class ExtractionError {
        private final String journeyId;
        private final String path;
        private final Throwable error;
        private final Instant timestamp;
        
        // Constructor, getters, etc.
    }
    
    private static class ErrorReport {
        private final String journeyId;
        private final List<ExtractionError> errors;
        private final Instant generatedAt;
        
        // Constructor, getters, etc.
    }
}
```

### 5. Performance Monitoring

```java
public class PerformanceMonitoringListener implements ExtractionListener {
    private final Map<String, JourneyMetrics> journeyMetrics = new ConcurrentHashMap<>();
    private final MetricsService metricsService;
    
    public PerformanceMonitoringListener(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
    
    @Override
    public void onJourneyStart(String journeyId) {
        JourneyMetrics metrics = new JourneyMetrics(journeyId);
        journeyMetrics.put(journeyId, metrics);
        metrics.startTimer();
    }
    
    @Override
    public void onEntityStart(String journeyId, Class<?> type, String path) {
        JourneyMetrics metrics = journeyMetrics.get(journeyId);
        if (metrics != null) {
            metrics.recordEntityStart(type, path);
        }
    }
    
    @Override
    public void onEntityEnd(String journeyId, Class<?> type, String path) {
        JourneyMetrics metrics = journeyMetrics.get(journeyId);
        if (metrics != null) {
            metrics.recordEntityEnd(type, path);
        }
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        JourneyMetrics metrics = journeyMetrics.remove(journeyId);
        if (metrics != null) {
            metrics.stopTimer();
            
            // Send metrics to monitoring service
            metricsService.recordJourneyMetrics(metrics);
            
            // Log performance summary
            logPerformanceSummary(journeyId, metrics);
        }
    }
    
    private void logPerformanceSummary(String journeyId, JourneyMetrics metrics) {
        Duration duration = metrics.getDuration();
        int entitiesProcessed = metrics.getEntitiesProcessed();
        double avgEntityTime = metrics.getAverageEntityProcessingTime();
        
        System.out.printf("Journey %s completed in %s, processed %d entities, avg: %.2fms%n",
                         journeyId, duration, entitiesProcessed, avgEntityTime);
    }
    
    private static class JourneyMetrics {
        private final String journeyId;
        private final Instant startTime;
        private Instant endTime;
        private final Map<Class<?>, Long> entityProcessingTimes = new HashMap<>();
        private final Map<Class<?>, Integer> entityCounts = new HashMap<>();
        
        public void startTimer() {
            // Implementation
        }
        
        public void stopTimer() {
            endTime = Instant.now();
        }
        
        public void recordEntityStart(Class<?> type, String path) {
            // Implementation
        }
        
        public void recordEntityEnd(Class<?> type, String path) {
            // Implementation
        }
        
        public Duration getDuration() {
            if (endTime == null) return Duration.ZERO;
            return Duration.between(startTime, endTime);
        }
        
        public int getEntitiesProcessed() {
            return entityCounts.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        public double getAverageEntityProcessingTime() {
            // Implementation
            return 0.0;
        }
    }
}
```

## Integration Examples

### 6. Spring Boot Integration

```java
@Component
public class SpringProgressListener implements ExtractionListener {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public SpringProgressListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void onJourneyStart(String journeyId) {
        eventPublisher.publishEvent(new JourneyStartedEvent(journeyId));
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        eventPublisher.publishEvent(new JourneyCompletedEvent(journeyId));
    }
    
    @Override
    public void onExtractError(String journeyId, String path, Throwable error) {
        eventPublisher.publishEvent(new ExtractionErrorEvent(journeyId, path, error));
    }
}

// Spring events
public class JourneyStartedEvent extends ApplicationEvent {
    private final String journeyId;
    
    public JourneyStartedEvent(String journeyId) {
        super(journeyId);
        this.journeyId = journeyId;
    }
    
    public String getJourneyId() {
        return journeyId;
    }
}

// Usage in Spring Boot application
@SpringBootApplication
public class ScrapingApplication {
    
    @Bean
    public WebTraveller webTraveller() {
        WebTraveller traveller = new WebTraveller();
        traveller.addGlobalListener(new SpringProgressListener(applicationEventPublisher));
        return traveller;
    }
}
```

### 7. REST API Integration

```java
@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    
    private final ExtractionState extractionState;
    
    public ProgressController(ExtractionState extractionState) {
        this.extractionState = extractionState;
    }
    
    @GetMapping("/journeys")
    public ResponseEntity<Map<String, JourneyProgress>> getAllJourneyProgress() {
        Map<String, JourneyProgress> progress = extractionState.getAllJourneyProgress();
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/journeys/{journeyId}")
    public ResponseEntity<JourneyProgress> getJourneyProgress(@PathVariable String journeyId) {
        JourneyProgress progress = extractionState.getJourneyProgress(journeyId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/journeys/{journeyId}/work-units")
    public ResponseEntity<WorkUnits> getJourneyWorkUnits(@PathVariable String journeyId) {
        WorkUnits workUnits = WorkUnits.calculate(journeyId, extractionState);
        return ResponseEntity.ok(workUnits);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<ProgressSummary> getProgressSummary() {
        Map<String, JourneyProgress> allProgress = extractionState.getAllJourneyProgress();
        
        long activeJourneys = allProgress.size();
        long totalEvents = extractionState.getTotalEvents();
        long errorCount = extractionState.getErrorCount();
        
        ProgressSummary summary = new ProgressSummary(activeJourneys, totalEvents, errorCount);
        return ResponseEntity.ok(summary);
    }
    
    private static class ProgressSummary {
        private final long activeJourneys;
        private final long totalEvents;
        private final long errorCount;
        
        // Constructor, getters, etc.
    }
}
```

### 8. WebSocket Real-time Updates

```java
@Component
public class WebSocketProgressListener implements ExtractionListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketProgressListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    @Override
    public void onJourneyStart(String journeyId) {
        sendProgressUpdate(journeyId, "STARTED", 0.0);
    }
    
    @Override
    public void onCollectionItemEnd(String journeyId, String path, int index) {
        // Calculate progress and send update
        double progress = calculateProgress(journeyId, path, index);
        sendProgressUpdate(journeyId, "IN_PROGRESS", progress);
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        sendProgressUpdate(journeyId, "COMPLETED", 100.0);
    }
    
    private void sendProgressUpdate(String journeyId, String status, double progress) {
        ProgressUpdate update = new ProgressUpdate(journeyId, status, progress, Instant.now());
        
        // Send to specific journey topic
        messagingTemplate.convertAndSend("/topic/progress/" + journeyId, update);
        
        // Send to general progress topic
        messagingTemplate.convertAndSend("/topic/progress", update);
    }
    
    private double calculateProgress(String journeyId, String path, int index) {
        // Implementation to calculate progress percentage
        return 0.0; // Placeholder
    }
    
    private static class ProgressUpdate {
        private final String journeyId;
        private final String status;
        private final double progress;
        private final Instant timestamp;
        
        // Constructor, getters, etc.
    }
}
```

## Configuration Examples

### 9. Custom Event Filtering

```java
// Filter by event type
EventFilter entityEventsOnly = EventFilter.byEventType(
    EventType.ENTITY_START, 
    EventType.ENTITY_END
);

// Filter by path pattern
EventFilter scorecardEventsOnly = EventFilter.byPathPattern(
    "Scorecard.*", 
    "Scorecard.team_batting_innings.*"
);

// Custom filter
EventFilter customFilter = new EventFilter() {
    @Override
    public boolean shouldProcess(ExtractionEvent event) {
        // Only process events from specific journeys
        return event.getJourneyId().startsWith("scorecard-");
    }
};

// Usage
JourneyBuilder.path()
    .withEventFiltering(customFilter)
    .withExtractionListener(new MyListener())
    .navigateTo(url)
    .consumePage(Scorecard.class, consumer);
```

### 10. Asynchronous Event Processing

```java
public class AsyncProgressListener implements ExtractionListener {
    
    private final ExecutorService executor;
    private final ExtractionListener delegate;
    
    public AsyncProgressListener(ExtractionListener delegate, int threadPoolSize) {
        this.delegate = delegate;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    @Override
    public void onJourneyStart(String journeyId) {
        executor.submit(() -> delegate.onJourneyStart(journeyId));
    }
    
    @Override
    public void onJourneyEnd(String journeyId) {
        executor.submit(() -> delegate.onJourneyEnd(journeyId));
    }
    
    // Implement other methods similarly...
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// Usage
ExtractionListener asyncListener = new AsyncProgressListener(
    new MyListener(), 
    4
);

JourneyBuilder.path()
    .withExtractionListener(asyncListener)
    .navigateTo(url)
    .consumePage(Scorecard.class, consumer);
```

## Best Practices

### 1. Listener Design
- Keep listeners lightweight and non-blocking
- Use default methods in interfaces for optional events
- Implement proper error handling within listeners
- Consider using builder pattern for complex listener configuration

### 2. Performance Considerations
- Use event filtering to reduce unnecessary processing
- Implement asynchronous processing for slow operations
- Batch events when possible
- Monitor memory usage in long-running applications

### 3. Error Handling
- Never let listener exceptions fail the journey
- Implement proper logging and monitoring
- Use circuit breakers for external service calls
- Provide fallback mechanisms for critical operations

### 4. Testing
- Test listeners in isolation
- Mock external dependencies
- Test error scenarios and edge cases
- Verify thread safety in concurrent environments

## Troubleshooting

### Common Issues

1. **Events not being received**
   - Check if progress tracking is enabled
   - Verify listener registration
   - Check event filtering configuration

2. **Performance degradation**
   - Review listener implementation
   - Consider using asynchronous processing
   - Implement event filtering

3. **Memory leaks**
   - Ensure proper cleanup in listeners
   - Monitor listener lifecycle
   - Use weak references for long-lived objects

4. **Thread safety issues**
   - Verify listener thread safety
   - Use proper synchronization
   - Test concurrent scenarios