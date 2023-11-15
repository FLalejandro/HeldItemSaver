package HeldItemSaver.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    // Logger instance specific for the HeldItemSaver mod.
    private static final Logger LOGGER = LoggerFactory.getLogger("HeldItemSaver");

    // Instance of ModConfig to manage configuration settings.
    private static ModConfig config = ConfigHandler.loadConfig();

    /**
     * Logs a debug message.
     * Only logs the message if the log setting is enabled in the mod configuration.
     *
     * @param format The message format.
     * @param arguments Arguments referenced by the format specifiers in the format string.
     */
    public static void debug(String format, Object... arguments) {
        if (config.areLogsEnabled()) {
            LOGGER.debug(format, arguments);
        }
    }

    /**
     * Logs an info level message.
     * Only logs the message if the log setting is enabled in the mod configuration.
     *
     * @param format The message format.
     * @param arguments Arguments referenced by the format specifiers in the format string.
     */
    public static void info(String format, Object... arguments) {
        if (config.areLogsEnabled()) {
            LOGGER.info(format, arguments);
        }
    }

    /**
     * Logs a warning message.
     * Only logs the message if the log setting is enabled in the mod configuration.
     *
     * @param format The message format.
     * @param arguments Arguments referenced by the format specifiers in the format string.
     */
    public static void warn(String format, Object... arguments) {
        if (config.areLogsEnabled()) {
            LOGGER.warn(format, arguments);
        }
    }

    /**
     * Logs an error message.
     * Only logs the message if the log setting is enabled in the mod configuration.
     *
     * @param format The message format.
     * @param arguments Arguments referenced by the format specifiers in the format string.
     */
    public static void error(String format, Object... arguments) {
        if (config.areLogsEnabled()) {
            LOGGER.error(format, arguments);
        }
    }

    /**
     * Reloads the configuration settings for the mod.
     */
    public static void reloadConfig() {
        config = ConfigHandler.loadConfig();
    }
}
