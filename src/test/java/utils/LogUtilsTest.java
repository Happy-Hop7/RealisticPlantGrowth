package utils;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing logging functionality in LogUtils class.
 * This ensures proper logging output format for different log levels.
 */
public class LogUtilsTest {
    private ServerMock server;
    private RealisticPlantGrowth plugin;
    private Logger mockPluginLogger;

    private ByteArrayOutputStream logCapture;
    private ByteArrayOutputStream errCapture;

    private PrintStream defaultStream;
    private PrintStream defaultErrorStream;

    private final String INFO_MSG = "This is an Info.";
    private final String WARN_MSG = "This is a Warning.";
    private final String ERROR_MSG = "This is an Error.";

    /**
     * Enum for different log levels used in testing.
     */
    public enum LOG_LVL {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR");

        LOG_LVL(String value) {}
    }

    /**
     * Setup method to initialize resources before each test.
     */
    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock(); // Initialize MockBukkit server

        // Capture console output
        logCapture = new ByteArrayOutputStream();
        errCapture = new ByteArrayOutputStream();

        // Save default System.out and System.err streams to restore later
        defaultStream = System.out;
        defaultErrorStream = System.err;

        // Redirect System.out and System.err to capture log and error outputs
        System.setOut(new PrintStream(logCapture, true));
        System.setErr(new PrintStream(errCapture, true));

        // Set the logger verbosity flags to false for basic logging
        LogUtils.setVerbose(false);
        LogUtils.setDebug(false);

        // Get the logger for RealisticPlantGrowth class
        mockPluginLogger = LogUtils.getLogger(RealisticPlantGrowth.class);
    }

    /**
     * Tear down method to restore resources after each test.
     * Prints captured log and error messages.
     */
    @AfterEach
    public void tearDown(TestInfo testInfo) {
        // Get the name of the test that was executed
        String testName = testInfo.getDisplayName();

        // Restore default output streams
        System.setOut(defaultStream);
        System.setErr(defaultErrorStream);

        if (logCapture.size() > 0) {
            // Print captured System.out logs (excluding logger messages)
            System.out.println(testName + "Captured Log:");
            System.out.println(logCapture);
        }

        if (errCapture.size() > 0) {
            // Print captured error logs (if any)
            System.out.println(testName + "Captured Error Log:");
            System.out.println(errCapture);
        }


        // Unmock the MockBukkit server
        MockBukkit.unmock();
    }

    // --- INFO Log Level Tests ---

    /**
     * Test the logging of an INFO message using the plugin logger.
     */
    @Test
    @DisplayName("Test0: Log an INFO message")
    public void testLogInfo() {
        // Log message using the plugin logger
        mockPluginLogger.info(INFO_MSG);
        // Validate the log line matches the expected format
        assertLogLine(logCapture.toString().trim(), LOG_LVL.INFO, INFO_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of an INFO message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test1: Log INFO with class name in verbose mode")
    public void testLogInfoWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(this.getClass());
        // Log message using the logger for this test class
        logger.info(INFO_MSG);
        // Validate the log line matches the verbose format
        assertLogLineVerbose(logCapture.toString().trim(), LOG_LVL.INFO, INFO_MSG, this.getClass());
    }

    // --- WARN Log Level Tests ---

    /**
     * Test the logging of a WARN message using the plugin logger.
     */
    @Test
    @DisplayName("Test2: Log a WARN message")
    public void testLogWarn() {
        // Log message using the plugin logger
        mockPluginLogger.warn(WARN_MSG);
        // Validate the log line matches the expected format
        assertLogLine(logCapture.toString().trim(), LOG_LVL.WARN, WARN_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of a WARN message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test3: Log WARN with class name in verbose mode")
    public void testLogWarnWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(this.getClass());
        // Log message using the logger for this test class
        logger.warn(WARN_MSG);
        // Validate the log line matches the verbose format
        assertLogLineVerbose(logCapture.toString().trim(), LOG_LVL.WARN, WARN_MSG, this.getClass());
    }

    // --- ERROR Log Level Tests ---

    /**
     * Test the logging of an ERROR message using the plugin logger.
     */
    @Test
    @DisplayName("Test4: Log an ERROR message")
    public void testLogError() {
        // Log message using the plugin logger
        mockPluginLogger.error(ERROR_MSG);
        // Validate the log line matches the expected format
        assertLogLine(logCapture.toString().trim(), LOG_LVL.ERROR, ERROR_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of an ERROR message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test5: Log ERROR with class name in verbose mode")
    public void testLogErrorWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);
        // Log message using the logger for this test class
        logger.error(ERROR_MSG);
        // Validate the log line matches the verbose format
        assertLogLineVerbose(logCapture.toString().trim(), LOG_LVL.ERROR, ERROR_MSG, LogUtilsTest.class);
    }


    // --- Component Tests ---

    @Test
    @DisplayName("Log INFO with Adventure Component")
    public void testLogInfoWithComponent() {
        // Create a sample component
        Component component = Component.text("Test INFO message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.info method
        LogUtils.info(mockPluginLogger, component);

        // Verify that the logger's info method was called with the correct serialized message
        String expectedMessage = "Test INFO message. This is very red";
        assertLogLine(logCapture.toString().trim(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Log WARN with Adventure Component")
    public void testLogWarnWithComponent() {
        // Create a sample component
        Component component = Component.text("Test WARN message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.warn method
        LogUtils.warn(mockPluginLogger, component);

        // Verify that the logger's warn method was called with the correct serialized message
        String expectedMessage = "Test WARN message. This is very red";
        assertLogLine(logCapture.toString().trim(), LOG_LVL.WARN, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Log ERROR with Adventure Component")
    public void testLogErrorWithComponent() {
        // Create a sample component
        Component component = Component.text("Test ERROR message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.error method
        LogUtils.error(mockPluginLogger, component);

        // Verify that the logger's error method was called with the correct serialized message
        String expectedMessage = "Test ERROR message. This is very red";
        assertLogLine(logCapture.toString().trim(), LOG_LVL.ERROR, expectedMessage, RealisticPlantGrowth.class);
    }


    // --- Verbose Logging Tests ---

    @Test
    @DisplayName("Verbose logging enabled - Component")
    public void testVerboseLoggingEnabledWithComponent() {
        // Enable verbose mode
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Create a test component
        Component component = Component.text("Verbose Component Test").color(NamedTextColor.RED);

        // Call verbose method
        LogUtils.verbose(logger, component);

        // Expected verbose component
        String expectedMessage = "VERBOSE >> Verbose Component Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLineVerbose(logCapture.toString().trim(), LOG_LVL.INFO, expectedMessage, LogUtilsTest.class);
    }

    @Test
    @DisplayName("Verbose logging enabled - String message")
    public void testVerboseLoggingEnabledWithString() {
        // Enable verbose mode
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Test message
        String message = "Verbose String Test";

        // Call verbose method
        LogUtils.verbose(logger, message);

        // Expected verbose component
        String expectedMessage = "VERBOSE >> Verbose String Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLineVerbose(logCapture.toString().trim(), LOG_LVL.INFO, expectedMessage, LogUtilsTest.class);
    }

    @Test
    @DisplayName("Verbose logging disabled")
    public void testVerboseLoggingDisabled() {
        // Disable verbose mode
        LogUtils.setVerbose(false); // default value
        LogUtils.setDebug(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Call verbose methods
        LogUtils.verbose(logger, "This should not log");
        LogUtils.verbose(logger, Component.text("This should not log either").color(NamedTextColor.RED));

        // Verify that logger.info is never called
        assertTrue(logCapture.toString().trim().isEmpty());
    }

    @Test
    @DisplayName("Debug logging enabled - Component")
    public void testDebugLoggingEnabledWithComponent() {
        // Enable verbose mode (debug uses verbose flag)
        LogUtils.setDebug(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Create a test component
        Component component = Component.text("Debug Component Test").color(NamedTextColor.RED);

        // Call debug method
        LogUtils.debug(logger, component);

        // Expected debug component
        String expectedMessage = "DEBUG >> Debug Component Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLine(logCapture.toString().trim(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Debug logging enabled - String message")
    public void testDebugLoggingEnabledWithString() {
        // Enable verbose mode (debug uses verbose flag)
        LogUtils.setDebug(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Test message
        String message = "Debug String Test";

        // Call debug method
        LogUtils.debug(logger, message);

        // Expected debug component
        String expectedMessage = "DEBUG >> Debug String Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLine(logCapture.toString().trim(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Debug logging disabled")
    public void testDebugLoggingDisabled() {
        // Disable verbose mode
        LogUtils.setDebug(false);
        LogUtils.setVerbose(true);
        Logger logger = LogUtils.getLogger(LogUtils.class);

        // Call debug methods
        LogUtils.debug(logger, "This should not log");
        LogUtils.debug(logger, Component.text("This should not log either"));

        // Verify that logger.info is never called
        assertTrue(logCapture.toString().trim().isEmpty());
    }

    /**
     * Helper method to assert that the logged line matches the expected format.
     * This method checks if the log message matches a regex pattern based on the log level, message, and class.
     *
     * @param logLine the captured log line
     * @param expectedLevel the expected log level (INFO, WARN, ERROR)
     * @param expectedMessage the expected log message
     * @param clazz the class associated with the logger
     */
    public void assertLogLine(String logLine, LOG_LVL expectedLevel, String expectedMessage, Class<?> clazz) {
        String logFormatRegex = "\\[\\d{2}:\\d{2}:\\d{2} " + expectedLevel + "]: \\[" + clazz.getSimpleName() + "] " + expectedMessage;
        assertTrue(logLine.matches(logFormatRegex), "Log line does not match regex pattern: " + logLine);
    }

    /**
     * Helper method to assert that the logged line in verbose mode matches the expected format.
     * This method checks if the log message matches a verbose format regex pattern with class name.
     *
     * @param logLine the captured log line
     * @param expectedLevel the expected log level (INFO, WARN, ERROR)
     * @param expectedMessage the expected log message
     * @param clazz the class associated with the logger
     */
    public void assertLogLineVerbose(String logLine, LOG_LVL expectedLevel, String expectedMessage, Class<?> clazz) {
        String logFormatRegex = "\\[\\d{2}:\\d{2}:\\d{2} " + expectedLevel + "]: \\[" + RealisticPlantGrowth.class.getSimpleName() + "->" + clazz.getSimpleName() + "] " + expectedMessage;
        assertTrue(logLine.matches(logFormatRegex), "Log line does not match verbose regex pattern: " + logLine);
    }
}
