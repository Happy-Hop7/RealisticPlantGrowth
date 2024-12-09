package de.nightevolution.realisticplantgrowth.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Utility class for handling logging with different levels of verbosity and debug information.
 */
public class LogUtils {
    // Flag for verbose logging
    private static boolean verbose;

    // Flag for debug logging
    private static boolean debug;

    // Default prefix for the plugin log messages
    private static final String pluginPrefix = "RealisticPlantGrowth";

    // Logger instance for the plugin
    private static final org.slf4j.Logger pluginLogger = LoggerFactory.getLogger(pluginPrefix);

    // Prefix for debug messages
    private static final String DEBUG = "DEBUG >> ";

    // Prefix for verbose messages
    private static final String VERBOSE = "VERBOSE >> ";

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
    public static org.slf4j.Logger getLogger(Class<?> clazz) {
        if (verbose && !pluginPrefix.equals(clazz.getSimpleName())) {
            return LoggerFactory.getLogger(pluginPrefix + "->" + clazz.getSimpleName());
        } else {
            return pluginLogger;
        }
    }

    /**
     * Logs an info message.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void info(org.slf4j.Logger logger, Component component) {
        logger.info(ANSIComponentSerializer.ansi().serialize(component));
    }

    /**
     * Logs a warning message.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void warn(org.slf4j.Logger logger, Component component) {
        logger.warn(ANSIComponentSerializer.ansi().serialize(component));
    }

    /**
     * Logs an error message.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void error(org.slf4j.Logger logger, Component component) {
        logger.error(ANSIComponentSerializer.ansi().serialize(component));
        // TODO: Additionally log errors to a separate file
    }

    /**
     * Logs an error message with a marker.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @param t      the exception to be logged
     */
    public static void error(Marker marker, String msg, Throwable t) {
        // TODO: Additionally log errors to a separate file
    }

    /**
     * Logs an error message.
     *
     * @param msg the message string to be logged
     * @param t   the exception to be logged
     */
    public static void error(String msg, Throwable t) {
        // TODO: Additionally log errors to a separate file
    }

    /**
     * Logs a verbose message if verbose mode is enabled.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void verbose(org.slf4j.Logger logger, Component component) {
        if (verbose) {
            Component newComponent = Component.text()
                    .append(Component.text(VERBOSE, NamedTextColor.DARK_RED))
                    .append(component)
                    .build();
            info(logger, newComponent);
        }
        // Skip message if verbose is not activated
    }

    /**
     * Logs a verbose message if verbose mode is enabled.
     *
     * @param logger  the logger to use
     * @param message the message string to be logged
     */
    public static void verbose(org.slf4j.Logger logger, String message) {
        if (verbose) {
            Component newComponent = Component.text()
                    .append(Component.text(VERBOSE, NamedTextColor.DARK_RED))
                    .append(Component.text(message))
                    .build();
            info(logger, newComponent);
        }
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param logger    the logger to use
     * @param component the message to log
     */
    public static void debug(org.slf4j.Logger logger, Component component) {
        if (debug) {
            Component newComponent = Component.text()
                    .append(Component.text(DEBUG, NamedTextColor.RED))
                    .append(component)
                    .build();
            info(logger, newComponent);
        }
        // Skip message if debug is not activated
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param logger  the logger to use
     * @param message the message string to be logged
     */
    public static void debug(org.slf4j.Logger logger, String message) {
        if (debug) {
            Component newComponent = Component.text()
                    .append(Component.text(DEBUG, NamedTextColor.RED))
                    .append(Component.text(message))
                    .build();
            info(logger, newComponent);
        }
    }
}
