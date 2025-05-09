package utils.version;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.version.UpdateChecker;
import de.nightevolution.realisticplantgrowth.utils.version.rest.ModrinthVersion;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateCheckerTest {

    private ServerMock server;
    private RealisticPlantGrowth rpgMock;
    private UpdateChecker updateChecker;

    @Mock
    private Logger loggerMock;

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    @Mock
    private BukkitScheduler schedulerMock;

    @Mock
    private BukkitTask bukkitTaskMock;

    @BeforeEach
    public void setUp() {
        // Setup MockBukkit
        server = MockBukkit.mock();

        // Create a direct mock of RealisticPlantGrowth
        rpgMock = mock(RealisticPlantGrowth.class);

        // Mock the necessary Plugin methods that UpdateChecker might use
        PluginDescriptionFile descriptionMock = mock(PluginDescriptionFile.class);
        when(descriptionMock.getVersion()).thenReturn("1.0.0");
        when(rpgMock.getDescription()).thenReturn(descriptionMock);

        // Set up the BukkitTask mock with a known task ID
        when(bukkitTaskMock.getTaskId()).thenReturn(1);

        // Mock static LogUtils.getLogger method
        try (MockedStatic<LogUtils> logUtilsMock = mockStatic(LogUtils.class)) {
            logUtilsMock.when(() -> LogUtils.getLogger(any())).thenReturn(loggerMock);

            // Mock Bukkit scheduler
            try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
                bukkitMock.when(Bukkit::getScheduler).thenReturn(schedulerMock);

                // Mock the asynchronous task execution to capture and execute the runnable
                doAnswer(invocation -> {
                    Runnable runnable = invocation.getArgument(1);
                    runnable.run(); // <-- Trigger it manually
                    return mockedTask;
                }).when(schedulerMock).runTaskAsynchronously(any(), (Consumer<? super BukkitTask>) any());


                // Mock the scheduled task execution
                doAnswer(invocation -> {
                    // Store and don't automatically execute
                    return bukkitTaskMock;
                }).when(schedulerMock).runTaskTimerAsynchronously(
                        eq(rpgMock),
                        any(Runnable.class),
                        anyLong(),
                        anyLong()
                );

                // Initialize UpdateChecker with our mocks
                updateChecker = new UpdateChecker(rpgMock, 24);

                // We need to manually set the task ID since we're not executing the scheduler
                // If there's no setter, you might need to use reflection or modify the class
                // For testing purposes, let's assume there's a method to set it directly
                setTaskIdInUpdateChecker(updateChecker, 1);
            }
        }
    }

    // Helper method to set task ID using reflection if there's no setter
    private void setTaskIdInUpdateChecker(UpdateChecker checker, int taskId) {
        try {
            java.lang.reflect.Field field = UpdateChecker.class.getDeclaredField("scheduledTaskId");
            field.setAccessible(true);
            field.set(checker, taskId);
        } catch (Exception e) {
            // This might not work if the field is named differently or final
            // In that case, consider modifying the UpdateChecker class to add a setter for testing
        }
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testConstructor() {
        // Since construction happens in setUp, we only need to verify here
        verify(schedulerMock).runTaskAsynchronously(eq(rpgMock), any(Runnable.class));
        verify(schedulerMock).runTaskTimerAsynchronously(eq(rpgMock), any(Runnable.class), anyLong(), anyLong());
        verify(loggerMock).info("Automatic update checks are enabled.");
    }

    @Test
    public void testCancelScheduledTask() {
        // Call the method to test
        updateChecker.cancelScheduledTask();

        // Verify that the task is canceled with the expected ID
        verify(schedulerMock).cancelTask(1);
    }

    @Test
    public void testGetVersionUpToDate() throws Exception {
        // Set up the HTTP response for an up-to-date scenario
        String jsonResponse = "[{\"version_number\":\"1.0.0\",\"name\":\"v1.0.0\"}]";
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(jsonResponse);

        // Create a mock consumer that will receive the version
        Consumer<ModrinthVersion> consumer = mock(Consumer.class);

        // Set up the HTTP client mock
        try (MockedStatic<HttpClient> httpClientStaticMock = mockStatic(HttpClient.class)) {
            httpClientStaticMock.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponseMock);



            // Extract and execute the runnable that would be scheduled
            doAnswer(invocation -> {
                // Execute the runnable directly during the test
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return bukkitTaskMock;
            }).when(schedulerMock).runTaskAsynchronously(eq(rpgMock), any(Runnable.class));

            // Call the method that would schedule the async task
            updateChecker.getVersion(consumer);

            // Verify the consumer was called with a valid version object
            verify(consumer).accept(any(ModrinthVersion.class));
            verify(loggerMock).info(contains("Your RealisticPlantGrowth plugin is up to date"), anyString());
        }
    }

    @Test
    public void testGetVersionUpdateAvailable() throws Exception {
        // Set up the HTTP response for a newer version available
        String jsonResponse = "[{\"version_number\":\"2.0.0\",\"name\":\"v2.0.0\"}]";
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(jsonResponse);

        // Create a mock consumer
        Consumer<ModrinthVersion> consumer = mock(Consumer.class);

        // Set up the HTTP client
        try (MockedStatic<HttpClient> httpClientStaticMock = mockStatic(HttpClient.class)) {
            httpClientStaticMock.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponseMock);



            // Execute the runnable immediately
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return bukkitTaskMock;
            }).when(schedulerMock).runTaskAsynchronously(eq(rpgMock), any(Runnable.class));

            // Call the method that would schedule the async task
            updateChecker.getVersion(consumer);

            // Verify the consumer was called and warning messages were logged
            verify(consumer).accept(any(ModrinthVersion.class));
            verify(loggerMock).warn(contains("A new version of RealisticPlantGrowth is available"));
        }
    }

    @Test
    public void testGetVersionApiError() throws Exception {
        // Set up the HTTP response for an API error
        when(httpResponseMock.statusCode()).thenReturn(404);

        // Create a mock consumer
        Consumer<ModrinthVersion> consumer = mock(Consumer.class);

        // Set up the HTTP client to return our error response
        try (MockedStatic<HttpClient> httpClientStaticMock = mockStatic(HttpClient.class)) {
            httpClientStaticMock.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponseMock);



            // Execute the runnable immediately
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return bukkitTaskMock;
            }).when(schedulerMock).runTaskAsynchronously(eq(rpgMock), any(Runnable.class));

            // Call the method that would schedule the async task
            updateChecker.getVersion(consumer);

            // Verify error was logged and consumer was not called
            verify(consumer, never()).accept(any(ModrinthVersion.class));
            verify(loggerMock).error(eq("Failed to check Modrinth API for RealisticPlantGrowth updates!"));
        }
    }

    @Test
    public void testGetVersionEmptyResponse() throws Exception {
        // Set up the HTTP response with an empty JSON array
        String jsonResponse = "[]";
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(jsonResponse);

        // Create a mock consumer
        Consumer<ModrinthVersion> consumer = mock(Consumer.class);

        // Set up the HTTP client
        try (MockedStatic<HttpClient> httpClientStaticMock = mockStatic(HttpClient.class)) {
            httpClientStaticMock.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponseMock);


            // Execute the runnable immediately
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return bukkitTaskMock;
            }).when(schedulerMock).runTaskAsynchronously(eq(rpgMock), any(Runnable.class));

            // Call the method that would schedule the async task
            updateChecker.getVersion(consumer);

            // Verify error was logged and consumer was called with null
            verify(consumer).accept(null);
            verify(loggerMock).error(contains("Modrinth API response contained no usable version data"));
        }
    }

    @Test
    public void testScheduleAutomaticUpdateChecks() {
        // Reset any previous invocations to get a clean slate
        reset(schedulerMock);

        // Call the method to schedule update checks
        updateChecker.scheduleAutomaticUpdateChecks();

        // Verify that runTaskTimerAsynchronously was called with the correct parameters
        // 24 hours = 24 * 60 * 60 * 20 = 1,728,000 ticks
        verify(schedulerMock).runTaskTimerAsynchronously(
                eq(rpgMock),  // Use rpgMock instead of plugin
                any(Runnable.class),
                eq(24 * 60L * 60L * 20L),
                eq(24 * 60L * 60L * 20L)
        );
    }
}