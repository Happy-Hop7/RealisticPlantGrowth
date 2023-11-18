package de.nightevolution;

import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.*;

//TODO: add config version system
public class ConfigManager {

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static MessageManager messageManager;
    private static Logger logger;

    private static String plugin_prefix;
    private static final String logFile = "debug";

    // Main config
    private static YamlDocument config;

    private static YamlDocument biomeGroupsFile;
    private static Map<String, Object> biomeGroupsData;

    private static YamlDocument growthModificatorsFile;
    private static Map<String, Object> growthModificatorsData;

    // Selected language file containing plugin messages
    private static YamlDocument selectedLanguageFile;
    private static Map<String, Object> languageFileData;

    private static File pluginFolder;
    private static File languageFolder;


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
    private static boolean require_all_uv_blocks;
    private static final ArrayList<Material> uv_blocks = new ArrayList<>();
    private static final ArrayList<Material> grow_in_dark = new ArrayList<>();


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

        pluginFolder = instance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");

        
        registerYamlConfigs();
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
            logger.logToFile("Language directory already exist.", logFile);

        logger.log("Loading supported languages...");
        registerSupportedLanguages();
        registerSelectedLanguage();
        readLanguageData();
        
        logger.log("Loading BiomeGroups data...");
        readBiomeGroupsData();
        
        logger.log("Loading GrowthModificators data...");
        readGrowthModificatorsData();

    }

    /**
     * Get a Singleton instance from ConfigManager.
     * Creates a new instance if no instance already exists.
     * @return ConfigManager Singleton instance
     */
    protected static ConfigManager get(){
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
        // -> should use default values if something is missing.
        try{
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"),
                    Objects.requireNonNull(instance.getResource("Config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            config.update();
            logger.log("Configuration loaded.");

        }catch (IOException e){
            logger.error("&cCouldn't load YAML configuration!");
            instance.disablePlugin();
        }

        // BiomeGroups Config
        // don't use defaults here
        GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
        try{
            biomeGroupsFile = YamlDocument.create(new File(pluginFolder, "BiomeGroups.yml"),
                    Objects.requireNonNull(instance.getResource("BiomeGroups.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            //biomeGroupsFile.update();
            logger.log("BiomeGroups loaded.");

        }catch (IOException e){
            logger.error("&cCouldn't load BiomeGroups YAML configuration!");
            instance.disablePlugin();
        }

        // GrowthModificators Config
        try{
            growthModificatorsFile = YamlDocument.create(new File(pluginFolder, "GrowthModificators.yml"),
                    Objects.requireNonNull(instance.getResource("GrowthModificators.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            //growthModificatorsFile.update();
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

        logger.logToFile("Language Folder: " + languageFolder, logFile);

        // Copies all supported language files into the lang directory.
        try {
            for (String languageCode : supportedLanguageCodes) {
                logger.logToFile("Language: " + languageCode, logFile);

                if(languageCode.equalsIgnoreCase(getLanguage_code())){
                    logger.logToFile( "Loading selected language File: " + languageFolder + File.separator + languageCode + ".yml", logFile);

                    selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

                }else {
                    logger.logToFile( "Loading language File: " + languageFolder + File.separator + languageCode + ".yml", logFile);
                    YamlDocument temp = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
                    temp.update();
                }

                logger.logToFile(languageCode + ".yml loaded.", logFile);

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

            GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
            logger.log("Searching for custom language files...");
            for (File file : allFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.equalsIgnoreCase(language_code + ".yml")) {
                        try {
                            selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, language_code + ".yml"),
                                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
                            logger.log(fileName + " loaded.");
                        } catch (IOException e) {
                            logger.warn("Couldn't load language_code: " + language_code);
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
                        Objects.requireNonNull(instance.getResource("lang/" + "en-US.yml")),
                        GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
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
            logger.logToFile("Selected language: " + language_code, logFile);

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
            logger.logToFile("debug_log: " + debug_log, logFile);

            plugin_prefix = config.getString("plugin_prefix");
            logger.setPluginPrefix(plugin_prefix);

            tree_log = config.getBoolean("tree_log");
            plant_log = config.getBoolean("plant_log");
            bonemeal_log = config.getBoolean("bonemeal_log");
            log_coords = config.getBoolean("log_coords");

            // General settings
            language_code = config.getString("language_code");
            enabled_worlds = config.getStringList("enabled_worlds");
            bonemeal_limit = config.getInt("bonemeal_limit");
            min_natural_light = config.getInt("min_natural_light");
            destroy_farmland = config.getBoolean("destroy_farmland");
            require_hoe = config.getBoolean("require_hoe");
            report_growth = config.getBoolean("report_growth");

            // Fertilizer settings
            fertilizer_enabled = config.getBoolean("fertilizer_enabled");
            fertilizer_radius = config.getInt("fertilizer_radius");
            fertilizer_passiv = config.getBoolean("fertilizer_passiv");
            fertilizer_boost_growth_rate = config.getDouble("fertilizer_boost_growth_rate");
            fertilizer_allow_growth_rate_above_100 = config.getBoolean("fertilizer_allow_growth_rate_above_100");

            // UV-Light settings
            uv_enabled = config.getBoolean("uv_enabled");
            uv_radius = config.getInt("uv_radius");
            require_all_uv_blocks = config.getBoolean("require_all_uv_blocks");

            List<String> uv_blocks_string = config.getStringList("uv_blocks");
            uv_blocks_string.forEach((materialName) -> {
                uv_blocks.add(Material.getMaterial(materialName));
            });

            List<String> grow_in_dark_string = config.getStringList("grow_in_dark");
            grow_in_dark_string.forEach((materialName) -> {
                grow_in_dark.add(Material.getMaterial(materialName));
            });


            printConfigData();

        }catch (YAMLException e){
            logger.error("&cAn Error occurred while reading config.yml data!");
            logger.log(e.getLocalizedMessage());

            instance.disablePlugin();
        }
    }

    // TODO: Check user modified data
    private void readLanguageData(){
        languageFileData = selectedLanguageFile.getStringRouteMappedValues(true);

        if(debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile( "-------------------- " +
                        Objects.requireNonNull(selectedLanguageFile.getFile()).getName() +
                        "-------------------- " , logFile);
                logger.logToFile("", logFile);

                printMap(languageFileData);

            }, 7 * 20);
        }
    }

    // TODO: Check user modified data
    private void readBiomeGroupsData(){
        biomeGroupsData = biomeGroupsFile.getStringRouteMappedValues(true);

        if(debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- BiomeGroups --------------------", logFile);
                logger.logToFile("", logFile);
                printMap(biomeGroupsData);
            }, 8 * 20);
        }
    }

    // TODO: Check user modified data
    private void readGrowthModificatorsData(){
        growthModificatorsData = growthModificatorsFile.getStringRouteMappedValues(true);

        if(debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- GrowthModificators --------------------", logFile);
                logger.logToFile("", logFile);
                printMap(growthModificatorsData);
            }, 9 * 20);
        }
    }

    private void printMap(Map<String, Object> data){
        Set<String> keys = data.keySet();
        keys.forEach((key) -> {
            logger.logToFile(key, logFile);
        });
    }

    private void printConfigData(){
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Config.yml Data --------------------", logFile);
            logger.logToFile("", logFile);

            logger.logToFile("plugin_prefix: " + plugin_prefix, logFile);
            logger.logToFile("tree_log: " + tree_log, logFile);
            logger.logToFile("plant_log: " + plant_log, logFile);
            logger.logToFile("bonemeal_log: " + bonemeal_log, logFile);
            logger.logToFile("log_coords: " + log_coords, logFile);
            logger.logToFile("language_code: " + language_code, logFile);
            logger.logToFile("enabled worlds:", logFile);
            enabled_worlds.forEach((n) -> {
                logger.logToFile("  - " + n, logFile);
            });
            logger.logToFile("bonemeal_limit: " + bonemeal_limit, logFile);
            logger.logToFile("min_natural_light: " + min_natural_light, logFile);
            logger.logToFile("destroy_farmland: " + destroy_farmland, logFile);
            logger.logToFile("require_hoe: " + require_hoe, logFile);
            logger.logToFile("report_growth: " + report_growth, logFile);

            logger.logToFile("fertilizer_enabled: " + fertilizer_enabled, logFile);
            logger.logToFile("fertilizer_radius: " + fertilizer_radius, logFile);
            logger.logToFile("fertilizer_passiv: " + fertilizer_passiv, logFile);
            logger.logToFile("fertilizer_boost_growth_rate: "
                    + fertilizer_boost_growth_rate, logFile);
            logger.logToFile("fertilizer_allow_growth_rate_above_100: "
                    + fertilizer_allow_growth_rate_above_100, logFile);

            logger.logToFile("uv_enabled: " + uv_enabled, logFile);
            logger.logToFile("uv_radius: " + uv_radius, logFile);
            logger.logToFile("require_all_uv_blocks: " + require_all_uv_blocks, logFile);

            logger.logToFile("uv_blocks:", logFile);
            uv_blocks.forEach( (materialName) -> {
                logger.logToFile("  - " + materialName, logFile);
            });

            logger.logToFile("grow_in_dark:", logFile);
            grow_in_dark.forEach( (materialName) -> {
                logger.logToFile("  - " + materialName, logFile);
            });
        }, 6 * 20);
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
            logger.debug("Config.yml reloaded.");

            growthModificatorsFile.reload();
            logger.debug("GrowthModificators.yml reloaded.");

            biomeGroupsFile.reload();
            logger.debug("BiomeGroups.yml reloaded.");

            selectedLanguageFile.update();
            logger.debug("Language files reloaded.");

            // Get updated config data and store new data in global variables.
            readConfigData();
            registerSupportedLanguages();
            registerSelectedLanguage();
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

    public int getMin_Natural_Light() {
        return min_natural_light;
    }

    public boolean isDestroy_Farmland(){
        return destroy_farmland;
    }

    public boolean isRequire_Hoe() {
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

    public boolean isUV_Enabled() {
        return uv_enabled;
    }

    public int getUV_Radius() {
        return uv_radius;
    }

    public ArrayList<Material> getUV_Blocks() {
        return uv_blocks;
    }

    public boolean getRequire_All_UV_Blocks(){
        return require_all_uv_blocks;
    }

    public ArrayList<Material> getGrow_In_Dark() {
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

    public YamlDocument getConfigFile(){
        return config;
    }

    public YamlDocument getBiomeGroupsFile(){
        return biomeGroupsFile;
    }

    public YamlDocument getGrowthModificatorsFile(){
        return growthModificatorsFile;
    }

}
