package me.novoro.helditemsaver.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * HeldItemSaver's Logger. It's not recommended to use this externally.
 */
public class HeldItemSaverLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("HeldItemSaver");

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s) {
        HeldItemSaverLogger.LOGGER.info("{}{}", "[HeldItemSaver]: ", s);
    }

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s, PokemonBattle battle) {
        HeldItemSaverLogger.LOGGER.info("{}{}", "[HeldItemSaver]: ", s, battle);
    }

    /**
     * Sends a warn log to console.
     * @param s The string to log.
     */
    public static void warn(String s) {
        HeldItemSaverLogger.LOGGER.warn("{}{}", "[HeldItemSaver]: ", s);
    }

    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s) {
        HeldItemSaverLogger.LOGGER.error("{}{}", "[HeldItemSaver]: ", s);
    }

    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s, UUID uuid) {
        HeldItemSaverLogger.LOGGER.error("{}{}", "[HeldItemSaver]: ", s, uuid);
    }

    /**
     * Prints a stacktrace using HeldItemSaver's Logger.
     * @param throwable The exception to print.
     */
    public static void printStackTrace(Throwable throwable) {
        HeldItemSaverLogger.error(throwable.toString());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace) HeldItemSaverLogger.error("\tat " + traceElement);
    }
}