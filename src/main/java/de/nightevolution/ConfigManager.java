package de.nightevolution;

import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
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

    private static YamlDocument growthModifiersFile;
    private static Map<String, Object> growthModifierData;

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
    private static boolean display_growth_rates;
    private static int display_cooldown;

    // Fertilizer config values
    private static boolean fertilizer_enabled;
    private static int fertilizer_radius;
    private static boolean fertilizer_passiv;
    private static double fertilizer_boost_growth_rate;
    private static boolean fertilizer_allow_growth_rate_above_100;
    private static boolean fertilizer_enables_growth_in_invalid_biomes;
    private static double fertilizer_invalid_biome_growth_rate;
    private static double fertilizer_invalid_biome_death_chance;

    // UV-Light config values
    private static boolean uv_enabled;
    private static int uv_radius;
    private static boolean require_all_uv_blocks;
    private static HashSet<Material> uv_blocks = new HashSet<>();
    private static HashSet<Material> grow_in_dark = new HashSet<>();

    // Sound and Effects Sections
    private static Section plant_death_sound_effect;


    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get().
     * ConfigManager uses BoostedYAML API to perform file operations.
     */
    private ConfigManager() {

        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        // TODO: Set verbose and debug values of logger to false
        logger = new Logger(this.getClass().getSimpleName(), instance, true, true);

        pluginFolder = instance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");


        registerYamlConfigs();
        readConfigData();


        if (!languageFolder.exists()) {
            logger.warn("&eLanguage directory doesn't exist!");
            logger.log("Creating new directory...");

            try {
                if (languageFolder.mkdir()) {
                    logger.log("New language directory created.");
                }

            } catch (SecurityException e) {
                logger.error("&cCouldn't create language directory!");
                instance.disablePlugin();
            }

        } else
            logger.logToFile("Language directory already exist.", logFile);

        logger.log("Loading supported languages...");
        registerSupportedLanguages();
        registerSelectedLanguage();
        readLanguageData();

        logger.log("Loading BiomeGroups data...");
        readBiomeGroupsData();

        logger.log("Loading GrowthModifiers ...");
        readGrowthModifierData();

    }

    /**
     * Get a Singleton instance from ConfigManager.
     * Creates a new instance if no instance already exists.
     *
     * @return ConfigManager Singleton instance
     */
    protected static ConfigManager get() {
        if (configManager == null)
            new ConfigManager();
        return configManager;
    }


    /**
     * Registers all config Files for RealisticPlantGrowth Plugin.
     * Creates new one if no config exists.
     * Uses BoostedYAML API for config operations.
     */
    private void registerYamlConfigs() {
        // Main Config
        // -> should use default values if something is missing.
        try {
            config = YamlDocument.create(new File(pluginFolder, "Config.yml"),
                    Objects.requireNonNull(instance.getResource("Config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            config.update();
            logger.log("Config.yml loaded.");

        } catch (IOException e) {
            logger.error("&cCouldn't load YAML configuration!");
            instance.disablePlugin();
        }

        // BiomeGroups Config
        // don't use defaults here
        GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
        try {
            biomeGroupsFile = YamlDocument.create(new File(pluginFolder, "BiomeGroups.yml"),
                    Objects.requireNonNull(instance.getResource("BiomeGroups.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            logger.log("BiomeGroups.yml loaded.");

        } catch (IOException e) {
            logger.error("&cCouldn't load BiomeGroups YAML configuration!");
            instance.disablePlugin();
        }

        // GrowthModifiers Config
        try {
            growthModifiersFile = YamlDocument.create(new File(pluginFolder, "GrowthModifiers.yml"),
                    Objects.requireNonNull(instance.getResource("GrowthModifiers.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            logger.log("GrowthModifiers.yml loaded.");

        } catch (IOException e) {
            logger.error("&cCouldn't load GrowthModifiers YAML configuration!");
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

                if (languageCode.equalsIgnoreCase(getLanguage_code())) {
                    logger.logToFile("Loading selected language File: " + languageFolder + File.separator + languageCode + ".yml", logFile);

                    selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

                } else {
                    logger.logToFile("Loading language File: " + languageFolder + File.separator + languageCode + ".yml", logFile);
                    YamlDocument temp = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
                    temp.update();
                }

                logger.logToFile(languageCode + ".yml loaded.", logFile);

            }
        } catch (IOException e) {
            logger.error("&cCouldn't load language files!");
            instance.disablePlugin();
            return;
        }

        // Search for custom files in lang directory.
        if (selectedLanguageFile == null) {
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
        if (selectedLanguageFile == null) {
            try {
                logger.warn("No custom language file with language_code '" + getLanguage_code() + "' located in 'lang' directory!");
                logger.warn("Using default language file: en-US");
                selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, "en-US.yml"),
                        Objects.requireNonNull(instance.getResource("lang/" + "en-US.yml")),
                        GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            } catch (IOException e) {
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
    private void registerSelectedLanguage() {

        try {
            selectedLanguageFile.update();
            logger.log("Language files loaded.");
            logger.log("Selected language: " + language_code);

        } catch (IOException e) {
            logger.error("&cCouldn't load YAML configuration!");
            instance.disablePlugin();
        }
    }

    /**
     * Reads the debug boolean from the config file.
     * Todo: Make this call asynchronous
     * Todo: Add parameter check !!!
     */
    private void readConfigData() {

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
            display_growth_rates = config.getBoolean("display_growth_rates");
            display_cooldown = config.getInt("display_cooldown");

            // Fertilizer settings
            fertilizer_enabled = config.getBoolean("fertilizer_enabled");
            fertilizer_radius = config.getInt("fertilizer_radius");
            fertilizer_passiv = config.getBoolean("fertilizer_passiv");
            fertilizer_boost_growth_rate = config.getDouble("fertilizer_boost_growth_rate");
            fertilizer_allow_growth_rate_above_100 = config.getBoolean("fertilizer_allow_growth_rate_above_100");
            fertilizer_enables_growth_in_invalid_biomes = config.getBoolean("fertilizer_enables_growth_in_invalid_biomes");
            fertilizer_invalid_biome_growth_rate = config.getDouble("fertilizer_invalid_biome_growth_rate");
            fertilizer_invalid_biome_death_chance = config.getDouble("fertilizer_invalid_biome_death_chance");

            // UV-Light settings
            uv_enabled = config.getBoolean("uv_enabled");
            uv_radius = config.getInt("uv_radius");
            require_all_uv_blocks = config.getBoolean("require_all_uv_blocks");

            List<String> uv_blocks_string = config.getStringList("uv_blocks");
            uv_blocks = getCheckedMaterialSet(uv_blocks_string);

            List<String> grow_in_dark_string = config.getStringList("grow_in_dark");
            grow_in_dark = getCheckedMaterialSet(grow_in_dark_string);

            // Sound & Effects
            plant_death_sound_effect = config.getSection("plant_death_sound_effect");
            checkSoundEffectSection();


            printConfigData();

        } catch (YAMLException e) {
            logger.error("&cAn Error occurred while reading config.yml data!");
            logger.log(e.getLocalizedMessage());

            instance.disablePlugin();
        }
    }

    private void readLanguageData() {
        languageFileData = selectedLanguageFile.getStringRouteMappedValues(true);

        if (debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- " +
                        Objects.requireNonNull(selectedLanguageFile.getFile()).getName() +
                        "-------------------- ", logFile);
                logger.logToFile("", logFile);

                printMap(languageFileData);

            }, 7 * 20);
        }
    }

    // TODO: Check user modified data
    private void readBiomeGroupsData() {
        biomeGroupsData = biomeGroupsFile.getStringRouteMappedValues(true);

        if (debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- BiomeGroups --------------------", logFile);
                logger.logToFile("", logFile);
                printMap(biomeGroupsData);
            }, 8 * 20);
        }
    }

    // TODO: Check user modified data
    private void readGrowthModifierData() {
        growthModifierData = growthModifiersFile.getStringRouteMappedValues(true);

        if (debug_log) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- GrowthModifiers --------------------", logFile);
                logger.logToFile("", logFile);
                printMap(growthModifierData);
            }, 9 * 20);
        }
    }

    private void printMap(Map<String, Object> data) {
        Set<String> keys = data.keySet();
        keys.forEach((key) -> {
            logger.logToFile(key, logFile);
        });
    }

    private void printConfigData() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Config.yml Data --------------------", logFile);
            logger.logToFile("", logFile);

            logger.logToFile("plugin_prefix: " + plugin_prefix, logFile);
            logger.logToFile("tree_log: " + tree_log, logFile);
            logger.logToFile("plant_log: " + plant_log, logFile);
            logger.logToFile("bonemeal_log: " + bonemeal_log, logFile);
            logger.logToFile("log_coords: " + log_coords, logFile);

            // General settings
            logger.logToFile("language_code: " + language_code, logFile);
            logger.logToFile("enabled worlds:", logFile);
            enabled_worlds.forEach((n) -> {
                logger.logToFile("  - " + n, logFile);
            });
            logger.logToFile("bonemeal_limit: " + bonemeal_limit, logFile);
            logger.logToFile("min_natural_light: " + min_natural_light, logFile);
            logger.logToFile("destroy_farmland: " + destroy_farmland, logFile);
            logger.logToFile("require_hoe: " + require_hoe, logFile);
            logger.logToFile("display_growth_rates: " + display_growth_rates, logFile);
            logger.logToFile("display_cooldown: " + display_cooldown, logFile);

            // Fertilizer settings
            logger.logToFile("fertilizer_enabled: " + fertilizer_enabled, logFile);
            logger.logToFile("fertilizer_radius: " + fertilizer_radius, logFile);
            logger.logToFile("fertilizer_passiv: " + fertilizer_passiv, logFile);
            logger.logToFile("fertilizer_boost_growth_rate: " +
                    fertilizer_boost_growth_rate, logFile);
            logger.logToFile("fertilizer_allow_growth_rate_above_100: " +
                    fertilizer_allow_growth_rate_above_100, logFile);
            logger.logToFile("fertilizer_enables_growth_in_invalid_biomes: " +
                    fertilizer_enables_growth_in_invalid_biomes, logFile);
            logger.logToFile("fertilizer_invalid_biome_growth_rate: " +
                    fertilizer_invalid_biome_growth_rate, logFile);
            logger.logToFile("fertilizer_invalid_biome_death_chance: " +
                    fertilizer_invalid_biome_death_chance, logFile);

            // UV-Light settings
            logger.logToFile("uv_enabled: " + uv_enabled, logFile);
            logger.logToFile("uv_radius: " + uv_radius, logFile);
            logger.logToFile("require_all_uv_blocks: " + require_all_uv_blocks, logFile);

            logger.logToFile("uv_blocks:", logFile);
            uv_blocks.forEach((materialName) -> {
                logger.logToFile("  - " + materialName, logFile);
            });

            logger.logToFile("grow_in_dark:", logFile);
            grow_in_dark.forEach((materialName) -> {
                logger.logToFile("  - " + materialName, logFile);
            });

            // Sound & Effects
            logger.logToFile("plant_death_sound_effect: ", logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getBoolean("enabled"), logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getString("sound"), logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getFloat("volume"), logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getFloat("pitch"), logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getString("effect"), logFile);
            logger.logToFile("  - " + plant_death_sound_effect.getInt("data"), logFile);

        }, 6 * 20);
    }

    /**
     * Reloads configuration files when the plugin is in a reloading state.
     * Reads values from the configuration files and updates global fields accordingly.
     * This method reloads the main configuration file (Config.yml), as well as other YAML files
     * responsible for growth modifiers (GrowthModifiers.yml), biome groups (BiomeGroups.yml),
     * and language settings (Language files).
     */
    public void reloadAllYAMLFiles() {
        logger.warn("&eReloading config file...");
        try {
            config.reload();
            logger.debug("Config.yml reloaded.");

            growthModifiersFile.reload();
            logger.debug("GrowthModifiers.yml reloaded.");

            biomeGroupsFile.reload();
            logger.debug("BiomeGroups.yml reloaded.");

            selectedLanguageFile.reload();
            logger.debug("Language files reloaded.");

            uv_blocks.clear();
            grow_in_dark.clear();

            // Get updated config data and store new data in global variables.
            readConfigData();
            registerSupportedLanguages();
            registerSelectedLanguage();
            readLanguageData();
            readBiomeGroupsData();
            readGrowthModifierData();

            logger.log("&2All configuration files reloaded.");


        } catch (YAMLException | IOException e) {
            logger.log(e.getLocalizedMessage());
            logger.error("&cError while reloading config files.");
            instance.disablePlugin();
        }
    }

    public Optional<Section> getConfigSection(Route routeToSection) {
        return config.getOptionalSection(routeToSection);
    }

    public Optional<Section> getGrowthModifierSection(Route routeToSection) {
        return growthModifiersFile.getOptionalSection(routeToSection);
    }

    public Optional<List<String>> getBiomeGroupStringList(Route routeToSection) {
        return biomeGroupsFile.getOptionalStringList(routeToSection);
    }

    public Section getDefaultModifierSection(Material plantType) {
        Route r = Route.from(plantType, "Default");
        Optional<Section> defaultSection = growthModifiersFile.getOptionalSection(r);
        if (defaultSection.isPresent()) {
            return defaultSection.get();
        }
        logger.error("Check your GrowthModifiers.yml and make sure every entry has a 'Default' Section!");
        throw new NullPointerException("Default Section for '" + plantType + "' is missing!");
    }

    public double getModifierOnRoute(Route route) {
        Optional<Double> optionalDouble = growthModifiersFile.getOptionalDouble(route);

        if (optionalDouble.isEmpty()) {
            logger.error("No value for modifier '" + route + "' found in default section!");
            logger.error("Please assign a double value to this modifier in the default section.");
            logger.error("For more information, check out GrowthModifiers in the wiki.");
            throw new YAMLException("Check your GrowthModifiers.yml!");
        }

        return optionalDouble.get();
    }


    private HashSet<Material> getCheckedMaterialSet(List<String> stringMaterialList) {
        HashSet<Material> materialSet = new HashSet<>(stringMaterialList.size());
        for (String materialName : stringMaterialList) {
            Material material = Material.getMaterial(materialName);
            if (material != null) {
                materialSet.add(material);
            } else {
                logger.warn("uv_blocks: '" + materialName + "' is not an Bukkit Material!");
                logger.warn("Please check your sections in config.yml!");
            }
        }

        return materialSet;
    }


    private void checkSoundEffectSection() {
        boolean soundEffectEnabled = plant_death_sound_effect.getBoolean("enabled");
        boolean soundValid = false;
        boolean effectValid = false;

        if (!soundEffectEnabled)
            return;

        // Checking, if String is a Bukkit sound/effect
        String sound = plant_death_sound_effect.getString("sound");
        String effect = plant_death_sound_effect.getString("effect");

        try {
            Sound.valueOf(sound);
            soundValid = true;
        } catch (IllegalArgumentException e) {
            logger.warn(sound + " is not a valid Bukkit sound!");
        }

        try {
            Effect.valueOf(effect);
            effectValid = true;
        } catch (IllegalArgumentException e) {
            logger.warn(effect + " is not a valid Bukkit effect!");
        }

        if (!(soundValid && effectValid)) {
            logger.warn("Using default values instead.");
            plant_death_sound_effect = plant_death_sound_effect.getDefaults();
        }

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

    public boolean isLog_Coords() {
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

    public boolean isDestroy_Farmland() {
        return destroy_farmland;
    }

    public boolean isRequire_Hoe() {
        return require_hoe;
    }

    public boolean isDisplay_growth_rates() {
        return display_growth_rates;
    }

    public int getDisplay_cooldown() {
        return display_cooldown;
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

    public boolean isFertilizer_Enables_Growth_In_Invalid_Biomes() {
        return fertilizer_enables_growth_in_invalid_biomes;
    }

    public double getFertilizer_invalid_biome_growth_rate() {
        return fertilizer_invalid_biome_growth_rate;
    }

    public double getFertilizer_invalid_biome_death_chance() {
        return fertilizer_invalid_biome_death_chance;
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

    public HashSet<Material> getUV_Blocks() {
        return uv_blocks;
    }

    public boolean getRequire_All_UV_Blocks() {
        return require_all_uv_blocks;
    }

    public HashSet<Material> getGrow_In_Dark() {
        return grow_in_dark;
    }

    public Section getPlant_death_sound_effect() {
        return plant_death_sound_effect;
    }

    public Map<String, Object> getBiomeGroups() {
        return biomeGroupsData;
    }

    public Map<String, Object> getGrowthModifiers() {
        return growthModifierData;
    }

    public Map<String, Object> getLanguageFileData() {
        return languageFileData;
    }

    public YamlDocument getConfigFile() {
        return config;
    }

    public String getSelectedLanguageString(String s) {
        return selectedLanguageFile.getString(s);
    }

    public YamlDocument getBiomeGroupsFile() {
        return biomeGroupsFile;
    }

    public YamlDocument getGrowthModifiersFile() {
        return growthModifiersFile;
    }

}
