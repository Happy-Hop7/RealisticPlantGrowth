package utils;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
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
    private Logger pluginLogger;

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
        pluginLogger = LogUtils.getLogger(RealisticPlantGrowth.class);
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
        pluginLogger.info(INFO_MSG);
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
        pluginLogger.warn(WARN_MSG);
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
        pluginLogger.error(ERROR_MSG);
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
        String logFormatRegex = "\\[\\d{2}:\\d{2}:\\d{2} " + expectedLevel + "]: \\[" + RealisticPlantGrowth.class.getSimpleName() + "#" + clazz.getSimpleName() + "] " + expectedMessage;
        assertTrue(logLine.matches(logFormatRegex), "Log line does not match verbose regex pattern: " + logLine);
    }
}
