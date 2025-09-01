package me.novoro.HeldItemSaver.configs;

/**
 * Configuration class for the me.novoro.HeldItemSaver mod.
 * This class holds various configuration settings that can be altered by the user or the mod itself.
 */
public class ModConfig {

    // Flag to determine if logging is enabled for the mod.
    private boolean logsEnabled = true;

    public boolean areLogsEnabled() {
        return logsEnabled;
    }

    public void setLogsEnabled(boolean logsEnabled) {
        this.logsEnabled = logsEnabled;
    }
}

