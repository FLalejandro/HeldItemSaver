package me.novoro.helditemsaver.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import me.novoro.helditemsaver.config.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * HeldItemSaver's Logger. It's not recommended to use this externally.
 */
public class HeldItemSaverLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("HeldItemSaver");

    private static boolean enabled() {
        return SettingsManager.areLogsEnabled();
    }

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s) {
        if (!enabled()) return;
        LOGGER.info("[HeldItemSaver] {}", s);
    }
    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s, PokemonBattle battle) {
        if (!enabled()) return;
        HeldItemSaverLogger.LOGGER.info("{}{}{}", "[HeldItemSaver]: ", s, battle);
    }

    /**
     * Sends a warn log to console.
     * @param s The string to log.
     */
    public static void warn(String s) {
        if (!enabled()) return;
        LOGGER.warn("[HeldItemSaver] {}", s);
    }
    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s) {
        if (!enabled()) return;
        LOGGER.error("[HeldItemSaver] {}", s);
    }
    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s, UUID uuid) {
        if (!enabled()) return;
        LOGGER.error("[HeldItemSaver] {} {}", s, uuid);
    }

    /**
     * Prints a stacktrace using HeldItemSaver's Logger.
     * @param throwable The exception to print.
     */
    public static void printStackTrace(Throwable throwable) {
        if (!enabled()) return;
        error(throwable.toString());
        for (StackTraceElement el : throwable.getStackTrace()) {
            error("\tat " + el);
        }
    }

    public static void info(String s, String string, UUID playerUUID) {
        if (!enabled()) return;
        HeldItemSaverLogger.LOGGER.error("{}{}{}{}", "[HeldItemSaver]: ", s, string, playerUUID);
    }
}