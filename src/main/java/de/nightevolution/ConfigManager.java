package de.nightevolution;

import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Material;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//TODO: add config version system
public class ConfigManager {

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static MessageManager messageManager;
    private static Logger logger;

    private static String plugin_prefix;
  

    // Main config
    private static YamlDocument config;
    private static YamlDocument biomeGroupsFile;
    private static YamlDocument growthModificatorsFile;


    // Selected language file containing plugin messages
    private static YamlDocument selectedLanguageFile;

    private static File pluginFolder;
    private static File languageFolder;
    private static File logFolder;


    // All predefined supported localizations.
    private static final List<String> supportedLanguageCodes = (Arrays.asList(
            "de-DE",
            "en-US"
    ));

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
    private static boolean destroy_farmland;
    private static boolean require_hoe;
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
    private static final ArrayList<Material> uv_blocks = new ArrayList<>();
    private static final ArrayList<Material> grow_in_dark = new ArrayList<>();

    // GrowthModificators.yml
    private static Map<String, Object> growthModificatorsData;

    // BiomeGroups.yml
    private static Map<String, Object> biomeGroupsData;

    // Selected language file data
    private static Map<String, Object> languageFileData;


    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get().
     * ConfigManager uses BoostedYAML API to perform file operations.
     */
    private ConfigManager(){

        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        // TODO: Set verbose and debug values of logger to false
        logger = new Logger(this.getClass().getSimpleName(), instance, true, true);

        logger.verbose("Creating new MessageManager");
        messageManager = new MessageManager(instance);

        pluginFolder = instance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");
        logFolder = new File(pluginFolder + File.separator + "log");

        logger.verbose("Calling registerYamlConfigs()");
        registerYamlConfigs();

        logger.verbose("Calling getConfigData()");
        readConfigData();




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
        registerSupportedLanguages();

        logger.verbose("Calling registerLanguage()");
        registerSelectedLanguage();

        logger.verbose("Calling readLanguageData()");
        readLanguageData();

        logger.verbose("Calling readBiomeGroupsData()");
        readBiomeGroupsData();

        logger.verbose("Calling readGrowthModificatorsData()");
        readGrowthModificatorsData();

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
     * Registers all config Files for RealisticPlantGrowth Plugin.
     * Creates new one if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerYamlConfigs(){
        // Main Config
        try{
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"),
                    Objects.requireNonNull(instance.getResource("Config.yml")));
            config.update();
            logger.log("Configuration loaded.");

        }catch (IOException e){
            logger.error("&cCouldn't load YAML configuration!");
            instance.disablePlugin();
        }

        // BiomeGroups Config
        try{
            biomeGroupsFile = YamlDocument.create(new File(pluginFolder, "BiomeGroups.yml"),
                    Objects.requireNonNull(instance.getResource("BiomeGroups.yml")));
            biomeGroupsFile.update();
            logger.log("BiomeGroups loaded.");

        }catch (IOException e){
            logger.error("&cCouldn't load BiomeGroups YAML configuration!");
            instance.disablePlugin();
        }

        // GrowthModificators Config
        try{
            growthModificatorsFile = YamlDocument.create(new File(pluginFolder, "GrowthModificators.yml"),
                    Objects.requireNonNull(instance.getResource("GrowthModificators.yml")));
            growthModificatorsFile.update();
            logger.log("GrowthModificators loaded.");

        }catch (IOException e){
            logger.error("&cCouldn't load GrowthModificators YAML configuration!");
            instance.disablePlugin();
        }

    }

    /**
     * This Method copies default language files into the "lang" directory during plugin initialization.
     * This method is executed only once, at the start of the plugin and during reloads.
     * It supports custom language files in the "lang" directory and selects "en-US" as the default language
     * if the language code specified in the Config.yml cannot be resolved.
     */
    private void registerSupportedLanguages() {

        logger.verbose("Language Folder: " + languageFolder);

        // Copies all supported language files into the lang directory.
        // Selects
        try {
            for (String languageCode : supportedLanguageCodes) {
                logger.verbose("Language: " + languageCode);

                if(languageCode.equalsIgnoreCase(getLanguage_code())){
                    logger.verbose( "Loading selected language File: " + languageFolder + File.separator + languageCode + ".yml");

                    selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")));

                }else {
                    logger.verbose( "Loading language File: " + languageFolder + File.separator + languageCode + ".yml");
                    YamlDocument temp = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")));
                    temp.update();
                }

                logger.debug(languageCode + ".yml loaded.");

            }
        }catch (IOException e){
            logger.error("&cCouldn't load language files!");
            instance.disablePlugin();
            return;
        }

        // Search for custom files in lang directory.
        if (selectedLanguageFile == null){
            // lese daten von ordner
            File[] allFiles = languageFolder.listFiles();

            if (allFiles == null) {
                logger.error("&cCouldn't load language files!");
                instance.disablePlugin();
                return;
            }

            logger.log("Searching for custom language files...");

            for (File allFile : allFiles) {
                if (allFile.isFile()) {
                    String fileName = allFile.getName();
                    if (fileName.equalsIgnoreCase(getLanguage_code())) {
                        try {
                            selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, "en-US.yml"));
                            logger.log(fileName + " loaded.");
                        } catch (IOException e) {
                            logger.warn("Couldn't load language_code: " + getLanguage_code());
                        }

                    }
                }
            }

        }

        // Setting the default language if selected 'language_code' file not found.
        if (selectedLanguageFile == null){
            try {
                logger.warn("No custom language file with language_code '" + getLanguage_code() + "' located in 'lang' directory!");
                logger.warn("Using default language file: en-US");
                selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, "en-US.yml"),
                        Objects.requireNonNull(instance.getResource("lang/" + "en-US.yml")));
            }catch (IOException e){
                logger.error("&cCouldn't load custom language file!");
                instance.disablePlugin();
            }

        }

    }


    /**
     * Registers the config Files for RealisticPlantGrowth Plugin.
     * Creates new one if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerSelectedLanguage(){

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
     * Reads the debug boolean from the config file.
     * Todo: Make this call asynchronous
     * Todo: Add parameter check !!!
     */
    private void readConfigData(){

        try {

            // Get different debugging and logging modes from Config.yml
            verbose = config.getBoolean("verbose");
            logger.log("verbose: " + verbose);
            logger.setVerbose(verbose);

            debug_log = config.getBoolean("debug_log");
            logger.setDebug(debug_log);
            logger.debug("debug_log: " + debug_log);

            logger.debug("");
            logger.debug("-------------------- Config.yml Data --------------------");
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

            destroy_farmland = config.getBoolean("destroy_farmland");
            logger.debug("destroy_farmland: " + destroy_farmland);

            require_hoe = config.getBoolean("require_hoe");
            logger.debug("require_hoe: " + require_hoe);

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
            
            
        }catch (YAMLException e){
            logger.error("&cAn Error occurred while reading config.yml data!");
            logger.log(e.getLocalizedMessage());

            instance.disablePlugin();
        }
    }

    private void readBiomeGroupsData(){
        biomeGroupsData = biomeGroupsFile.getStringRouteMappedValues(true);

        logger.debug("");
        logger.debug("-------------------- BiomeGroups.yml Data --------------------");
        logger.debug("");
        if(verbose)
            printMap(biomeGroupsData);
        // TODO: Check Data

    }

    private void readGrowthModificatorsData(){
        growthModificatorsData = growthModificatorsFile.getStringRouteMappedValues(true);

        logger.debug("");
        logger.debug("-------------------- GrowthModificators.yml Data --------------------");
        logger.debug("");
        if(verbose)
            printMap(growthModificatorsData);
        // TODO: Check Data
    }

    private void readLanguageData(){
        languageFileData = selectedLanguageFile.getStringRouteMappedValues(true);

        logger.debug("");
        logger.debug("-------------------- " + language_code + ".yml Data --------------------");
        logger.debug("");
        if(verbose)
            printMap(languageFileData);
        // TODO: Check Data
    }

    private void printMap(Map<String, Object> data){
        Set<String> keys = data.keySet();
        keys.forEach((key) -> {
            logger.debug(data.get(key).toString());
        });
    }

    /**
     * Reloads configuration files when the plugin is in a reloading state.
     * Reads values from the configuration files and updates global fields accordingly.
     * This method reloads the main configuration file (Config.yml), as well as other YAML files
     * responsible for growth modifiers (GrowthModificators.yml), biome groups (BiomeGroups.yml),
     * and language settings (Language files).
     */
    public void reloadAllYAMLFiles() {
        logger.warn("&eReloading config file...");
        try {
            config.reload();
            logger.verbose("Config.yml reloaded.");

            growthModificatorsFile.reload();
            logger.verbose("GrowthModificators.yml reloaded.");

            biomeGroupsFile.reload();
            logger.verbose("BiomeGroups.yml reloaded.");

            selectedLanguageFile.update();
            logger.verbose("Language files reloaded.");

            // Get updated config data and store new data in global variables.
            readConfigData();
            registerSupportedLanguages();
            readLanguageData();
            readBiomeGroupsData();
            readGrowthModificatorsData();

            logger.log("&2All configuration files reloaded.");


        }catch (YAMLException | IOException e){
            logger.log(e.getLocalizedMessage());
            logger.error("&cError while reloading config files.");
            instance.disablePlugin();
        }
    }


    /**
     * Writes a given String into a .log File.
     * If the file does not exit, this method will create a new one.
     * Uses {@link FileWriter} in order to write into the .log files.
     * @param msg String to write into the file.
     * @param fileName String representing the name of a File.
     */
    public void writeToLogFile(String msg, String fileName){

        if(!logFolder.exists()){
            logger.warn("&eLog directory doesn't exist!");
            logger.log("Creating new directory...");

            try {
                if(logFolder.mkdir()){
                    logger.log("New log directory created.");

                }


            }catch (SecurityException e){
                logger.error("&cCouldn't create log directory!");
                return;
            }
        }else
            logger.verbose("Log directory does exist.");

        try {

            File logFile = new File(logFolder, fileName+".log");
            if (logFile.createNewFile()) {
                logger.log("New log File created: " + logFile.getName());
            }else{
                logger.debug(logFile.getName() + " loaded.");
            }

            FileWriter fw = new FileWriter(logFile, true);
            PrintWriter pw = new PrintWriter(fw);

            pw.println(msg);
            pw.flush();
            pw.close();

        }
        catch (IOException e)        {
            logger.error("An Error occurred while trying to log a message into a log file.");
        }

    }

    // Setters for config values

    public static void setEnabled_worlds(List<String> enabled_worlds) {
        ConfigManager.enabled_worlds = enabled_worlds;
    }

    public static void setBonemeal_limit(int bonemeal_limit) {
        ConfigManager.bonemeal_limit = bonemeal_limit;
    }

    public static void setMin_natural_light(int min_natural_light) {
        ConfigManager.min_natural_light = min_natural_light;
    }

    public static void setReport_growth(boolean report_growth) {
        ConfigManager.report_growth = report_growth;
    }

    public static void setFertilizer_enabled(boolean fertilizer_enabled) {
        ConfigManager.fertilizer_enabled = fertilizer_enabled;
    }

    public static void setFertilizer_radius(int fertilizer_radius) {
        ConfigManager.fertilizer_radius = fertilizer_radius;
    }

    public static void setFertilizer_passiv(boolean fertilizer_passiv) {
        ConfigManager.fertilizer_passiv = fertilizer_passiv;
    }

    public static void setFertilizer_boost_growth_rate(double fertilizer_boost_growth_rate) {
        ConfigManager.fertilizer_boost_growth_rate = fertilizer_boost_growth_rate;
    }

    public static void setFertilizer_allow_growth_rate_above_100(boolean fertilizer_allow_growth_rate_above_100) {
        ConfigManager.fertilizer_allow_growth_rate_above_100 = fertilizer_allow_growth_rate_above_100;
    }

    public static void setUv_enabled(boolean uv_enabled) {
        ConfigManager.uv_enabled = uv_enabled;
    }

    public static void setUv_radius(int uv_radius) {
        ConfigManager.uv_radius = uv_radius;
    }





    // Getters for config values
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

    public boolean isDestroy_farmland(){
        return destroy_farmland;
    }

    public boolean isRequire_hoe() {
        return require_hoe;
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

    public Map<String, Object> getBiomeGroups(){
        return biomeGroupsData;
    }

    public Map<String, Object> getGrowthModificators(){
        return growthModificatorsData;
    }

    public Map<String, Object> getLanguageFileData(){
        return languageFileData;
    }

}
