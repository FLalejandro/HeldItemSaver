package me.novoro.helditemsaver.config;

import me.novoro.helditemsaver.api.configuration.Configuration;
import me.novoro.helditemsaver.api.configuration.VersionedConfig;

public class SettingsManager extends VersionedConfig {
    private static boolean areLogsEnabled;

    @Override
    protected void reload(Configuration settingsConfig){
        super.reload(settingsConfig);
        SettingsManager.areLogsEnabled = settingsConfig.getBoolean("logging");
    }

    public static boolean areLogsEnabled() {
        return SettingsManager.areLogsEnabled();
    }

    @Override
    public double getCurrentConfigVersion() {
        return 2.0;
    }

    @Override
    protected String getConfigFileName() {
        return "settings.yml";
    }
}
