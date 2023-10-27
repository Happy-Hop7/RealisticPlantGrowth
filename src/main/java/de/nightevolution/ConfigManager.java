package de.nightevolution;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;


public class ConfigManager {
    private static YamlDocument config;
    private static boolean debug;
    private static RealisticPlantGrowth instance;
    private static String pluginPrefix = "[RealisticPlantGrowth] ";
    private static String classPrefix = pluginPrefix + "ConfigManager: ";
    private static ConfigManager configManager;

    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Calls RealisticPlantGrowth for Plugin prefix.
     * Get an instance of ConfigManager with ConfigManager.get();.
     * ConfigManager uses BoostedYAMAL API in order to perform file operations.
     */
    private ConfigManager(){
        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        registerYamlConfig();

        debug = getDebugFromConfig();
        pluginPrefix = readPluginPrefixFromConfig();

    }

    /**
     * Get a Singleton instance from ConfigManager.
     * Creates a new instance if no instance already exists.
     * @return ConfigManager Singleton instance
     */
    public static ConfigManager get(){
        if(configManager == null)
            new ConfigManager();
        return configManager;
    }


    /**
     * Registers the config Files for RealisticPlantGrowth Plugin.
     * Creates new one, if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerYamlConfig(){
        try{
            config = YamlDocument.create(new File(instance.getDataFolder(), "Config.yml"), instance.getResource("Config.yml"));
            config.update();
            Bukkit.getLogger().info("[RealisticPlantGrowth] Configuration loaded.");
        }catch (IOException e){
            Bukkit.getLogger().warning("[RealisticPlantGrowth] " + Color.RED + "ConfigManager: Couldn't load YAML configuration!");
            Bukkit.getLogger().warning("[RealisticPlantGrowth] ConfigManager: Configuration could not be loaded.");
            instance.disablePlugin();
        }
    }

    /**
     * Reads the debug boolean from the config File.
     * @return TRUE, if debug is activated. FALSE otherwise.
     */
    private boolean getDebugFromConfig(){
        try {
            // Get Debug-Mode from Config.yml

            return config.getBoolean("debug");

        }catch (YAMLException e){
            Bukkit.getLogger().warning("[RealisticPlantGrowth] ConfigManager: Debug-mode could not be read from config!");
            Bukkit.getLogger().warning(e.getLocalizedMessage());
        }
        return false;
    }


    private String readPluginPrefixFromConfig(){
        String formatted = dissolveColorCodes(config.getString("plugin-prefix"));
        if(debug)
            Bukkit.getLogger().info(classPrefix + "Plugin-Prefix from config: " + formatted);
        return formatted;
    }

    /**
     * This method is executed when Plugin is reloading.
     * Reads all values from config File and updates global Fields.
     */
    public void reloadConfig() {
        Bukkit.getLogger().warning("ConfigManager: Reloading config file...");
        // TODO: save changes in config first before updating
        try {
            config.reload();
            Bukkit.getLogger().info(pluginPrefix + "Config reloaded.");

            debug = getDebugFromConfig();
            pluginPrefix = readPluginPrefixFromConfig();
            instance.setPluginPrefix(pluginPrefix);

            // todo: Methode zum auslesen wichtiger config Inhalte();
            Bukkit.getLogger().info(pluginPrefix + "Sachen ausgelesen.");

        }catch (YAMLException | IOException e){
            Bukkit.getLogger().warning(classPrefix + e.getLocalizedMessage());
            Bukkit.getLogger().warning(classPrefix + "Error while reloading config file.");
            instance.disablePlugin();
        }
    }

    /**
     * Dissolves all Minecraft color codes in a given Sting.
     * Uses Bukkit's color codes.
     * @param stringWithColorCodes String with Minecraft color codes.
     * @return Formatted and colored String.
     */
    public String dissolveColorCodes(String stringWithColorCodes){
        Bukkit.getLogger().info(pluginPrefix + "StringWithColorCodes: " + stringWithColorCodes);
        return ChatColor.translateAlternateColorCodes('&', stringWithColorCodes);
    }

    // Getters for config values
    public String getPluginPrefix(){
        return pluginPrefix;
    }
    public boolean getDebug(){
        return debug;
    }


}
