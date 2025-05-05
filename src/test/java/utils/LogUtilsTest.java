package utils;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing logging functionality in LogUtils class.
 * This ensures proper logging output format for different log levels.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class LogUtilsTest {
    private org.apache.logging.log4j.core.Logger mockPluginLogger;
    private org.apache.logging.log4j.core.Logger mockClassLogger;


    private TestListAppender testAppender;


    private final String INFO_MSG = "This is an Info.";
    private final String WARN_MSG = "This is a Warning.";
    private final String ERROR_MSG = "This is an Error.";

    private final String LOG_PATH = "target/plugins/RealisticPlantGrowth";


    // Custom ListAppender for capturing log messages
    private static class TestListAppender extends AbstractAppender {
        private final List<String> messages = new ArrayList<>();

        protected TestListAppender(String name) {
            super(name, null, PatternLayout.newBuilder()
                    .withPattern("[%d{HH:mm:ss} %p]: [%c{1}] %m%n")
                    .build(), false, null);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(getLayout().toSerializable(event).toString());
        }

        public List<String> getMessages() {
            return messages;
        }
    }


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
        // Container for console output
        testAppender = new TestListAppender("TestListAppender");
        testAppender.start();

        // Initialize the logger with verbosity flags set to false for basic logging
        LogUtils.initialize(new File(LOG_PATH), false, false);

        // Get the logger for RealisticPlantGrowth class
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        mockPluginLogger = context.getLogger(LogUtils.getLogger(RealisticPlantGrowth.class).getName());
        mockPluginLogger.addAppender(testAppender);
        mockPluginLogger.setAdditive(false);

    }


    /**
     * Tear down method to restore resources after each test.
     * Prints captured log and error messages.
     */
    @AfterEach
    public void tearDown(TestInfo testInfo) {
        if (mockPluginLogger != null && testAppender != null) {
            mockPluginLogger.removeAppender(testAppender);
            testAppender.stop();
        }

        // reset logging context
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure();
    }



    // --- File System Tests ---

    @Test
    @DisplayName("Test00: Create Log Files")
    public void createLogFiles() throws Exception {
        for (LogUtils.LogFile logFile : LogUtils.LogFile.values()) {
            String testMessage = "LogFile created.";
            LogUtils.logToFileAsync(logFile, testMessage);

            // Define the expected file
            File file = new File(LOG_PATH + "/log", logFile.getValue());

            // Wait for the file to be written (basic polling)
            boolean fileWritten = false;
            for (int i = 0; i < 100; i++) { // up to ~1 second
                if (file.exists() && file.length() > 0) {
                    fileWritten = true;
                    break;
                }
                Thread.sleep(100);
            }

            assertTrue(fileWritten, "Log file was not created or is empty: " + file.getPath());

            // Optional: check file content
            String content = Files.readString(file.toPath());
            assertTrue(content.contains(testMessage), "Log file does not contain expected message.");
        }
    }

    // --- INFO Log Level Tests ---

    @Test
    public void testInfoLogging() {
        Logger logger = LogUtils.getLogger(LogUtils.class);
        LogUtils.info(mockPluginLogger, Component.text("Test message"));

        List<String> messages = testAppender.getMessages();
        System.out.println(messages.getFirst());
        assertFalse(messages.isEmpty());
        assertTrue(messages.getFirst().contains("Test message"));
        assertLogLine(messages.getFirst(), LOG_LVL.INFO, "Test message", RealisticPlantGrowth.class);
    }

    /**
     * Test the logging of an INFO message using the plugin logger.
     */
    @Test
    @DisplayName("Test01: Log an INFO String")
    public void testLogInfo() {
        // Log message using the plugin logger
        mockPluginLogger.info(INFO_MSG);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Validate the log line matches the expected format
        assertLogLine(messages.getFirst(), LOG_LVL.INFO, INFO_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of an INFO message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test02: Log INFO String with class name in verbose mode")
    public void testLogInfoWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(this.getClass());
        // Log message using the logger for this test class
        logger.info(INFO_MSG);
        // Get logged messages
        List<String> messages = testAppender.getMessages();
        System.out.println(messages.getFirst());
        // Validate the log line matches the verbose format
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.INFO, INFO_MSG, this.getClass());
    }

    // --- WARN Log Level Tests ---

    /**
     * Test the logging of a WARN message using the plugin logger.
     */
    @Test
    @DisplayName("Test03: Log a WARN message")
    public void testLogWarn() {
        // Log message using the plugin logger
        mockPluginLogger.warn(WARN_MSG);
        // Get logged messages
        List<String> messages = testAppender.getMessages();
        // Validate the log line matches the expected format
        assertLogLine(messages.getFirst(), LOG_LVL.WARN, WARN_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of a WARN message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test04: Log WARN with class name in verbose mode")
    public void testLogWarnWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(this.getClass());
        // Log message using the logger for this test class
        logger.warn(WARN_MSG);
        // Get logged messages
        List<String> messages = testAppender.getMessages();
        // Validate the log line matches the verbose format
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.WARN, WARN_MSG, this.getClass());
    }

    // --- ERROR Log Level Tests ---

    /**
     * Test the logging of an ERROR message using the plugin logger.
     */
    @Test
    @DisplayName("Test05: Log an ERROR message")
    public void testLogError() {
        // Log message using the plugin logger
        mockPluginLogger.error(ERROR_MSG);
        // Get logged messages
        List<String> messages = testAppender.getMessages();
        // Validate the log line matches the expected format
        assertLogLine(messages.getFirst(), LOG_LVL.ERROR, ERROR_MSG, RealisticPlantGrowth.class);
    }

    /**
     * Test logging of an ERROR message with class name included in verbose mode.
     */
    @Test
    @DisplayName("Test06: Log ERROR with class name in verbose mode")
    public void testLogErrorWithClass() {
        // Enable verbose logging for class-specific logs
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(LogUtilsTest.class);
        // Log message using the logger for this test class
        logger.error(ERROR_MSG);
        // Get logged messages
        List<String> messages = testAppender.getMessages();
        // Validate the log line matches the verbose format
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.ERROR, ERROR_MSG, LogUtilsTest.class);
    }


    // --- Component Tests ---

    @Test
    @DisplayName("Test07: Log INFO with Adventure Component")
    public void testLogInfoWithComponent() {
        // Create a sample component
        Component component = Component.text("Test INFO message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.info method
        LogUtils.info(mockPluginLogger, component);

        // Get logged messages
        List<String> messages = testAppender.getMessages();
        // Verify that the logger's info method was called with the correct serialized message
        String expectedMessage = "Test INFO message. This is very red";
        assertLogLine(messages.getFirst(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Test08: Log WARN with Adventure Component")
    public void testLogWarnWithComponent() {
        // Create a sample component
        Component component = Component.text("Test WARN message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.warn method
        LogUtils.warn(mockPluginLogger, component);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Verify that the logger's warn method was called with the correct serialized message
        String expectedMessage = "Test WARN message. This is very red";
        assertLogLine(messages.getFirst(), LOG_LVL.WARN, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Test09: Log ERROR with Adventure Component")
    public void testLogErrorWithComponent() {
        // Create a sample component
        Component component = Component.text("Test ERROR message. ")
                .append(Component.text("This is very red").color(NamedTextColor.RED));

        // Call the LogUtils.error method
        LogUtils.error(mockPluginLogger, component);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Verify that the logger's error method was called with the correct serialized message
        String expectedMessage = "Test ERROR message. This is very red";
        assertLogLine(messages.getFirst(), LOG_LVL.ERROR, expectedMessage, RealisticPlantGrowth.class);
    }


    @Test
    @DisplayName("Test10: Log ERROR with Component and Throwable")
    void testLogErrorWithComponentAndThrowable() {
        Throwable throwable = new IllegalArgumentException("Test exception");

        // Create a sample component
        Component component = Component.text("Test ERROR message.");

        // Call method
        LogUtils.error(mockPluginLogger, component, throwable);

        // Verify that the logger's error method was called with the correct serialized message
        String expectedMessage = "Test ERROR message.";

        //assertThat(expectedMessage, logCapture.toString().contains(expectedMessage));
        //assertThat(expectedMessage, logCapture.toString().contains(throwable.toString()));

    }

    // --- Verbose Logging Tests ---

    @Test
    @DisplayName("Test11: Verbose logging enabled - Component")
    public void testVerboseLoggingEnabledWithComponent() {
        // Enable verbose mode
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(LogUtilsTest.class);

        // Create a test component
        Component component = Component.text("Verbose Component Test").color(NamedTextColor.RED);

        // Call verbose method
        LogUtils.verbose(logger, component);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Expected verbose component
        String expectedMessage = "VERBOSE >> Verbose Component Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.INFO, expectedMessage, LogUtilsTest.class);
    }

    @Test
    @DisplayName("Test12: Verbose logging enabled - String message")
    public void testVerboseLoggingEnabledWithString() {
        // Enable verbose mode
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(LogUtilsTest.class);

        // Test message
        String message = "Verbose String Test";

        // Call verbose method
        LogUtils.verbose(logger, message);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Expected verbose component
        String expectedMessage = "VERBOSE >> Verbose String Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.INFO, expectedMessage, LogUtilsTest.class);
    }

    @Test
    @DisplayName("Test13: Verbose logging disabled")
    public void testVerboseLoggingDisabled() {
        // Disable verbose mode
        LogUtils.setVerbose(false); // default value
        LogUtils.setDebug(true);
        Logger logger = getClassLogger(LogUtilsTest.class);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Call verbose methods
        LogUtils.verbose(logger, "This should not log");
        LogUtils.verbose(logger, Component.text("This should not log either").color(NamedTextColor.RED));

        // Verify that logger.info is never called
        assertTrue(messages.isEmpty(), "Expected no log messages");
    }

    @Test
    @DisplayName("Test14: Debug logging enabled - Component")
    public void testDebugLoggingEnabledWithComponent() {
        // Enable verbose mode (debug uses verbose flag)
        LogUtils.setDebug(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Create a test component
        Component component = Component.text("Debug Component Test").color(NamedTextColor.RED);

        // Call debug method
        LogUtils.debug(logger, component);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Expected debug component
        String expectedMessage = "DEBUG >> Debug Component Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLine(messages.getFirst(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Test15: Debug logging enabled - String message")
    public void testDebugLoggingEnabledWithString() {
        // Enable verbose mode (debug uses verbose flag)
        LogUtils.setDebug(true);
        Logger logger = LogUtils.getLogger(LogUtilsTest.class);

        // Test message
        String message = "Debug String Test";

        // Call debug method
        LogUtils.debug(logger, message);

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Expected debug component
        String expectedMessage = "DEBUG >> Debug String Test";

        // Verify the logger.info method is called with the expected serialized message
        assertLogLine(messages.getFirst(), LOG_LVL.INFO, expectedMessage, RealisticPlantGrowth.class);
    }

    @Test
    @DisplayName("Test16: Debug logging disabled, verbose enabled")
    public void testDebugLoggingVerbose() {
        // Disable verbose mode
        LogUtils.setDebug(false);
        LogUtils.setVerbose(true);
        Logger logger = getClassLogger(LogUtils.class);

        // Test message
        String message = "Debug String Test";

        // Call debug methods
        LogUtils.debug(logger, message);

        // Expected debug component
        String expectedMessage = "DEBUG >> Debug String Test";

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Verify the logger.info method is called with the expected serialized message
        assertLogLineVerbose(messages.getFirst(), LOG_LVL.INFO, expectedMessage, LogUtils.class);
    }

    @Test
    @DisplayName("Test17: Debug logging disabled, verbose disabled")
    public void testDebugLoggingDisabled() {
        // Disable verbose mode
        LogUtils.setDebug(false);
        LogUtils.setVerbose(false);
        Logger logger = getClassLogger(LogUtils.class);

        // Call debug methods
        LogUtils.debug(logger, "This should not log");
        LogUtils.debug(logger, Component.text("This should not log either"));

        // Get logged messages
        List<String> messages = testAppender.getMessages();

        // Verify that logger.info is never called
        assertTrue(messages.isEmpty(), "Expected no log messages");
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
        // Convert expected level to string for regex pattern
        String levelString = expectedLevel.name(); // Ensures the log level is in uppercase (e.g., INFO, DEBUG)

        // Ensure expected message does not include any special characters that might break the regex
        String escapedMessage = Pattern.quote(expectedMessage); // Escape any regex special characters in the expected message

        // Create the regular expression pattern
        String logFormatRegex = String.format(
                "\\[\\d{2}:\\d{2}:\\d{2} %s\\]: \\[%s\\] %s\\s*",
                levelString,  // Log level part
                clazz.getSimpleName(),  // Logger name part (simple name of the class)
                escapedMessage  // The actual expected log message
        );

        // Assert that the log line matches the pattern
        System.out.println("Log Line: \n" + logLine); // Print the log line to debug
        assertThat(logLine, logLine.matches(logFormatRegex));
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
        // Convert expected level to string for regex pattern
        String levelString = expectedLevel.name(); // Ensures log level is in uppercase

        // Escape expected message to safely include in regex
        String escapedMessage = Pattern.quote(expectedMessage);

        // Construct the regular expression pattern
        String logFormatRegex = String.format(
                "\\[\\d{2}:\\d{2}:\\d{2} %s]: \\[%s->%s] %s\\s*",
                levelString,  // Log level
                RealisticPlantGrowth.class.getSimpleName(),  // Outer logger class
                clazz.getSimpleName(),  // Target class
                escapedMessage  // Escaped message content
        );

        // Optionally print the log line for debugging
        System.out.println("Log Line (Verbose): \n" + logLine);

        // Assert that the log line matches the pattern
        assertThat(logLine, logLine.matches(logFormatRegex));
    }

    private org.apache.logging.log4j.core.Logger getClassLogger(Class<?> clazz) {
        // Get the logger for RealisticPlantGrowth class
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.Logger logger = context.getLogger(LogUtils.getLogger(clazz).getName());
        testAppender.start();
        logger.addAppender(testAppender);
        logger.setAdditive(false);
        return logger;
    }

}
