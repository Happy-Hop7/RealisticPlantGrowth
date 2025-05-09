package de.nightevolution.realisticplantgrowth.utils;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for handling logging with different levels of verbosity and debug information.
 * Compatible with standard log4j2 methods while providing additional functionality.
 */
public class LogUtils {
    /** Default prefix for the plugin log messages */
    private static final String PLUGIN_PREFIX = "RealisticPlantGrowth";

    /** Prefix for debug messages */
    private static final Component DEBUG_PREFIX = Component.text("DEBUG >> ", NamedTextColor.RED);

    /** Prefix for verbose messages */
    private static final Component VERBOSE_PREFIX = Component.text("VERBOSE >> ", NamedTextColor.DARK_RED);

    /** Logger instance for the plugin */
    private static final Logger PLUGIN_LOGGER = LogManager.getLogger(PLUGIN_PREFIX);

    /** Flag for verbose logging */
    private static boolean verbose = false;

    /** Flag for debug logging */
    private static boolean debug = false;

    /** Executor for asynchronous logging into a plugin file */
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    /** Subdirectory for separate log files. */
    private static File logDir;

    /** Name of the log file */
    public enum LogFile {
        ERROR("error.log"),
        DEBUG("debug.log"),
        VERBOSE("verbose.log"),
        PLANT("plant.log"),
        STRUCTURE("structure.log"),
        PLAYER("player.log"),
        BONEMEAL("bonemeal.log"),
        CONFIG_DUMP("ConfigDump.log");

        private final String value;

        LogFile(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Initializes the LogUtils class
     */
    public static void initialize(RealisticPlantGrowth plugin, boolean debug, boolean verbose) {
        LogUtils.debug = debug;
        LogUtils.verbose = verbose;
        logDir = new File(plugin.getDataFolder(), "log");
    }

    /**
     * Initializes the LogUtils class
     */
    public static void initialize(File dataFolder, boolean debug, boolean verbose) {
        LogUtils.debug = debug;
        LogUtils.verbose = verbose;
        logDir = dataFolder;
    }

    /**
     * Shuts down the logging executor gracefully.
     */
    public static void shutdown() {
        logExecutor.shutdown();
    }

    // --- Standard Log4j2 Compatible Methods ---

    /**
     * Logs a message at DEBUG level
     */
    public static void debug(Logger logger, String message) {
        if (debug || verbose) {
            Component prefixedComponent = DEBUG_PREFIX.append(Component.text(message));
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(getLogFileName(), PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a message at DEBUG level with parameters
     */
    public static void debug(Logger logger, String message, Object... params) {
        if (debug || verbose) {
            String formattedMessage = String.format(message.replace("{}", "%s"), params);
            Component prefixedComponent = DEBUG_PREFIX.append(Component.text(formattedMessage));
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(getLogFileName(), PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a message at DEBUG level
     */
    public static void debug(Logger logger, Component message) {
        if (debug || verbose) {
            Component prefixedComponent = DEBUG_PREFIX.append(message);
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(getLogFileName(), PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a message at INFO level
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }

    /**
     * Logs a message at INFO level with parameters
     */
    public static void info(Logger logger, String message, Object... params) {
        logger.info(message, params);
    }

    /**
     * Logs a message at INFO level
     */
    public static void info(Logger logger, Component message) {
        logger.info(ANSIComponentSerializer.ansi().serialize(message));
    }

    /**
     * Logs a message at WARN level
     */
    public static void warn(Logger logger, String message) {
        logger.warn(message);
    }

    /**
     * Logs a message at WARN level with parameters
     */
    public static void warn(Logger logger, String message, Object... params) {
        logger.warn(message, params);
    }

    /**
     * Logs a message at WARN level
     */
    public static void warn(Logger logger, Component message) {
        logger.warn(ANSIComponentSerializer.ansi().serialize(message));
    }

    /**
     * Logs a message at ERROR level
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
        logToFileAsync(LogFile.ERROR, message);
    }

    /**
     * Logs a message at ERROR level with parameters
     */
    public static void error(Logger logger, String message, Object... params) {
        logger.error(message, params);
        String formattedMessage = String.format(message.replace("{}", "%s"), params);
        logToFileAsync(LogFile.ERROR, formattedMessage);
    }

    /**
     * Logs a message at ERROR level with exception
     */
    public static void error(Logger logger, String message, Throwable t) {
        logger.error(message, t);
        logToFileAsync(LogFile.ERROR, message);
        logStackTrace(t);
    }

    /**
     * Logs a message at ERROR level
     */
    public static void error(Logger logger, Component message) {
        logger.error(ANSIComponentSerializer.ansi().serialize(message));
        logToFileAsync(LogFile.ERROR, PlainTextComponentSerializer.plainText().serialize(message));
    }

    /**
     * Logs a message at ERROR level with exception
     */
    public static void error(Logger logger, Component message, Throwable t) {
        logger.error(ANSIComponentSerializer.ansi().serialize(message), t);
        logToFileAsync(LogFile.ERROR, PlainTextComponentSerializer.plainText().serialize(message));
        logStackTrace(t);
    }

    // --- Custom Logger Methods ---

    /**
     * Logs a verbose message if verbose mode is enabled.
     */
    public static void verbose(Logger logger, String message) {
        if (verbose) {
            Component prefixedComponent = VERBOSE_PREFIX.append(Component.text(message));
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(LogFile.VERBOSE, PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a verbose message with parameters if verbose mode is enabled.
     */
    public static void verbose(Logger logger, String message, Object... params) {
        if (verbose) {
            String formattedMessage = String.format(message.replace("{}", "%s"), params);
            Component prefixedComponent = VERBOSE_PREFIX.append(Component.text(formattedMessage));
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(LogFile.VERBOSE, PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a verbose message if verbose mode is enabled.
     */
    public static void verbose(Logger logger, Component message) {
        if (verbose) {
            Component prefixedComponent = VERBOSE_PREFIX.append(message);
            logger.info(ANSIComponentSerializer.ansi().serialize(prefixedComponent));
            logToFileAsync(LogFile.VERBOSE, PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
    }

    /**
     * Logs a message asynchronously to a separate file.
     *
     * @param logFileName Path to the log file.
     * @param message     The message to log.
     */
    public static void logToFileAsync(LogFile logFileName, String message) {
        logExecutor.submit(() -> {
            File logFile = new File(logDir, logFileName.getValue());
            createLogFileIfNeeded(logFile);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.write("[" + timestamp + "]: " + message);
                writer.newLine();
            } catch (IOException e) {
                PLUGIN_LOGGER.error("Failed to write to log file: {}", logFileName, e);
            }
        });
    }

    /**
     * Helper method to log stack traces to file
     */
    private static void logStackTrace(Throwable t) {
        logToFileAsync(LogFile.ERROR, t.toString());
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            logToFileAsync(LogFile.ERROR, "\tat " + stackTraceElement);
        }
    }

    /**
     * Ensures that the log file exists.
     *
     * @param logFile The file to check or create.
     */
    private static void createLogFileIfNeeded(File logFile) {
        if (!logFile.exists()) {
            PLUGIN_LOGGER.warn("Log file '{}' doesn't exist yet!", logFile);
            PLUGIN_LOGGER.info("Creating new file...");

            try {
                if (logFile.getParentFile().mkdirs())
                    PLUGIN_LOGGER.info("New log directory created.");

                if (logFile.createNewFile())
                    PLUGIN_LOGGER.info("New log file created at: {}", logFile.getPath());

            } catch (IOException e) {
                PLUGIN_LOGGER.error("Failed to create log file at: {}", logFile.getPath(), e);
            }
        }
    }

    /**
     * Determines the appropriate log file name based on verbosity.
     *
     * @return the log file name
     */
    private static LogFile getLogFileName() {
        return verbose ? LogFile.VERBOSE : LogFile.DEBUG;
    }

    // --- Getter and Setter ---

    /**
     * Sets the verbose flag.
     *
     * @param verbose true to enable verbose logging, false to disable
     */
    public static void setVerbose(boolean verbose) {
        LogUtils.verbose = verbose;
    }

    /**
     * Sets the debug flag.
     *
     * @param debug true to enable debug logging, false to disable
     */
    public static void setDebug(boolean debug) {
        LogUtils.debug = debug;
    }

    /**
     * Returns a logger for the specified class, optionally with a prefix if verbose mode is enabled.
     *
     * @param clazz the class for which the logger is requested
     * @return a logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        if (verbose && !PLUGIN_PREFIX.equals(clazz.getSimpleName())) {
            return LogManager.getLogger(PLUGIN_PREFIX + "->" + clazz.getSimpleName());
        } else {
            return PLUGIN_LOGGER;
        }
    }
}