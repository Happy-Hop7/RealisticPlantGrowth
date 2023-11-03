package de.nightevolution;

import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.YamlDocument;

import org.bukkit.Material;

import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: add config version system
public class ConfigManager {

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static MessageManager messageManager;
    private static Logger logger;

    private static String plugin_prefix;
  

    // Main config
    private static YamlDocument config;

    // Language files containing plugin messages
    private static YamlDocument defaultLanguageFile_en_US;
    private static YamlDocument selectedLanguageFile;

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
    private static ArrayList<Material> uv_blocks = new ArrayList<>();
    private static ArrayList<Material> grow_in_dark = new ArrayList<>();

    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get();.
     * ConfigManager uses BoostedYAML API in order to perform file operations.
     */
    private ConfigManager(){

        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        // TODO: Set verbose and debug values of logger to false
        logger = new Logger(this.getClass().getSimpleName(), instance, true, true);

        logger.verbose("Creating new MessageManager");
        messageManager = new MessageManager(instance, configManager);

        pluginFolder = instance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");

        logger.verbose("Calling registerYamlConfig()");
        registerYamlConfig();

        logger.verbose("Calling getConfigData()");
        getConfigData();

        
        if(!languageFolder.exists()){
            logger.warn("&eLanguage directory doesn't exist!");
            logger.log("Creating new directory...");
            
            try {
                if(languageFolder.mkdir()){
                    logger.log("New language directory created.");

                }


            }catch (SecurityException e){
                logger.error("&cCouldn't create language directory!");
                instance.disablePlugin();
            }
        }else
            logger.verbose("Language directory does exist");

        logger.log("Loading supported languages...");
        logger.verbose("Calling copyDefaultLanguages()");
        copyDefaultLanguages();

        logger.verbose("Calling registerLanguage()");
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
            logger.log("Configuration loaded.");
        }catch (IOException e){
            logger.error("&cCouldn't load YAML configuration!");
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
            selectedLanguageFile.update();
            logger.log("Language files loaded.");
            logger.debug("Selected language: " + language_code);

        }catch (IOException e){
            logger.error("&cCouldn't load YAML configuration!");
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
                    YamlDocument temp = YamlDocument.create(new File(languageFolder, languageCode + ".yml"),
                            instance.getResource(languageCode + ".yml"));
                    temp.update();
                }

                logger.debug(languageCode + ".yml loaded.");

            }
        }catch (IOException e){
            logger.error("&cCouldn't load language files!");
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

            // Get different debugging and logging modes from Config.yml
            verbose = config.getBoolean("verbose");
            logger.log("verbose: " + verbose);
            logger.setVerbose(verbose);

            debug_log = config.getBoolean("debug_log");
            logger.setDebug(debug_log);
            logger.debug("debug_log: " + debug_log);

            logger.debug("");
            logger.debug("-------------------- config data --------------------");
            logger.debug("");

            plugin_prefix = config.getString("plugin_prefix");
            logger.setPluginPrefix(plugin_prefix);
            logger.debug("plugin_prefix: " + plugin_prefix);

            tree_log = config.getBoolean("tree_log");
            logger.debug("tree_log: " + tree_log);

            plant_log = config.getBoolean("plant_log");
            logger.debug("plant_log: " + plant_log);

            bonemeal_log = config.getBoolean("bonemeal_log");
            logger.debug("bonemeal_log: " + bonemeal_log);

            log_coords = config.getBoolean("log_coords");
            logger.debug("log_coords: " + log_coords);

            
            // General settings
            language_code = config.getString("language_code");
            logger.debug("language_code: " + language_code);

            enabled_worlds = config.getStringList("enabled_worlds");
            
            logger.debug("enabled worlds:");
            enabled_worlds.forEach((n) -> {
                logger.debug("  - " + n);
            });
            

            bonemeal_limit = config.getInt("bonemeal_limit");
            logger.debug("bonemeal_limit: " + bonemeal_limit);

            min_natural_light = config.getInt("min_natural_light");
            logger.debug("min_natural_light: " + min_natural_light);

            report_growth = config.getBoolean("report_growth");
            logger.debug("report_growth: " + report_growth);

            
            // Fertilizer settings
            fertilizer_enabled = config.getBoolean("fertilizer_enabled");
            logger.debug("fertilizer_enabled: " + fertilizer_enabled);

            fertilizer_radius = config.getInt("fertilizer_radius");
            logger.debug("fertilizer_radius: " + fertilizer_radius);

            fertilizer_passiv = config.getBoolean("fertilizer_passiv");
            logger.debug("fertilizer_passiv: " + fertilizer_passiv);

            fertilizer_boost_growth_rate = config.getDouble("fertilizer_boost_growth_rate");
            logger.debug("fertilizer_boost_growth_rate: " + fertilizer_boost_growth_rate);

            fertilizer_allow_growth_rate_above_100 = config.getBoolean("fertilizer_allow_growth_rate_above_100");
            logger.debug("fertilizer_allow_growth_rate_above_100: " + fertilizer_allow_growth_rate_above_100);

            // UV-Light settings
            uv_enabled = config.getBoolean("uv_enabled");
            logger.debug("uv_enabled: " + uv_enabled);

            uv_radius = config.getInt("uv_radius");
            logger.debug("uv_radius: " + uv_radius);

            List <String> uv_blocks_string= config.getStringList("uv_blocks");
            logger.debug("uv_blocks:");
            uv_blocks_string.forEach( (materialName) -> {
                uv_blocks.add(Material.getMaterial(materialName));
                logger.debug("  - " + materialName);
            });

            List <String> grow_in_dark_string= config.getStringList("grow_in_dark");
            logger.debug("grow_in_dark:");
            grow_in_dark_string.forEach( (materialName) -> {
                grow_in_dark.add(Material.getMaterial(materialName));
                logger.debug("  - " + materialName);
            });

            logger.debug("");
            logger.debug("-----------------------------------------------------");
            logger.debug("");
            
            
        }catch (YAMLException e){
            logger.error("&cAn Error occurred while reading config.yml data!");
            logger.log(e.getLocalizedMessage());

            instance.disablePlugin();
        }
    }

    /**
     * This method is executed when Plugin is reloading.
     * Reads all values from config File and updates global Fields.
     */
    public void reloadConfig() {
        logger.warn("&eReloading config file...");
        try {
            config.save();
            config.reload();
            logger.log("Config reloaded.");

            // Gets updated config data and stores them as global variables.
            getConfigData();

            //todo: update language file

        }catch (YAMLException | IOException e){
            logger.log(e.getLocalizedMessage());
            logger.error("&cError while reloading config file.");
            instance.disablePlugin();
        }
    }


    // Getters for config values

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

    public String getPluginPrefix() {
        return plugin_prefix;
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
