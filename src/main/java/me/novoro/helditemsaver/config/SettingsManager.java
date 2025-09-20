package me.novoro.helditemsaver.config;

import me.novoro.helditemsaver.api.configuration.Configuration;
import me.novoro.helditemsaver.api.configuration.VersionedConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.List;

public class SettingsManager extends VersionedConfig {
    private static boolean areLogsEnabled;
    private static List<String> heldItemsBlacklist;


    @Override
    protected void reload(Configuration settingsConfig){
        super.reload(settingsConfig);
        SettingsManager.areLogsEnabled = settingsConfig.getBoolean("Logging");
        SettingsManager.heldItemsBlacklist = settingsConfig.getStringList("Held-Items.Blacklisted-Items");
    }

    public static boolean areLogsEnabled() {
        return SettingsManager.areLogsEnabled();
    }

    public static boolean isHeldItemBlacklisted(ItemStack item){
        return checkBlacklist(item, heldItemsBlacklist);
    }

    private static boolean checkBlacklist(ItemStack item, List<String> blacklist) {
        if (blacklist.isEmpty()) return false;

        final String itemId = Registries.ITEM.getId(item.getItem()).toString();
        CustomModelDataComponent customModelDataComponent = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        int customModelData = customModelDataComponent != null ? customModelDataComponent.value() : 0;

        return blacklist.contains(itemId) || blacklist.contains(itemId + ":" + customModelData);
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
