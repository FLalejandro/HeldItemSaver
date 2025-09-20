package me.novoro.helditemsaver;

import com.mojang.brigadier.CommandDispatcher;
import me.novoro.helditemsaver.api.configuration.Configuration;
import me.novoro.helditemsaver.api.configuration.YamlConfiguration;
import me.novoro.helditemsaver.commands.ReloadCommand;
import me.novoro.helditemsaver.config.LangManager;
import me.novoro.helditemsaver.config.SettingsManager;
import me.novoro.helditemsaver.events.*;
import me.novoro.helditemsaver.util.HeldItemSaverLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


public class HeldItemSaver implements ModInitializer {
    public static final String MOD_PREFIX = "<gray><bold>[<gradient:#FDC830:#F37335>&l[HᴇʟᴅIᴛᴇᴍSᴀᴠᴇʀ</gradient><gray><bold>]&f ";


    private static HeldItemSaver instance;
    public static MinecraftServer server;
    private final SettingsManager settingsManager = new SettingsManager();
    private final LangManager langManager = new LangManager();

    /**
     * Called during the mod initialization phase.
     * Handles registration of commands, event listeners, and other initial setup.
     */
    @Override
    public void onInitialize() {
        HeldItemSaver.instance = this;

        // novoro loves ascii
        this.displayAsciiArt();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.reloadConfigs();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> this.registerCommands(dispatcher));

        // Initialize BattleListener and CatchEventListener
        new BattleListener();
        HeldItemSwapEventHandler.register();
    }

    // Reloads Configs
    public void reloadConfigs() {
        // Settings
        this.settingsManager.reload();
        // Lang
        this.langManager.reload();
    }

    private void displayAsciiArt() {
        HeldItemSaverLogger.info("\u001B[33m  _    _      _     _   _____ _                    _____                      \u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m | |  | |    | |   | | |_   _| |                  / ____|                     \u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m | |__| | ___| | __| |   | | | |_ ___ _ __ ___   | (___   __ ___   _____ _ __ \u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m |  __  |/ _ \\ |/ _` |   | | | __/ _ \\ '_ ` _ \\   \\___ \\ / _` \\ \\ / / _ \\ '__|\u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m | |  | |  __/ | (_| |  _| |_| ||  __/ | | | | |  ____) | (_| |\\ V /  __/ |   \u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m |_|  |_|\\___|_|\\__,_| |_____|\\__\\___|_| |_| |_| |_____/ \\__,_| \\_/ \\___|_|   \u001B[0m");
        HeldItemSaverLogger.info("\u001B[33m By Novoro: https://discord.gg/wzpp8jeJ9s \u001B[0m");
    }

    /**
     * Gets Seam's current instance. It is not recommended to use externally.
     */
    public static HeldItemSaver inst() {
        return HeldItemSaver.instance;
    }

    /**
     * Gets the current {@link MinecraftServer} Seam is currently running on.
     */
    public static MinecraftServer getServer() {
        return HeldItemSaver.instance.server;
    }

    /**
     * Registers the reload command for the mod.
     */
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        ReloadCommand.register(dispatcher);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getDataFolder() {
        File folder = FabricLoader.getInstance().getConfigDir().resolve("HeldItemSaver").toFile();
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getFile(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return file;
    }

    public Configuration getConfig(String fileName, boolean saveResource) {
        File configFile = this.getFile(fileName);
        if (!configFile.exists()) {
            if (!saveResource) return null;
            this.saveResource(fileName, false);
        }
        return this.getConfig(configFile);
    }

    public Configuration getConfig(File configFile) {
        try {
            return YamlConfiguration.loadConfiguration(configFile);
        } catch (IOException e) {
            HeldItemSaverLogger.error("Something went wrong getting the config: " + configFile.getName() + ".");
            HeldItemSaverLogger.printStackTrace(e);
        }
        return null;
    }

    public void saveConfig(String fileName, Configuration config) {
        File file = this.getFile(fileName);
        try {
            YamlConfiguration.save(config, file);
        } catch (IOException e) {
            HeldItemSaverLogger.warn("Something went wrong saving the config: " + fileName + ".");
            HeldItemSaverLogger.printStackTrace(e);
        }
    }

    @SuppressWarnings("resource")
    public void saveResource(String fileName, boolean overwrite) {
        File file = this.getFile(fileName);
        if (file.exists() && !overwrite) return;
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            Path path = Paths.get("configurations", fileName);
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
            assert in != null;
            in.transferTo(outputStream);
        } catch (IOException e) {
            HeldItemSaverLogger.error("Something went wrong saving the resource: " + fileName + ".");
            HeldItemSaverLogger.printStackTrace(e);
        }
    }
}
