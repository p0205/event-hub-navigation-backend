// package com.utem.event_hub_navigation;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.test.util.ReflectionTestUtils; // Useful for testing private/protected methods if necessary, but avoid if possible

// import com.itextpdf.text.DocumentException;
// import com.utem.event_hub_navigation.dto.EventAttendanceReportDTO;
// import com.utem.event_hub_navigation.listener.ReportGenerationListener;
// import com.utem.event_hub_navigation.model.Event;
// import com.utem.event_hub_navigation.model.EventReport;
// import com.utem.event_hub_navigation.model.ReportType;
// import com.utem.event_hub_navigation.repo.EventRepo;
// import com.utem.event_hub_navigation.repo.EventReportRepo;
// import com.utem.event_hub_navigation.service.EventReportService;
// import com.utem.event_hub_navigation.service.impl.EventReportServiceImpl;
// import com.utem.event_hub_navigation.utils.SupabaseStorageService;

// import java.io.IOException;
// import java.util.Optional;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// // Use MockitoExtension to enable Mockito annotations
// @ExtendWith(MockitoExtension.class)
// class ReportGenerationTests {

//     // --- Mocks for EventReportService dependencies ---
//     @Mock
//     private EventRepo eventRepo;
//     @Mock
//     private EventReportRepo eventReportRepo;
//     @Mock
//     private SupabaseStorageService supabaseStorageService;
//     // Mock the internal PDF generation logic (assuming these are methods in EventReportService)
//     // If these were separate classes, you would mock those classes.
//     // Since they are internal methods, we'll use a Spy on EventReportService
//     // Or, if we want pure unit tests, we can mock the *results* of these methods
//     // Let's assume generateEventAttendanceReport calls getAttendanceReportData and generateAttendanceReportPDF internally
//     // For testing generateEventAttendanceReport, we'll mock the internal calls if possible, or use a Spy
//     // Let's use a Spy to test generateEventAttendanceReport while mocking its internal calls

//     @InjectMocks
//     private EventReportServiceImpl eventReportService; // Use InjectMocks to inject mocks into this instance

//     // --- Mocks for ReportGenerationListener dependencies ---
//     // The listener depends on EventReportService, which is already @InjectMocks above
//     // If the listener had other dependencies, they would be mocked here.

//     @InjectMocks
//     private ReportGenerationListener reportGenerationListener; // Use InjectMocks for the listener

//     // Helper method to mock the internal PDF generation logic within EventReportService
//     // We use ReflectionTestUtils because generateEventAttendanceReport and generateAttendanceReportPDF are not public
//     // If these were public or protected, we could use doReturn/doThrow with a Spy
//     // A better design would be to extract PDF generation into a separate service that can be easily mocked.
//     // For this example, we'll use ReflectionTestUtils to set up the spy behavior on internal methods.
//     // NOTE: Using ReflectionTestUtils can make tests fragile if method names/signatures change.
//     // Consider refactoring the service to make internal helper methods protected or package-private if testing them directly is needed,
//     // or better yet, extract them into separate testable classes.
//     private EventReportServiceImpl spyEventReportService;

//     @BeforeEach
//     void setUp() {
//         // Initialize the spy for EventReportService before each test
//         spyEventReportService = spy(eventReportService);

//         // We need to inject the mocks into the spy manually if using @InjectMocks on the original
//         // Alternatively, we could just @Mock EventReportService and test the listener by verifying calls to the mock service.
//         // Let's stick to unit testing each component separately for clarity.
//         // So, we'll test EventReportService using @InjectMocks on it, and test the Listener by mocking EventReportService.

//         // Reset mocks before each test to ensure isolation
//         reset(eventRepo, eventReportRepo, supabaseStorageService);
//         // If using the spy approach for internal methods, reset the spy too
//         // reset(spyEventReportService);
//     }

//     // --- Tests for ReportGenerationListener ---

//     @Test
//     void handleEventCompleted_shouldCallStoreReport_whenEventIsCompleted() throws IOException {
//         // Arrange
//         Integer eventId = 123;
//         EventCompletedReportEvent event = new EventCompletedReportEvent(this, eventId);

//         // We need to mock the behavior of reportService.storeReport()
//         // Since we are testing the listener, we mock its direct dependency
//         EventReportService mockReportService = mock(EventReportService.class);
//         // Manually set the mocked reportService in the listener instance created by @InjectMocks
//         ReflectionTestUtils.setField(reportGenerationListener, "reportService", mockReportService);


//         // Act
//         reportGenerationListener.handleEventCompleted(event);

//         // Assert
//         // Verify that reportService.storeReport was called exactly once with the correct arguments
//         // We need to wait a bit because the listener is @Async, but for a simple unit test
//         // where we control the execution flow (Mockito doesn't run the actual async method),
//         // the call happens immediately within the test context.
//         // If using @SpringBootTest with a real Executor, you'd need Awaitility or similar.
//         verify(mockReportService, times(1)).storeReport(eventId, ReportType.ATTENDANCE);
//     }

//     @Test
//     void handleEventCompleted_shouldHandleException_whenStoreReportThrowsException() throws IOException {
//         // Arrange
//         Integer eventId = 456;
//         EventCompletedReportEvent event = new EventCompletedReportEvent(this, eventId);

//         EventReportService mockReportService = mock(EventReportService.class);
//          ReflectionTestUtils.setField(reportGenerationListener, "reportService", mockReportService);

//         // Mock reportService.storeReport to throw an exception
//         doThrow(new RuntimeException("Simulated report storage error")).when(mockReportService)
//               .storeReport(anyInt(), any(ReportType.class));

//         // Act & Assert
//         // We expect the exception to be caught within the listener, so we don't assert for a thrown exception here.
//         // We could use a logging framework and verify log output, but for this example,
//         // we'll just ensure storeReport was attempted.
//         reportGenerationListener.handleEventCompleted(event);

//         // Verify that storeReport was called, even though it threw an exception
//          verify(mockReportService, times(1)).storeReport(eventId, ReportType.ATTENDANCE);

//         // In a real scenario, you might verify logging or other error handling mechanisms were triggered.
//     }

//     // Note: Testing InterruptedException with Thread.sleep is tricky in unit tests.
//     // It's often better to assume the sleep works and focus on the logic around it.

//     // --- Tests for EventReportService.storeReport ---

//     @Test
//     void storeReport_shouldSuccessfullyStoreReport() throws IOException {
//         // Arrange
//         Integer eventId = 789;
//         ReportType reportType = ReportType.ATTENDANCE;
//         byte[] mockReportBytes = "mock pdf content".getBytes();
//         String mockFileUrl = "http://supabase.com/event-media/mock-file.pdf";
//         Event mockEvent = Event.builder().id(eventId).build(); // Create a mock Event object
//         EventReport expectedReport = EventReport.builder()
//                 .event(mockEvent)
//                 .type(reportType)
//                 .fileUrl(mockFileUrl)
//                 // generatedAt will be set by the service, we'll verify it's not null
//                 .build();

//         // Mock the internal call to generate the report bytes
//         // We need to use a Spy to mock internal method calls
//         spyEventReportService = spy(eventReportService);
//         // Re-inject mocks into the spy
//         ReflectionTestUtils.setField(spyEventReportService, "eventRepo", eventRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "eventReportRepo", eventReportRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "supabaseStorageService", supabaseStorageService);


//         try {
//              // Mock the internal method call using ReflectionTestUtils for private methods
//             // If generateEventAttendanceReport was protected/public, we'd use doReturn(mockReportBytes).when(spyEventReportService).generateEventAttendanceReport(eventId);
//             // Assuming generateEventAttendanceReport is private or needs internal mocking:
//             // This approach is complex and fragile. Refactoring is highly recommended.
//             // For simplicity in this example, let's assume generateEventAttendanceReport is testable and mock its result directly on the spy.
//             doReturn(mockReportBytes).when(spyEventReportService).generateEventAttendanceReport(eventId);

//         } catch (Exception e) {
//              // This catch is just to handle potential issues with mocking private methods if that approach was taken.
//              // With the doReturn on spy approach, this catch isn't strictly needed for this mock setup.
//         }


//         // Mock dependencies' behavior
//         when(supabaseStorageService.uploadFile(eq(mockReportBytes), eq("event-media"), anyString()))
//                 .thenReturn(mockFileUrl);
//         when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
//         when(eventReportRepo.save(any(EventReport.class))).thenAnswer(invocation -> {
//             // Return the saved object to allow verification of its state
//             EventReport savedReport = invocation.getArgument(0);
//             // Assert that generatedAt is set
//             assertNotNull(savedReport.getGeneratedAt());
//             // Set it on our expected object for easier comparison if needed, though verifying fields is better
//             expectedReport.setGeneratedAt(savedReport.getGeneratedAt()); // Capture the generatedAt value
//             return savedReport;
//         });

//         // Act
//         assertDoesNotThrow(() -> spyEventReportService.storeReport(eventId, reportType));

//         // Assert
//         // Verify interactions with dependencies
//         verify(spyEventReportService, times(1)).generateEventAttendanceReport(eventId); // Verify internal call
//         verify(supabaseStorageService, times(1)).uploadFile(eq(mockReportBytes), eq("event-media"), anyString());
//         verify(eventRepo, times(1)).findById(eventId);
//         verify(eventReportRepo, times(1)).save(any(EventReport.class)); // Verify save was called

//         // You can also verify the state of the saved report object if needed,
//         // by capturing the argument passed to save().
//         // ArgumentCaptor<EventReport> reportCaptor = ArgumentCaptor.forClass(EventReport.class);
//         // verify(eventReportRepo).save(reportCaptor.capture());
//         // EventReport capturedReport = reportCaptor.getValue();
//         // assertEquals(eventId, capturedReport.getEvent().getId());
//         // assertEquals(reportType, capturedReport.getType());
//         // assertEquals(mockFileUrl, capturedReport.getFileUrl());
//         // assertNotNull(capturedReport.getGeneratedAt());
//     }

//     @Test
//     void storeReport_shouldThrowException_whenEventNotFound() throws IOException {
//         // Arrange
//         Integer eventId = 999;
//         ReportType reportType = ReportType.ATTENDANCE;

//         // Mock eventRepo.findById to return empty
//         when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

//         // Act & Assert
//         RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
//             eventReportService.storeReport(eventId, reportType);
//         });

//         // Verify the exception message
//         assertTrue(thrown.getMessage().contains("Event with ID " + eventId + " not found."));

//         // Verify that subsequent calls were not made
//         verify(eventRepo, times(1)).findById(eventId);
//         verifyNoInteractions(supabaseStorageService);
//         verifyNoInteractions(eventReportRepo);
//         // Note: generateEventAttendanceReport might still be called before the event lookup,
//         // depending on the actual implementation order. The provided snippet calls it first.
//         // If it's called first, you might need to mock it to return some bytes,
//         // but verify that uploadFile and save are NOT called.
//         // Let's assume based on the snippet that generate is called before lookup:
//          spyEventReportService = spy(eventReportService);
//          ReflectionTestUtils.setField(spyEventReportService, "eventRepo", eventRepo);
//          ReflectionTestUtils.setField(spyEventReportService, "eventReportRepo", eventReportRepo);
//          ReflectionTestUtils.setField(spyEventReportService, "supabaseStorageService", supabaseStorageService);
//          byte[] mockReportBytes = "mock pdf content".getBytes();
//          doReturn(mockReportBytes).when(spyEventReportService).generateEventAttendanceReport(eventId);

//          thrown = assertThrows(RuntimeException.class, () -> {
//              spyEventReportService.storeReport(eventId, reportType);
//          });
//          assertTrue(thrown.getMessage().contains("Event with ID " + eventId + " not found."));
//          verify(spyEventReportService, times(1)).generateEventAttendanceReport(eventId); // Verify internal call was made
//          verify(eventRepo, times(1)).findById(eventId);
//          verifyNoInteractions(supabaseStorageService); // Verify upload was NOT called
//          verifyNoInteractions(eventReportRepo); // Verify save was NOT called

//     }

//     @Test
//     void storeReport_shouldThrowException_whenUploadFileThrowsException() throws IOException {
//          // Arrange
//         Integer eventId = 101;
//         ReportType reportType = ReportType.ATTENDANCE;
//         byte[] mockReportBytes = "mock pdf content".getBytes();
//         Event mockEvent = Event.builder().id(eventId).build();

//         spyEventReportService = spy(eventReportService);
//         ReflectionTestUtils.setField(spyEventReportService, "eventRepo", eventRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "eventReportRepo", eventReportRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "supabaseStorageService", supabaseStorageService);

//         doReturn(mockReportBytes).when(spyEventReportService).generateEventAttendanceReport(eventId);
//         when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
//         // Mock uploadFile to throw an exception
//         doThrow(new IOException("Simulated upload error")).when(supabaseStorageService)
//               .uploadFile(any(byte[].class), anyString(), anyString());

//         // Act & Assert
//         IOException thrown = assertThrows(IOException.class, () -> {
//             spyEventReportService.storeReport(eventId, reportType);
//         });

//         // Verify the exception message (or just that an IOException was thrown)
//         assertTrue(thrown.getMessage().contains("Simulated upload error"));

//         // Verify interactions
//         verify(spyEventReportService, times(1)).generateEventAttendanceReport(eventId);
//         verify(eventRepo, times(1)).findById(eventId);
//         verify(supabaseStorageService, times(1)).uploadFile(any(byte[].class), eq("event-media"), anyString());
//         verifyNoInteractions(eventReportRepo); // Verify save was NOT called
//     }

//      @Test
//     void storeReport_shouldThrowException_whenSaveReportThrowsException() throws IOException {
//          // Arrange
//         Integer eventId = 102;
//         ReportType reportType = ReportType.ATTENDANCE;
//         byte[] mockReportBytes = "mock pdf content".getBytes();
//         String mockFileUrl = "http://supabase.com/event-media/mock-file.pdf";
//         Event mockEvent = Event.builder().id(eventId).build();

//         spyEventReportService = spy(eventReportService);
//         ReflectionTestUtils.setField(spyEventReportService, "eventRepo", eventRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "eventReportRepo", eventReportRepo);
//         ReflectionTestUtils.setField(spyEventReportService, "supabaseStorageService", supabaseStorageService);

//         doReturn(mockReportBytes).when(spyEventReportService).generateEventAttendanceReport(eventId);
//         when(eventRepo.findById(eventId)).thenReturn(Optional.of(mockEvent));
//         when(supabaseStorageService.uploadFile(any(byte[].class), eq("event-media"), anyString()))
//                 .thenReturn(mockFileUrl);
//         // Mock save to throw an exception
//         doThrow(new RuntimeException("Simulated database save error")).when(eventReportRepo)
//               .save(any(EventReport.class));

//         // Act & Assert
//         RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
//             spyEventReportService.storeReport(eventId, reportType);
//         });

//         // Verify the exception message
//         assertTrue(thrown.getMessage().contains("Simulated database save error"));

//         // Verify interactions
//         verify(spyEventReportService, times(1)).generateEventAttendanceReport(eventId);
//         verify(eventRepo, times(1)).findById(eventId);
//         verify(supabaseStorageService, times(1)).uploadFile(any(byte[].class), eq("event-media"), anyString());
//         verify(eventReportRepo, times(1)).save(any(EventReport.class)); // Verify save was attempted
//     }


//     // --- Tests for EventReportService.generateEventAttendanceReport ---

//     @Test
//     void generateEventAttendanceReport_shouldReturnPdfBytes_whenSuccessful() {
//         // Arrange
//         Integer eventId = 456;
//         EventAttendanceReportDTO mockReportData = new EventAttendanceReportDTO(); // Mock DTO
//         byte[] mockPdfBytes = "mock pdf content".getBytes();

//         // We need to mock the internal calls to getAttendanceReportData and generateAttendanceReportPDF
//         spyEventReportService = spy(eventReportService);

//         try {
//             // Mock the internal methods using ReflectionTestUtils or make them protected/package-private
//             // Assuming getAttendanceReportData is private/protected:
//              doReturn(mockReportData).when(spyEventReportService).getAttendanceReportData(eventId);
//             // Assuming generateAttendanceReportPDF is private/protected:

//         } catch (Exception e) {
//             fail("Failed to mock internal methods: " + e.getMessage());
//         }


//         // Act
//         byte[] resultBytes = assertDoesNotThrow(() -> spyEventReportService.generateEventAttendanceReport(eventId));

//         // Assert
//         assertNotNull(resultBytes);
//         assertArrayEquals(mockPdfBytes, resultBytes);

//         // Verify internal calls were made
//          try {
//              verify(spyEventReportService, times(1)).getAttendanceReportData(eventId);
//              verify(spyEventReportService, times(1)).generateAttendanceReportPDF(eq(mockReportData), anyString());
//          } catch (Exception e) {
//               fail("Failed to verify internal method calls: " + e.getMessage());
//          }
//     }

//      @Test
//     void generateEventAttendanceReport_shouldThrowRuntimeException_whenPdfGenerationThrowsDocumentException() {
//         // Arrange
//         Integer eventId = 789;
//         EventAttendanceReportDTO mockReportData = new EventAttendanceReportDTO();

//         spyEventReportService = spy(eventReportService);

//         try {
//              doReturn(mockReportData).when(spyEventReportService).getAttendanceReportData(eventId);
//             // Mock generateAttendanceReportPDF to throw DocumentException
//              doThrow(new DocumentException("Simulated PDF error")).when(spyEventReportService).generateAttendanceReportPDF(eq(mockReportData), anyString());
//         } catch (Exception e) {
//              fail("Failed to mock internal methods: " + e.getMessage());
//         }


//         // Act & Assert
//         RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
//             spyEventReportService.generateEventAttendanceReport(eventId);
//         });

//         // Verify the exception message indicates a PDF generation error
//         assertTrue(thrown.getMessage().contains("Error generating PDF report"));
//         assertTrue(thrown.getMessage().contains("Simulated PDF error"));

//         // Verify internal calls were made
//          try {
//              verify(spyEventReportService, times(1)).getAttendanceReportData(eventId);
//              verify(spyEventReportService, times(1)).generateAttendanceReportPDF(eq(mockReportData), anyString());
//          } catch (Exception e) {
//               fail("Failed to verify internal method calls: " + e.getMessage());
//          }
//     }

//     @Test
//     void generateEventAttendanceReport_shouldThrowRuntimeException_whenPdfGenerationThrowsOtherException() {
//          // Arrange
//         Integer eventId = 888;
//         EventAttendanceReportDTO mockReportData = new EventAttendanceReportDTO();

//         spyEventReportService = spy(eventReportService);

//         try {
//              doReturn(mockReportData).when(spyEventReportService).getAttendanceReportData(eventId);
//             // Mock generateAttendanceReportPDF to throw a generic Exception
//              doThrow(new IllegalStateException("Another simulated error")).when(spyEventReportService).generateAttendanceReportPDF(eq(mockReportData), anyString());
//         } catch (Exception e) {
//              fail("Failed to mock internal methods: " + e.getMessage());
//         }


//         // Act & Assert
//         RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
//             spyEventReportService.generateEventAttendanceReport(eventId);
//         });

//         // Verify the exception message indicates a PDF generation error
//         assertTrue(thrown.getMessage().contains("Error generating PDF report"));
//          assertTrue(thrown.getMessage().contains("Another simulated error"));

//         // Verify internal calls were made
//          try {
//              verify(spyEventReportService, times(1)).getAttendanceReportData(eventId);
//              verify(spyEventReportService, times(1)).generateAttendanceReportPDF(eq(mockReportData), anyString());
//          } catch (Exception e) {
//               fail("Failed to verify internal method calls: " + e.getMessage());
//          }
//     }

//     // Note: Testing getAttendanceReportData would require mocking its dependencies (e.g., other repositories)
//     // and verifying the returned DTO. This would be a separate set of tests for that method.
// }
