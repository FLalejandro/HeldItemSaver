package HeldItemSaver.configs;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigHandler {
    private static final String CONFIG_FILE_NAME = "helditemsaver.json";
    // Resolve the configuration path to be within the "config" directory
    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    private static ModConfig config;

    public static ModConfig loadConfig() {
        Gson gson = new Gson();
        try {
            // Check if the config file exists
            if (Files.notExists(configPath)) {
                // Create the config directory and file with default settings
                Files.createDirectories(configPath.getParent());
                saveDefaultConfig(gson);
            }
            // Read the config file
            try (FileReader reader = new FileReader(configPath.toFile())) {
                config = gson.fromJson(reader, ModConfig.class);
                // If the file is empty or has different structure, load defaults
                if (config == null) {
                    config = new ModConfig();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Load default in case of any IO errors
            config = new ModConfig();
        }
        System.out.println("Configuration loaded: logsEnabled = " + config.areLogsEnabled());
        return config;
    }

    private static void saveDefaultConfig(Gson gson) {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            gson.toJson(new ModConfig(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
