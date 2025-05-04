package de.nightevolution.realisticplantgrowth;

import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.enums.ModifierType;
import de.nightevolution.realisticplantgrowth.utils.exception.ConfigurationException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ConfigManager is a singleton class responsible for managing and handling the configuration
 * of the Bukkit/Spigot plugin "RealisticPlantGrowth." It uses the BoostedYAML API for file operations
 * and provides methods to access and update configuration values.
 */
public class ConfigManager {

    private static ConfigManager configManager;
    private static RealisticPlantGrowth instance;
    private static Logger logger;

    private static String plugin_prefix;
    private static final String logFile = "debug";

    // Route to plant modifiers in GrowthModifiers.yml
    private static final Route biomeGroupSectionRoute = Route.from("BiomeGroup");
    private static final Route biomeGroupsListRoute = Route.from("BiomeGroup", "Groups");
    private static final Route defaultSectionRoute = Route.from("Default");
    private static final Route defaultBiomeListRoute = Route.from("Default", "Biome");

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
            "en-US",
            "fi-FI",
            "ru-RU",
            "zh-Hant"
    ));

    private static String language_code;

    // Different debug and logging modes
    private static boolean verbose = true;
    private static boolean debug_log;
    private static boolean structure_log;
    private static boolean plant_log;
    private static boolean bonemeal_log;
    private static boolean player_log;

    // Enabled worlds
    private static List<String> enabled_worlds;
    private static boolean use_enabled_worlds_as_world_blacklist;

    // More config values
    private static int bonemeal_limit;
    private static boolean allow_bonemeal_in_composters;
    private static int min_natural_light;
    private static boolean destroy_farmland;
    private static boolean require_hoe;
    private static boolean display_growth_rates;
    private static int display_cooldown;
    private static boolean use_metrics;
    private static boolean check_for_updates;

    // Composter config values
    private static Section composterSection;

    // Fertilizer config values
    private static boolean fertilizer_enabled;
    private static int fertilizer_radius;
    private static boolean fertilizer_passive;
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
     * Constructor for a new Singleton ConfigManager instance, which creates, reads, and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get().
     * ConfigManager uses BoostedYAML API to perform file operations.
     */
    private ConfigManager() {

        configManager = this;
        instance = RealisticPlantGrowth.getInstance();

        logger = new Logger(this.getClass().getSimpleName(), false, false);

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

            } catch (Exception e) {
                logger.error("&cCouldn't create language directory!");
                throw new ConfigurationException("&cCouldn't create language directory!");
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

        // Check GrowthModifiers.yml
        verifyGrowthModifiersConfiguration();

        if (debug_log)
            printConfigData();

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
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
            config.update();
            logger.log("Config.yml loaded.");

        } catch (IOException e) {
            logger.error("&cCouldn't load YAML configuration!");
            throw new ConfigurationException("&cCouldn't load YAML configuration!");
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
            throw new ConfigurationException("&cCouldn't load BiomeGroups YAML configuration!");
        }

        // GrowthModifiers Config
        try {
            growthModifiersFile = YamlDocument.create(new File(pluginFolder, "GrowthModifiers.yml"),
                    Objects.requireNonNull(instance.getResource("GrowthModifiers.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            logger.log("GrowthModifiers.yml loaded.");

        } catch (IOException e) {
            logger.error("&cCouldn't load GrowthModifiers YAML configuration!");
            throw new ConfigurationException("&cCouldn't load GrowthModifiers YAML configuration!");
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
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

                } else {
                    logger.logToFile("Loading language File: " + languageFolder + File.separator + languageCode + ".yml", logFile);
                    YamlDocument temp = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(instance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
                    temp.update();
                }

                logger.logToFile(languageCode + ".yml loaded.", logFile);

            }
        } catch (IOException e) {
            logger.error("&cCouldn't load language files!");
            throw new ConfigurationException("&cCouldn't load language files!");
        }

        // Search for custom files in lang directory.
        if (selectedLanguageFile == null) {
            // lese daten von ordner
            File[] allFiles = languageFolder.listFiles();

            if (allFiles == null) {
                logger.error("&cCouldn't load language files!");
                throw new ConfigurationException("&cCouldn't load language files!");
            }

            GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
            logger.log("Searching for custom language files...");
            for (File file : allFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.equalsIgnoreCase(language_code + ".yml")) {
                        try {
                            selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, language_code + ".yml"),
                                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                                    UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
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
                        GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                        UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
            } catch (IOException e) {
                logger.error("&cCouldn't load custom language file!");
                throw new ConfigurationException("&cCouldn't load custom language file!");
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
            throw new ConfigurationException("&cCouldn't load YAML configuration!");
        }
    }

    /**
     * Reads the configuration data from the main config file (Config.yml) and initializes global variables
     * with the values read from the file.
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

            structure_log = config.getBoolean("structure_log");
            plant_log = config.getBoolean("plant_log");
            bonemeal_log = config.getBoolean("bonemeal_log");
            player_log = config.getBoolean("player_log");

            // General settings
            language_code = config.getString("language_code");
            enabled_worlds = config.getStringList("enabled_worlds");
            use_enabled_worlds_as_world_blacklist = config.getBoolean("use_enabled_worlds_as_world_blacklist");
            bonemeal_limit = config.getInt("bonemeal_limit");
            allow_bonemeal_in_composters = config.getBoolean("allow_bonemeal_in_composters");
            min_natural_light = config.getInt("min_natural_light");
            destroy_farmland = config.getBoolean("destroy_farmland");
            require_hoe = config.getBoolean("require_hoe");
            display_growth_rates = config.getBoolean("display_growth_rates");
            display_cooldown = config.getInt("display_cooldown");
            use_metrics = config.getBoolean("use_metrics");
            check_for_updates = config.getBoolean("check_for_updates");

            // Composter settings
            composterSection = config.getSection("composter");

            // Fertilizer settings
            fertilizer_enabled = config.getBoolean("fertilizer_enabled");
            fertilizer_radius = config.getInt("fertilizer_radius");
            fertilizer_passive = config.getBoolean("fertilizer_passive");
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


        } catch (YAMLException e) {
            logger.error("&cAn Error occurred while reading config.yml data!");
            logger.log(e.getLocalizedMessage());

            throw new ConfigurationException("&cAn Error occurred while reading config.yml data!");
        }
    }

    /**
     * Reads the language data from the selected language file and initializes the languageFileData map
     * with the values read from the file.
     */
    private void readLanguageData() {
        languageFileData = selectedLanguageFile.getStringRouteMappedValues(false);
    }

    /**
     * Reads the BiomeGroups data from the BiomeGroups.yml file and initializes the biomeGroupsData map
     * with the values read from the file.
     */
    private void readBiomeGroupsData() {
        biomeGroupsData = biomeGroupsFile.getStringRouteMappedValues(false);
    }


    /**
     * Reads the GrowthModifiers data from the GrowthModifiers.yml file and initializes the growthModifierData map
     * with the values read from the file.
     */
    private void readGrowthModifierData() {
        growthModifierData = growthModifiersFile.getStringRouteMappedValues(false);
    }

    /**
     * Prints the key set of a given map to the log file.
     *
     * @param data The map to be printed.
     */
    private void printMap(Map<String, Object> data) {
        Set<String> keys = data.keySet();
        keys.forEach((key) -> logger.logToFile(key, logFile));
    }

    /**
     * Prints the configuration data to the log file.
     */
    private void printConfigData() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Config.yml Data --------------------", logFile);
            logger.logToFile("", logFile);

            logger.logToFile("plugin_prefix: " + plugin_prefix, logFile);
            logger.logToFile("structure_log: " + structure_log, logFile);
            logger.logToFile("plant_log: " + plant_log, logFile);
            logger.logToFile("bonemeal_log: " + bonemeal_log, logFile);
            logger.logToFile("player_log: " + player_log, logFile);

            // General settings
            logger.logToFile("language_code: " + language_code, logFile);
            logger.logToFile("enabled worlds:", logFile);
            enabled_worlds.forEach((n) -> logger.logToFile("  - " + n, logFile));

            logger.logToFile("bonemeal_limit: " + bonemeal_limit, logFile);
            logger.logToFile("allow_bonemeal_in_composters: " + allow_bonemeal_in_composters, logFile);
            logger.logToFile("min_natural_light: " + min_natural_light, logFile);
            logger.logToFile("destroy_farmland: " + destroy_farmland, logFile);
            logger.logToFile("require_hoe: " + require_hoe, logFile);
            logger.logToFile("display_growth_rates: " + display_growth_rates, logFile);
            logger.logToFile("display_cooldown: " + display_cooldown, logFile);
            logger.logToFile("use_metrics: " + use_metrics, logFile);
            logger.logToFile("check_for_updates: " + check_for_updates, logFile);

            // Composter settings
            logger.logToFile("composter: ", logFile);
            logger.logToFile("  - " + composterSection.getBoolean("disable_bonemeal_output"), logFile);
            logger.logToFile("  - " + composterSection.getBoolean("quick_fill_with_shift"), logFile);
            logger.logToFile("  - " + composterSection.getBoolean("allow_bonemeal_as_input"), logFile);

            // Fertilizer settings
            logger.logToFile("fertilizer_enabled: " + fertilizer_enabled, logFile);
            logger.logToFile("fertilizer_radius: " + fertilizer_radius, logFile);
            logger.logToFile("fertilizer_passive: " + fertilizer_passive, logFile);
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
            uv_blocks.forEach((materialName) -> logger.logToFile("  - " + materialName, logFile));

            logger.logToFile("grow_in_dark:", logFile);
            grow_in_dark.forEach((materialName) -> logger.logToFile("  - " + materialName, logFile));

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
     * Reloads all configuration files when the plugin is in a reloading state.
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
            verifyGrowthModifiersConfiguration();


            if (debug_log)
                printConfigData();

            logger.log("&2All configuration files reloaded.");


        } catch (YAMLException | IOException e) {
            logger.log(e.getLocalizedMessage());
            logger.error("&cError while reloading config files.");
            throw new ConfigurationException("&cError while reloading config files.");
        }
    }


    /**
     * <p>Validates the configuration in {@code GrowthModifiers.yml} for correctness and completeness.</p>
     *
     * <p>This method ensures that all plants have defined growth modifiers for their respective biome groups
     * or appropriate default settings. During the verification process, any detected issues are logged,
     * and the plugin is disabled if the configuration is found to be incomplete.</p>
     *
     * <p><b>Important:</b> Proper configuration is critical to avoid runtime errors and ensure expected plugin functionality.</p>
     */
    private void verifyGrowthModifiersConfiguration() {
        logger.log("Starting verification of GrowthModifiers.yml...");

        // Iterate through all plants defined in the GrowthModifiers.yml file
        for (String plantSectionString : growthModifierData.keySet()) {
            Section plantSection = growthModifiersFile.getSection(Route.from(plantSectionString));

            // Retrieve all biome groups assigned to the current plant
            List<String> biomeGroupsList = plantSection.getStringList(biomeGroupsListRoute);

            // Validate growth modifiers for each biome group assigned to the plant
            if (!biomeGroupsList.isEmpty()) {
                for (String biomeGroup : biomeGroupsList) {
                    if (isValidBiomeGroupName(biomeGroup))
                        checkBiomeGroupModifiers(plantSection, biomeGroup);
                    else {
                        logger.warn("BiomeGroup '" + biomeGroup + "' is not defined in your BiomeGroups.yml and will be ignored.");
                        logger.warn("Check the configuration at: " + plantSection.getNameAsString() + " -> BiomeGroup -> Groups -> " + biomeGroup);
                        logger.warn("Please verify your BiomeGroups.yml to avoid potential issues");
                    }
                }
            }

            // Check for default modifiers if additional biomes are defined in the Default section
            List<String> biomeStringList = plantSection.getStringList(defaultBiomeListRoute);

            if (!biomeStringList.isEmpty()) {
                checkDefaultModifiers(plantSection);
            }

            logger.verbose(plantSection.getNameAsString() + ": Verification completed.");
        }

        logger.log("GrowthModifiers.yml verification completed successfully.");
    }

    /**
     * <p>Validates the growth modifiers for a specified biome group within a plant's configuration.</p>
     *
     * <p>This method ensures that all required modifiers for the given biome group are declared.
     * If the biome group is missing or incomplete, the method checks the plant's default modifiers
     * for fallback settings. If any required modifier is missing, it logs the error and triggers
     * the necessary error handling routine.</p>
     *
     * @param plantSection <br>The {@link Section} of the plant being validated.
     * @param biomeGroupString <br>The name of the biome group whose growth modifiers are being verified.
     */
    private void checkBiomeGroupModifiers(Section plantSection, String biomeGroupString) {
        Optional<Section> optionalBiomeGroupSection = plantSection.getOptionalSection(Route.from("BiomeGroup", biomeGroupString));

        if (optionalBiomeGroupSection.isPresent()) {
            // Check if all required modifiers are declared in this section
            for (ModifierType modifier : ModifierType.getModifierTypeList()) {
                Optional<Double> optionalDouble = optionalBiomeGroupSection.get().getOptionalDouble(modifier.getValue());

                if (optionalDouble.isEmpty()) {
                    Route modifierRoute = Route.from(plantSection.getRouteAsString(), "BiomeGroup", biomeGroupString, modifier.getValue());
                    handleConfigurationError(modifierRoute);
                }
            }
        } else {
            // If the biome group section is not defined, check default modifiers
            logger.debug("BiomeGroup Section '" + biomeGroupString + "' not found for plant '" + plantSection.getNameAsString() + "'.");
            logger.debug("Checking default modifier section for this plant...");
            checkDefaultModifiers(plantSection);
        }
    }

    /**
     * <p>Validates the default growth modifiers for a given plant section.</p>
     *
     * <p>This method ensures that all required modifiers are defined in the default section of the plant's configuration.
     * If any required modifier is missing, it logs the error and triggers the necessary error handling routine.</p>
     *
     * @param plantSection <br>The {@link Section} representing the plant to validate.
     */
    private void checkDefaultModifiers(Section plantSection) {
        for (ModifierType modifier : ModifierType.getModifierTypeList()) {
            Route modifierRoute = Route.from("Default", modifier.getValue());
            Optional<Double> optionalDouble = plantSection.getOptionalDouble(modifierRoute);

            if (optionalDouble.isEmpty()) {
                Route plantModifierRoute = Route.from(plantSection.getRouteAsString(), "Default", modifier.getValue());
                handleConfigurationError(plantModifierRoute);
            }
        }
    }

    /**
     * Checks if the provided biome group name is valid by verifying if it exists in the {@code BiomeGroups.yml} FIle.
     *
     * <p>This method looks up the given biome group name in the internal collection of registered biome groups
     * and returns {@code true} if the name exists, indicating that it's a valid biome group name.</p>
     *
     * @param biomeGroupNameToCheck the name of the biome group to be checked for validity
     * @return {@code true} if the biome group name exists in the internal data, otherwise {@code false}
     */
    private boolean isValidBiomeGroupName(String biomeGroupNameToCheck) {
        Set<String> biomeGroupNames = biomeGroupsData.keySet();
        return biomeGroupNames.contains(biomeGroupNameToCheck);
    }


    /**
     * <p>Handles configuration errors by logging the issue and disabling the plugin.</p>
     *
     * <p>This method is invoked whenever a required modifier is missing or the configuration is incomplete.
     * It logs the missing details, provides a link to the documentation for troubleshooting,
     * and disables the plugin to prevent runtime errors.</p>
     *
     * @param missingModifierRoute <br>The {@link Route} indicating the location of the missing modifier in the configuration file.
     */
    private void handleConfigurationError(Route missingModifierRoute) {
        logger.error("Configuration error detected in GrowthModifiers.yml!");
        logger.error("Missing modifier at: " + missingModifierRoute.get(0) + " -> " + missingModifierRoute.get(1) + " -> " + missingModifierRoute.get(2));
        logger.error("Please refer to the Wiki for correct configuration or join our Discord for support.");
        logger.error("Wiki: https://realistic-plant-growth.nightevolution.de/guides/configuration/growthmodifiers.yml");

        // Disable the plugin due to an incomplete configuration
        throw new ConfigurationException("Configuration error detected in GrowthModifiers.yml!");

    }


    /**
     * Gets the Section from the growth modifiers file based on the provided route.
     *
     * @param routeToSection The route to the desired section.
     * @return An optional Section containing the data from the specified route.
     */
    public Optional<Section> getGrowthModifierSection(Route routeToSection) {
        return growthModifiersFile.getOptionalSection(routeToSection);
    }

    /**
     * Gets a Set of Biomes from the BiomeGroups file based on the provided route.
     *
     * @param biomeGroup The name of the desired biome group section.
     * @return a HashSet containing Biomes from the specified route. (empty if route not valid)
     */
    @NotNull
    public List<String> getBiomeSetOfBiomeGroup(@NotNull String biomeGroup) {
        Optional<List<String>> biomeList = biomeGroupsFile.getOptionalStringList(biomeGroup);

        if (instance.isPaperFork()) {
            if (biomeList.isPresent() && !biomeList.get().isEmpty()) {
                List<String> returnList = new ArrayList<>();
                for (String biomeString : biomeList.get()) {
                    if (biomeString.contains(":")) {
                        returnList.add(biomeString);
                    } else {
                        try {
                            Biome biome = Biome.valueOf(biomeString);
                            returnList.add((biome.getKey().asString()));
                            logger.verbose("Checked BiomeString: " + biome);
                            logger.verbose("Added to return list: " + biome.getKey().asString());
                        } catch (IllegalArgumentException e) {
                            logger.warn("Biome '" + biomeString + "' is not a valid Bukkit Biome name!");
                            logger.warn("Please check your BiomeGroups.yml!");
                            logger.warn("Include the Namespace of custom Biomes!");
                        }
                    }
                }

                return returnList;
            } else
                return new ArrayList<>();
        } else {
            if (biomeList.isPresent() && !biomeList.get().isEmpty()) {
                return getCheckedBiomeSet(biomeList.get());
            } else
                return new ArrayList<>();
        }
    }


    /**
     * Gets a HashSet of Bukkit Material objects from a list of material names.
     * This method converts a list of material names into a HashSet of corresponding Bukkit {@link Material} objects.
     * If a material name is not recognized, a warning message is logged, and the invalid material is skipped.
     *
     * @param stringMaterialList The list of material names to be converted.
     * @return A HashSet containing Bukkit {@link Material} objects derived from the input material names.
     */
    private HashSet<Material> getCheckedMaterialSet(List<String> stringMaterialList) {
        HashSet<Material> materialSet = new HashSet<>(stringMaterialList.size());
        for (String materialName : stringMaterialList) {
            Material material = Material.getMaterial(materialName);
            if (material != null) {
                materialSet.add(material);
            } else {
                logger.warn("uv_blocks: '" + materialName + "' is not a recognized Bukkit Material!");
                logger.warn("Please check your sections in config.yml!");
            }
        }

        return materialSet;
    }

    /**
     * Gets a HashSet of Bukkit {@link Biome} objects from a list of biome names.
     * This method converts a list of biome names into a HashSet of corresponding Bukkit {@link Biome} objects.
     * If a biome name is not recognized, a warning message is logged, and the invalid biome is skipped.
     *
     * @param stringBiomeList The list of biome names to be converted.
     * @return A HashSet containing Bukkit {@link Biome} objects derived from the input biome names.
     */
    public List<String> getCheckedBiomeSet(List<String> stringBiomeList) {
        List<String> biomeSet = new ArrayList<>(stringBiomeList.size());
        for (String biomeName : stringBiomeList) {
            try {
                Biome biome = Biome.valueOf(biomeName);
                biomeSet.add(biome.toString());
                logger.verbose("Checked BiomeString: " + biome);
            } catch (IllegalArgumentException e) {
                logger.warn("Biome '" + biomeName + "' is not a valid Bukkit Biome name!");
                logger.warn("Please check your BiomeGroups.yml!");
            }
        }
        return biomeSet;
    }


    /**
     * Checks the validity of the sound and effect specified in the plant_death_sound_effect section.
     * If not valid, reverts to default values.
     */
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

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug_log() {
        return debug_log;
    }

    public boolean isStructure_log() {
        return structure_log;
    }

    public boolean isPlant_log() {
        return plant_log;
    }

    public boolean isBonemeal_log() {
        return bonemeal_log;
    }

    public boolean isPlayer_log() {
        return player_log;
    }


    public List<String> getEnabled_worlds() {
        return enabled_worlds;
    }

    public boolean isUse_enabled_worlds_as_world_blacklist() {
        return use_enabled_worlds_as_world_blacklist;
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

    public boolean use_metrics() {
        return use_metrics;
    }

    public boolean check_for_updates() {
        return check_for_updates;
    }


    public boolean isComposterBonemealOutputDisabled() {
        return composterSection.getBoolean("disable_bonemeal_output");
    }

    public boolean isComposterQuickFillEnabled() {
        return composterSection.getBoolean("quick_fill_with_shift");
    }

    public boolean isComposterBonemealInputAllowed() {
        return composterSection.getBoolean("allow_bonemeal_as_input");
    }


    public boolean isFertilizer_enabled() {
        return fertilizer_enabled;
    }

    public int getFertilizer_radius() {
        return fertilizer_radius;
    }

    public boolean isFertilizer_passive() {
        return fertilizer_passive;
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

    public Map<String, Object> getGrowthModifiers() {
        return growthModifierData;
    }

    public String getSelectedLanguageString(String s) {
        return selectedLanguageFile.getString(s);
    }

}
