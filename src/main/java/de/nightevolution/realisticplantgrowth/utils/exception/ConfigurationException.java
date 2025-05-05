package de.nightevolution.realisticplantgrowth.utils.exception;

/**
 * Thrown to indicate that a configuration-related error has occurred.
 * <p>
 * This exception is typically thrown in two scenarios:
 * <ul>
 *     <li>When the application fails to initialize or set up configuration data (e.g., directory creation, file access).</li>
 *     <li>When a configuration file (such as a YAML file) contains invalid syntax or cannot be parsed correctly.</li>
 * </ul>
 * <p>
 * This is a runtime exception and is meant to signal non-recoverable configuration issues during application startup or runtime loading.
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of this exception (typically another exception such as IOException or YAMLException)
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
