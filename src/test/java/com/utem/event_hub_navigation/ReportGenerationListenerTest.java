package com.utem.event_hub_navigation;

import com.utem.event_hub_navigation.listener.ReportGenerationListener;
import com.utem.event_hub_navigation.model.ReportType;
import com.utem.event_hub_navigation.service.EventReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportGenerationListenerTest {

    @Mock
    private EventReportService reportService;

    @InjectMocks
    private ReportGenerationListener reportGenerationListener;

    private static final Integer TEST_EVENT_ID = 123;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test if needed, though MockitoExtension does this
    }

    @Test
    void testHandleEventCompleted_callsStoreReport() throws Exception {
        // Arrange
        EventCompletedReportEvent event = new EventCompletedReportEvent(this, TEST_EVENT_ID);

        // Act
        reportGenerationListener.handleEventCompleted(event);

        // Assert
        // Verify that reportService.storeReport was called with the correct parameters
        verify(reportService, times(1)).storeReport(TEST_EVENT_ID, ReportType.ATTENDANCE);

        // You could also verify that no other interactions happened with the mock
        verifyNoMoreInteractions(reportService);
    }

    @Test
    void testHandleEventCompleted_handlesException() throws Exception {
        // Arrange
        EventCompletedReportEvent event = new EventCompletedReportEvent(this, TEST_EVENT_ID);
        String errorMessage = "Simulated report generation error";

        // Configure the mock to throw an exception when storeReport is called
        doThrow(new RuntimeException(errorMessage)).when(reportService).storeReport(TEST_EVENT_ID, ReportType.ATTENDANCE);

        // Act
        // The listener catches the exception, so we don't expect an exception here
        reportGenerationListener.handleEventCompleted(event);

        // Assert
        // Verify that storeReport was still attempted
        verify(reportService, times(1)).storeReport(TEST_EVENT_ID, ReportType.ATTENDANCE);

        // In a real scenario, you might want to verify logging or a different error handling mechanism
        // For this simple test, we primarily ensure the listener doesn't crash and still attempts the call.
        verifyNoMoreInteractions(reportService);
    }

    @Test
    void testAnnotationsPresent() {
        // Assert that the class and method have the expected annotations
        assertNotNull(ReportGenerationListener.class.getAnnotation(Component.class));

        try {
            assertNotNull(ReportGenerationListener.class.getMethod("handleEventCompleted", EventCompletedReportEvent.class).getAnnotation(Async.class));
            assertNotNull(ReportGenerationListener.class.getMethod("handleEventCompleted", EventCompletedReportEvent.class).getAnnotation(EventListener.class));
        } catch (NoSuchMethodException e) {
            // This should not happen if the method signature is correct
            throw new RuntimeException("Method handleEventCompleted not found", e);
        }
    }
}