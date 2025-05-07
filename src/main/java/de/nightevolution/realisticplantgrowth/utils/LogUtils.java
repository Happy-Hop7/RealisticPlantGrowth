package de.nightevolution.realisticplantgrowth.utils;

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
 */
public class LogUtils {
    /** Default prefix for the plugin log messages */
    private static final String PLUGIN_PREFIX = "RealisticPlantGrowth";

    /** Prefix for debug messages */
    private static final String DEBUG_PREFIX = "DEBUG >> ";

    /** Prefix for verbose messages */
    private static final String VERBOSE_PREFIX = "VERBOSE >> ";

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
    public static void initialize(File pluginDir,boolean debug, boolean verbose) {
        LogUtils.logDir =  new File(pluginDir + File.separator + "log");
        LogUtils.debug = debug;
        LogUtils.verbose = verbose;
    }

    /**
     * Shuts down the logging executor gracefully.
     */
    public static void shutdown() {
        logExecutor.shutdown();
    }

    // --- Logging Methods ---

    /**
     * Logs an info message.
     *
     * @param component the message to log
     */
    public static void info(Logger logger, Component component) {
        logger.info(ANSIComponentSerializer.ansi().serialize(component));
    }

    /**
     * Logs a warning message.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void warn(Logger logger, Component component) {
        logger.warn(ANSIComponentSerializer.ansi().serialize(component));
    }

    /**
     * Logs an error message.
     *
     * @param logger    the logger to use
     * @param message the message to log
     */
    public static void error(Logger logger, Component message) {
        logger.error(ANSIComponentSerializer.ansi().serialize(message));
        logToFileAsync(LogFile.ERROR, PlainTextComponentSerializer.plainText().serialize(message));
    }

    /**
     * Logs an error message.
     *
     * @param logger    the logger to use
     * @param message the message to log
     */
    public static void error(Logger logger, Component message, Throwable t) {
        logger.error(ANSIComponentSerializer.ansi().serialize(message), t);
        logToFileAsync(LogFile.ERROR, PlainTextComponentSerializer.plainText().serialize(message));
        logToFileAsync(LogFile.ERROR, t.toString());
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            logToFileAsync(LogFile.ERROR, "\tat " + stackTraceElement);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message the message string to be logged
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
        logToFileAsync(LogFile.ERROR, message);
    }

    /**
     * Logs an error message.
     *
     * @param message the message string to be logged
     * @param t   the exception to be logged
     */
    public static void error(Logger logger, String message, Throwable t) {
        logger.error(message, t);
        logToFileAsync(LogFile.ERROR, message);
        logToFileAsync(LogFile.ERROR, t.toString());
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            logToFileAsync(LogFile.ERROR, "\tat " + stackTraceElement);
        }
    }

    /**
     * Logs a verbose message if verbose mode is enabled.
     *
     * @param logger    the logger to use
     * @param message the message to log
     */
    public static void verbose(Logger logger, Component message) {
        if (verbose) {
            Component prefixedComponent = applyPrefix(VERBOSE_PREFIX, NamedTextColor.DARK_RED, message);
            info(logger, prefixedComponent);
            logToFileAsync(LogFile.VERBOSE, PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
        // Skip message if verbose is not activated
    }

    /**
     * Logs a verbose message if verbose mode is enabled.
     *
     * @param logger  the logger to use
     * @param message the message string to be logged
     */
    public static void verbose(Logger logger, String message) {
        if (verbose) {
            Component prefixedComponent = applyPrefix(VERBOSE_PREFIX, NamedTextColor.DARK_RED, Component.text(message));
            info(logger, prefixedComponent);
            logToFileAsync(LogFile.VERBOSE, PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
        // Skip message if verbose is not activated
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param logger    the logger to use
     * @param message the message to log
     */
    public static void debug(Logger logger, Component message) {
        if (debug || verbose) {
            Component prefixedComponent = applyPrefix(DEBUG_PREFIX, NamedTextColor.RED, message);
            info(logger, prefixedComponent);
            logToFileAsync(getLogFileName(), PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
        // Skip message if debug is not activated
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param logger  the logger to use
     * @param message the message string to be logged
     */
    public static void debug(Logger logger, String message) {
        if (debug || verbose) {
            Component prefixedComponent = applyPrefix(DEBUG_PREFIX, NamedTextColor.RED, Component.text(message));
            info(logger, prefixedComponent);
            logToFileAsync(getLogFileName(), PlainTextComponentSerializer.plainText().serialize(prefixedComponent));
        }
        // Skip message if debug is not activated
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


    // --- Private Methods ---
    /**
     * Applies a prefix to the given component.
     */
    private static Component applyPrefix(String prefix, NamedTextColor prefixColor ,Component component) {
        return Component.text()
                .append(Component.text(prefix, prefixColor))
                .append(component)
                .build();
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
