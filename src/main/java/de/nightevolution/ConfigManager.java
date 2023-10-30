package de.nightevolution;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;


public class ConfigManager {
    // Main config file
    private static YamlDocument config;

    // locals containing plugin messages
    private static YamlDocument defaultLocale_en_US;
    private static YamlDocument selectedLocale;

    // All predefined localizations
    String[] localsArray = {"en_US", "de_DE"};

    private static File pluginFolder;
    private static File localsFolder;

    private static String pluginPrefix = "[RealisticPlantGrowth] ";
    private static String classPrefix = pluginPrefix + "ConfigManager: ";

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static MessageManager messageManager;

    // Different debug and logging modes
    private static boolean verboseMode = true;
    private static boolean debug_log;
    private static boolean tree_log;
    private static boolean plant_log;
    private static boolean bonemeal_log;
    private static boolean log_coords;

    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Calls RealisticPlantGrowth for Plugin prefix.
     * Get an instance of ConfigManager with ConfigManager.get();.
     * ConfigManager uses BoostedYAMAL API in order to perform file operations.
     */
    private ConfigManager(){
        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        pluginFolder = instance.getDataFolder();
        localsFolder = new File(pluginFolder + File.separator + "lang");

        if(!localsFolder.exists()){
            if(verboseMode){
                Bukkit.getLogger().info(classPrefix + "Language directory doesn't exist!");
                Bukkit.getLogger().info(classPrefix + "Creating new directory...");
            }
            try {
                if(localsFolder.mkdir()){
                    Bukkit.getLogger().info(pluginPrefix + "New language directory created.");
                }


            }catch (SecurityException e){
                Bukkit.getLogger().warning(classPrefix + "Couldn't create language directory!");
                instance.disablePlugin();
            }
        }

        registerYamlConfig();
        registerLocals();

        getConfigData();

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
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"), instance.getResource("Config.yml"));
            config.update();
            Bukkit.getLogger().info(pluginPrefix + "Configuration loaded.");
        }catch (IOException e){
            Bukkit.getLogger().warning(classPrefix + "Couldn't load YAML configuration!");
            Bukkit.getLogger().warning(classPrefix + "Configuration could not be loaded.");
            instance.disablePlugin();
        }
    }

    /**
     * Registers the config Files for RealisticPlantGrowth Plugin.
     * Creates new one, if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerLocals(){
        try{
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"), instance.getResource("Config.yml"));
            config.update();
            Bukkit.getLogger().info(pluginPrefix + "Configuration loaded.");
        }catch (IOException e){
            Bukkit.getLogger().warning(classPrefix + "Couldn't load YAML configuration!");
            Bukkit.getLogger().warning(classPrefix + "Configuration could not be loaded.");
            instance.disablePlugin();
        }
    }

    /**
     * Reads the debug boolean from the config file.
     * @return TRUE, if debug is activated. FALSE otherwise.
     * Todo: Add all config parameters
     * Todo: Make this call asynchronous
     */
    private void getConfigData(){
        try {
            // Get diffrent debugging and logging modes from Config.yml
            verboseMode = config.getBoolean("verbose");
            debug_log = config.getBoolean("debug_log");;
            tree_log = config.getBoolean("tree_log");
            plant_log = config.getBoolean("plant_log");
            bonemeal_log = config.getBoolean("bonemeal_log");
            log_coords = config.getBoolean("log_coords");

        }catch (YAMLException e){
            Bukkit.getLogger().warning(classPrefix + "Verbose-mode could not be read from config!");
            Bukkit.getLogger().warning(e.getLocalizedMessage());
        }
    }

    /**
     * Reads plugin-prefix String from the config file.
     * This Method calls the MessageManager and dissolves any miniMessage Tags the String is containing.
     * @return A formatted String with the plugin prefix shown in console.
     */
    private String readPluginPrefixFromConfig(){
        String formatted = instance.getMessageManager().parseMessage(config.getString("plugin-prefix")).toString();
        if(verboseMode)
            Bukkit.getLogger().info(classPrefix + "Plugin-Prefix from config: " + formatted);
        return formatted;
    }

    /**
     * This method is executed when Plugin is reloading.
     * Reads all values from config File and updates global Fields.
     */
    public void reloadConfig() {
        Bukkit.getLogger().warning("ConfigManager: Reloading config file...");
        try {
            config.save();
            config.reload();
            Bukkit.getLogger().info(pluginPrefix + "Config reloaded.");

            verboseMode = getConfigData();
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


    // Getters for config values
    public String getPluginPrefix(){
        return pluginPrefix;
    }
    public boolean getDebug(){
        return verboseMode;
    }


}
