package de.nightevolution.realisticplantgrowth;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.enums.MainConfigPath;
import de.nightevolution.realisticplantgrowth.utils.exception.ConfigurationException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    private static RealisticPlantGrowth rpgInstance;
    private static org.apache.logging.log4j.Logger logger;

    // Main config
    private static YamlDocument config;

    // Plant configs
    private static YamlDocument biomeGroupsFile;
    private static YamlDocument growthModifiersFile;

    // Selected language file containing plugin messages
    private static YamlDocument selectedLanguageFile;
    private static String selectedLanguageCode;

    private static File pluginFolder;
    private static File languageFolder;


    // All predefined supported localizations.
    private static final List<String> SUPPORTED_LANGUAGE_CODES = (Arrays.asList(
            "de-DE",
            "en-US",
            "fi-FI",
            "ru-RU",
            "zh-Hant"
    ));


    /**
     * Constructor for a new Singleton ConfigManager instance, which creates, reads, and updates the config file.
     * Get an instance of ConfigManager with ConfigManager.get().
     * ConfigManager uses BoostedYAML API to perform file operations.
     */
    private ConfigManager() {

        configManager = this;
        rpgInstance = RealisticPlantGrowth.getInstance();
        logger = LogUtils.getLogger(this.getClass());

        pluginFolder = rpgInstance.getDataFolder();
        languageFolder = new File(pluginFolder + File.separator + "lang");


        registerYamlConfigs();

        if (!languageFolder.exists()) {
            logger.warn("Language directory doesn't exist!");
            logger.info("Creating new directory...");

            if (languageFolder.mkdirs()) {
                logger.info("New language directory created.");
            } else {
                String errorMsg = "Failed to create language directory: " + languageFolder.getAbsolutePath();
                LogUtils.error(logger, errorMsg);
                throw new ConfigurationException(errorMsg);
            }

        } else
            LogUtils.debug(logger, "Language directory already exist.");

        logger.info("Loading supported languages...");
        registerSupportedLanguages();
        registerSelectedLanguage();

        logger.info("Loading BiomeGroups data...");

        logger.info("Loading GrowthModifiers ...");

        //TODO: Check Data

    }

    /**
     * Get a Singleton instance from ConfigManager.
     * Creates a new instance if no instance already exists.
     *
     * @return ConfigManager Singleton instance
     */
    public static synchronized ConfigManager get() {
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
                    Objects.requireNonNull(rpgInstance.getResource("Config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
            config.update();
            logger.info("Config.yml loaded.");

            // Set Logger debug flags
            LogUtils.setDebug(isDebug());
            LogUtils.setVerbose(isVerbose());
            logger = LogUtils.getLogger(this.getClass());

            if (isDebug() || isVerbose()) {
                logger.warn("Debug Mode activated.");
                logger.warn("Check your log Folder for categorized debug messages.");
            }

        } catch (IOException e) {
            LogUtils.error(logger, "Couldn't load YAML configuration!");
            throw new ConfigurationException("Couldn't load YAML configuration!");
        }

        // BiomeGroups Config
        // don't use defaults here
        GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
        try {
            biomeGroupsFile = YamlDocument.create(new File(pluginFolder, "BiomeGroups.yml"),
                    Objects.requireNonNull(rpgInstance.getResource("BiomeGroups.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            logger.info("BiomeGroups.yml loaded.");

        } catch (IOException e) {
            LogUtils.error(logger, "Couldn't load BiomeGroups YAML configuration!");
            throw new ConfigurationException("Couldn't load BiomeGroups YAML configuration!");
        }

        // GrowthModifiers Config
        try {
            growthModifiersFile = YamlDocument.create(new File(pluginFolder, "GrowthModifiers.yml"),
                    Objects.requireNonNull(rpgInstance.getResource("GrowthModifiers.yml")),
                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            logger.info("GrowthModifiers.yml loaded.");

        } catch (IOException e) {
            LogUtils.error(logger, "Couldn't load GrowthModifiers YAML configuration!");
            throw new ConfigurationException("Couldn't load GrowthModifiers YAML configuration!");
        }

    }

    /**
     * This Method copies default language files into the "lang" directory during plugin initialization.
     * This method is executed only once, at the start of the plugin and during reloads.
     * It supports custom language files in the "lang" directory and selects "en-US" as the default language
     * if the language code specified in the Config.yml cannot be resolved.
     */
    private void registerSupportedLanguages() {

        LogUtils.debug(logger, "Language Folder: " + languageFolder);

        // Copies all supported language files into the lang directory.
        try {
            for (String languageCode : SUPPORTED_LANGUAGE_CODES) {
                LogUtils.debug(logger, "Language: " + languageCode);

                if (languageCode.equalsIgnoreCase(getLanguageCode())) {
                    LogUtils.debug(logger, "Loading selected language File: " + languageFolder + File.separator + languageCode + ".yml");

                    selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(rpgInstance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

                } else {
                    LogUtils.debug(logger, "Loading language File: " + languageFolder + File.separator + languageCode + ".yml");
                    YamlDocument temp = YamlDocument.create(new File(languageFolder + File.separator, languageCode + ".yml"),
                            Objects.requireNonNull(rpgInstance.getResource("lang/" + languageCode + ".yml")),
                            GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
                    temp.update();
                }

                LogUtils.debug(logger, languageCode + ".yml loaded.");

            }
        } catch (IOException e) {
            LogUtils.error(logger, "Couldn't load language files!", e);
            throw new ConfigurationException("Couldn't load language files!");
        }

        // Search for custom files in lang directory.
        if (selectedLanguageFile == null) {
            // List all files from language directory
            File[] allFiles = languageFolder.listFiles();

            if (allFiles == null) {
                LogUtils.error(logger, "Couldn't load language files!");
                throw new ConfigurationException("Couldn't load language files!");
            }

            GeneralSettings gs = GeneralSettings.builder().setUseDefaults(false).build();
            logger.info("Searching for custom language files...");
            for (File file : allFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.equalsIgnoreCase(getLanguageCode() + ".yml")) {
                        try {
                            selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, getLanguageCode() + ".yml"),
                                    gs, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                                    UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
                            logger.info("{} loaded.", fileName);
                        } catch (IOException e) {
                            logger.warn("Couldn't load getLanguageCode(): {}", getLanguageCode());
                        }

                    }
                }
            }

        }

        // Setting the default language if selected 'getLanguageCode()' file not found.
        if (selectedLanguageFile == null) {
            try {
                logger.warn("No custom language file with getLanguageCode() '{}' located in 'lang' directory!", getLanguageCode());
                logger.warn("Using default language file: en-US");
                selectedLanguageFile = YamlDocument.create(new File(languageFolder + File.separator, "en-US.yml"),
                        Objects.requireNonNull(rpgInstance.getResource("lang/" + "en-US.yml")),
                        GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                        UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
            } catch (IOException e) {
                LogUtils.error(logger, "Couldn't load custom language file!", e);
                throw new ConfigurationException("Couldn't load custom language file!");
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
            logger.info("Language files loaded.");
            logger.info("Selected language: {}", getLanguageCode());

        } catch (IOException e) {
            LogUtils.error(logger, "Couldn't load selected language file!", e);
            throw new ConfigurationException("Couldn't load selected language file!");
        }
    }



    /**
     * Reloads all configuration files when the plugin is in a reloading state.
     * Reads values from the configuration files and updates global fields accordingly.
     * This method reloads the main configuration file (Config.yml), as well as other YAML files
     * responsible for growth modifiers (GrowthModifiers.yml), biome groups (BiomeGroups.yml),
     * and language settings (Language files).
     */
    public void reloadAllYAMLFiles() {
        logger.warn("Reloading config and language files...");
        try {
            config.reload();
            LogUtils.debug(logger, "Config.yml reloaded.");

            growthModifiersFile.reload();
            LogUtils.debug(logger, "GrowthModifiers.yml reloaded.");

            biomeGroupsFile.reload();
            LogUtils.debug(logger, "BiomeGroups.yml reloaded.");

            selectedLanguageFile.reload();
            LogUtils.debug(logger, "Language files reloaded.");

            // Get updated config data and store new data in global variables.
            registerSupportedLanguages();
            registerSelectedLanguage();

            //TODO: Verify Config Files

            LogUtils.info(logger, Component.text("All configuration and language files reloaded.").color(NamedTextColor.GREEN));


        } catch (YAMLException | IOException e) {
            logger.info(e.getLocalizedMessage());
            LogUtils.error(logger, "Error while reloading config files.", e);
            throw new ConfigurationException("Error while reloading config files.");
        }
    }
    

    public void dumpConfigData() {
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== Config.yml =====");
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, config.dump());
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== BiomeGroups.yml =====");
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, biomeGroupsFile.dump());
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== GrowthModifiers.yml =====");
        LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, growthModifiersFile.dump());
    }

    // Getters

    public YamlDocument getConfig() {
        return config;
    }

    public YamlDocument getBiomeGroupsFile() {
        return biomeGroupsFile;
    }

    public YamlDocument getGrowthModifiersFile() {
        return growthModifiersFile;
    }

    public YamlDocument getSelectedLanguageFile() {
        return selectedLanguageFile;
    }

    public String getLanguageCode() {
        return config.getString(MainConfigPath.GENERAL_LANGUAGE_CODE.getPath());
    }

    public boolean isDebug() {
        return config.getBoolean(MainConfigPath.LOGGING_DEBUG_LOG.toString());
    }

    public boolean isVerbose() {
        return config.getBoolean(MainConfigPath.VERBOSE.toString());
    }

}