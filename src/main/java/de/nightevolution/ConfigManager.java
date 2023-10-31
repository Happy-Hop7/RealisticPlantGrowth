package de.nightevolution;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//TODO: add config version system
public class ConfigManager {

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static MessageManager messageManager;
    
    private static Logger logger = Bukkit.getLogger();

    // Main config
    private static YamlDocument config;

    // Language files containing plugin messages
    private static YamlDocument defaultLanguageFile_en_US;
    private static YamlDocument selectedLanguageFile;


    private static String pluginPrefix = "[RealisticPlantGrowth] ";
    private static String classPrefix = pluginPrefix + "ConfigManager: ";

    private static File pluginFolder;
    private static File languageFolder;


    // All predefined localizations
    String[] localsArray = {"de-DE", "en-US"};
    private static String language_code;

    // Different debug and logging modes
    private static boolean verbose = true;
    private static boolean debug_log;
    private static boolean tree_log;
    private static boolean plant_log;
    private static boolean bonemeal_log;
    private static boolean log_coords;

    // Enabled worlds
    private static List<String> enabled_worlds;

    // More config values
    private static int bonemeal_limit;
    private static int min_natural_light;
    private static boolean report_growth;

    // Fertilizer config values
    private static boolean fertilizer_enabled;
    private static int fertilizer_radius;
    private static boolean fertilizer_passiv;
    private static double fertilizer_boost_growth_rate;
    private static boolean fertilizer_allow_growth_rate_above_100;

    // UV-Light config values
    private static boolean uv_enabled;
    private static int uv_radius;
    private static ArrayList<Material> uv_blocks;
    private static ArrayList<Material> grow_in_dark;

    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get();.
     * ConfigManager uses BoostedYAML API in order to perform file operations.
     */
    private ConfigManager(){
        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        pluginFolder = instance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");

        registerYamlConfig();
        getConfigData();

        
        if(!languageFolder.exists()){
            if(verbose){
                logger.info(classPrefix + "Language directory doesn't exist!");
                logger.info(classPrefix + "Creating new directory...");
            }
            try {
                if(languageFolder.mkdir()){
                    logger.info(pluginPrefix + "New language directory created.");
                    logger.info(pluginPrefix + "Loading supported languages...");
                    copyDefaultLanguages();
                }


            }catch (SecurityException e){
                logger.warning(classPrefix + "Couldn't create language directory!");
                instance.disablePlugin();
            }
        }
        
        registerLanguage();
        
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
            logger.info(pluginPrefix + "Configuration loaded.");
        }catch (IOException e){
            logger.warning(classPrefix + "Couldn't load YAML configuration!");
            instance.disablePlugin();
        }
    }

    /**
     * Registers the config Files for RealisticPlantGrowth Plugin.
     * Creates new one, if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerLanguage(){

        try{
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"),
                    instance.getResource("Config.yml"));
            config.update();
            logger.info(pluginPrefix + "Configuration loaded.");
        }catch (IOException e){
            logger.warning(classPrefix + "Couldn't load YAML configuration!");
            instance.disablePlugin();
        }
    }

    /**
     * Copies all default language files into the "lang" directory.
     * Gets executed only at first plugin start.
     */
    private void copyDefaultLanguages(){

        try {
            for (String languageCode : localsArray) {

                if(languageCode.equalsIgnoreCase(getLanguage_code())){
                    selectedLanguageFile = YamlDocument.create(new File(languageFolder, languageCode + ".yml"),
                            instance.getResource(languageCode + ".yml"));

                }else {
                    YamlDocument.create(new File(languageFolder, languageCode + ".yml"),
                            instance.getResource(languageCode + ".yml"));
                }

                logger.info(pluginPrefix + languageCode + ".yml loaded.");

            }
        }catch (IOException e){
            logger.warning(classPrefix + "Couldn't load language files!");
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
            verbose = config.getBoolean("verbose");
            if(verbose) logger.info(classPrefix + "verbose: true");

            pluginPrefix = readPluginPrefixFromConfig();

            debug_log = config.getBoolean("debug_log");
            if(verbose) logger.info(classPrefix + "debug_log: " + debug_log);

            tree_log = config.getBoolean("tree_log");
            if(verbose) logger.info(classPrefix + "tree_log: " + tree_log);

            plant_log = config.getBoolean("plant_log");
            if(verbose) logger.info(classPrefix + "plant_log: " + plant_log);

            bonemeal_log = config.getBoolean("bonemeal_log");
            if(verbose) logger.info(classPrefix + "bonemeal_log: " + bonemeal_log);

            log_coords = config.getBoolean("log_coords");
            if(verbose) logger.info(classPrefix + "log_coords: " + log_coords);

            
            // General settings
            language_code = config.getString("language_code");
            if(verbose) logger.info(classPrefix + "language_code: " + language_code);

            enabled_worlds = config.getStringList("enabled_worlds");
            if(verbose) {
                logger.info(classPrefix + "enabled worlds:");
                enabled_worlds.forEach((n) -> {
                    logger.info(classPrefix + "- " + n);
                });
            }

            bonemeal_limit = config.getInt("bonemeal_limit");
            if(verbose) logger.info(classPrefix + "bonemeal_limit: " + bonemeal_limit);

            min_natural_light = config.getInt("min_natural_light");
            if(verbose) logger.info(classPrefix + "min_natural_light: " + min_natural_light);

            report_growth = config.getBoolean("report_growth");
            if(verbose) logger.info(classPrefix + "report_growth: " + report_growth);

            
            // Fertilizer settings
            fertilizer_enabled = config.getBoolean("fertilizer_enabled");
            if(verbose) logger.info(classPrefix + "fertilizer_enabled: " + fertilizer_enabled);

            fertilizer_radius = config.getInt("fertilizer_radius");
            if(verbose) logger.info(classPrefix + "fertilizer_radius: " + fertilizer_radius);

            fertilizer_passiv = config.getBoolean("fertilizer_passiv");
            if(verbose) logger.info(classPrefix + "fertilizer_passiv: " + fertilizer_passiv);

            fertilizer_boost_growth_rate = config.getDouble("fertilizer_boost_growth_rate");
            if(verbose) logger.info(classPrefix + "fertilizer_boost_growth_rate: " + fertilizer_boost_growth_rate);

            fertilizer_allow_growth_rate_above_100 = config.getBoolean("fertilizer_allow_growth_rate_above_100");
            if(verbose) logger.info(classPrefix + "fertilizer_allow_growth_rate_above_100: " + fertilizer_allow_growth_rate_above_100);

            // UV-Light settings
            uv_enabled = config.getBoolean("uv_enabled");
            if(verbose) logger.info(classPrefix + "uv_enabled: " + uv_enabled);

            uv_radius = config.getInt("uv_radius");
            if(verbose) logger.info(classPrefix + "uv_radius: " + uv_radius);

            List <String> uv_blocks_string= config.getStringList("uv_blocks");
            if(verbose) logger.info(classPrefix + "uv_blocks: " + uv_blocks);
            uv_blocks_string.forEach( (materialName) -> {
                uv_blocks.add(Material.getMaterial(materialName));
                if(verbose) logger.info(classPrefix + "- " + materialName);
            });

            List <String> grow_in_dark_string= config.getStringList("grow_in_dark");
            if(verbose) logger.info(classPrefix + "grow_in_dark: " + grow_in_dark);
            grow_in_dark_string.forEach( (materialName) -> {
                grow_in_dark.add(Material.getMaterial(materialName));
                if(verbose) logger.info(classPrefix + "- " + materialName);
            });


        }catch (YAMLException e){
            logger.warning(classPrefix + "An Error occurred while reading config.yml data!");
            logger.warning(e.getLocalizedMessage());

            instance.disablePlugin();
        }
    }

    /**
     * Reads plugin_prefix String from the main config file.
     * This Method calls the MessageManager and dissolves any miniMessage Tags the String is containing.
     * @return A formatted String with the plugin prefix shown in console.
     */
    private String readPluginPrefixFromConfig(){
        String formatted = instance.getMessageManager().parseMessage(config.getString("plugin_prefix")).toString();
        if(verbose)
            logger.info(classPrefix + "plugin_prefix: " + formatted);
        return formatted;
    }


    /**
     * This method is executed when Plugin is reloading.
     * Reads all values from config File and updates global Fields.
     */
    public void reloadConfig() {
        logger.warning(pluginPrefix + "Reloading config file...");
        try {
            config.save();
            config.reload();
            logger.info(pluginPrefix + "Config reloaded.");

            // Gets updated config data and stores them as global variables.
            getConfigData();

            //todo: update language file

        }catch (YAMLException | IOException e){
            logger.warning(classPrefix + e.getLocalizedMessage());
            logger.warning(classPrefix + "Error while reloading config file.");
            instance.disablePlugin();
        }
    }


    // Getters for config values
    public static String getPluginPrefix() {
        return pluginPrefix;
    }

    public static String getClassPrefix() {
        return classPrefix;
    }

    public static File getPluginFolder() {
        return pluginFolder;
    }

    public static File getLanguageFolder() {
        return languageFolder;
    }

    public String[] getLocalsArray() {
        return localsArray;
    }

    public String getLanguage_code() {
        return language_code;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug_log() {
        return debug_log;
    }

    public boolean isTree_log() {
        return tree_log;
    }

    public boolean isPlant_log() {
        return plant_log;
    }

    public boolean isBonemeal_log() {
        return bonemeal_log;
    }

    public boolean isLog_coords() {
        return log_coords;
    }

    public List<String> getEnabled_worlds() {
        return enabled_worlds;
    }

    public int getBonemeal_limit() {
        return bonemeal_limit;
    }

    public int getMin_natural_light() {
        return min_natural_light;
    }

    public boolean isReport_growth() {
        return report_growth;
    }

    public boolean isFertilizer_enabled() {
        return fertilizer_enabled;
    }

    public int getFertilizer_radius() {
        return fertilizer_radius;
    }

    public boolean isFertilizer_passiv() {
        return fertilizer_passiv;
    }

    public double getFertilizer_boost_growth_rate() {
        return fertilizer_boost_growth_rate;
    }

    public boolean isFertilizer_allow_growth_rate_above_100() {
        return fertilizer_allow_growth_rate_above_100;
    }

    public boolean isUv_enabled() {
        return uv_enabled;
    }

    public int getUv_radius() {
        return uv_radius;
    }

    public ArrayList<Material> getUv_blocks() {
        return uv_blocks;
    }

    public ArrayList<Material> getGrow_in_dark() {
        return grow_in_dark;
    }


}
