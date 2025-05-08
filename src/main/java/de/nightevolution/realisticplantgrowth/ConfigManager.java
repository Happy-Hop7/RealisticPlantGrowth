package de.nightevolution.realisticplantgrowth;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.enums.ConfigPath;
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
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConfigManager handles loading, updating, and accessing all configuration and language files
 * used by the RealisticPlantGrowth plugin.
 * <p>
 * This implementation uses a thread-safe approach with read-write locks to ensure
 * safe access to configuration data across multiple threads, making it suitable for
 * multithreaded server environments like Folia.
 */
public class ConfigManager {

    // Core plugin instance and logger
    private final RealisticPlantGrowth rpgPlugin;
    private org.apache.logging.log4j.Logger logger;

    // Paths to important plugin folders
    private final File pluginFolder;
    private final File languageFolder;

    // Thread safety mechanism
    private final ReadWriteLock configLock = new ReentrantReadWriteLock();

    // YAML documents managed by this class - marked volatile for visibility across threads
    private YamlDocument config;
    private YamlDocument biomeGroupsFile;
    private YamlDocument growthModifiersFile;
    private YamlDocument selectedLanguageFile;

    // List of officially supported language codes, in order of fallback preference.
    // The first entry of the List ("en-US") is treated as the default.
    private static final List<String> SUPPORTED_LANGUAGE_CODES = List.of(
            "en-US", "de-DE", "fi-FI", "ru-RU", "zh-Hant"
    );

    private static final String configVersionString = "config-version";
    private static final String languageVersionString = "version";

    private static final String configName = "Config.yml";
    private static final String biomeGroupsName = "BiomeGroups.yml";
    private static final String growthModifiersName = "GrowthModifiers.yml";

    /**
     * Creates a new ConfigManager instance.
     *
     * @param rpgPlugin The plugin instance that this manager belongs to
     */
    public ConfigManager(RealisticPlantGrowth rpgPlugin) {
        this.rpgPlugin = rpgPlugin;
        this.pluginFolder = rpgPlugin.getDataFolder();
        this.languageFolder = new File(pluginFolder, "lang");
        this.logger = LogUtils.getLogger(this.getClass());

        createLanguageFolder();

        logger.info("Loading plugin files ...");
        loadConfigs();
    }

    /**
     * Ensures the language folder exists, creating it if necessary.
     * Throws ConfigurationException if the folder cannot be created.
     */
    private void createLanguageFolder() {
        if (!languageFolder.exists()) {
            LogUtils.verbose(logger, "Language directory doesn't exist!");
            LogUtils.verbose(logger, "Creating new directory...");

            if (languageFolder.mkdirs()) {
                LogUtils.verbose(logger, "New language directory created.");
            } else {
                String errorMsg = "Failed to create language directory: " + languageFolder.getAbsolutePath();
                LogUtils.error(logger, errorMsg);
                throw new ConfigurationException(errorMsg);
            }

        } else
            LogUtils.verbose(logger, "Language directory already exist.");
    }

    /**
     * Loads all core configuration files, sets logging modes, and loads language files.
     */
    private void loadConfigs() {
        configLock.writeLock().lock();
        try {
            LogUtils.verbose(logger, "Loading Config.yml ...");
            this.config = loadYaml(configName, false, true, true, configVersionString);

            // Set logging levels and update logger
            LogUtils.setDebug(isDebug());
            LogUtils.setVerbose(isVerbose());
            logger = LogUtils.getLogger(this.getClass());

            LogUtils.verbose(logger, "Loading BiomeGroups data...");
            this.biomeGroupsFile = loadYaml(biomeGroupsName, false, false, true, null);

            LogUtils.verbose(logger, "Loading GrowthModifiers ...");
            this.growthModifiersFile = loadYaml(growthModifiersName, false, false,true, null);

            LogUtils.verbose(logger, "Loading supported languages ...");
            loadLanguageFiles();
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Loads a YamlDocument from the file system.
     *
     * @param fileName     Name of the YAML file (with or without path).
     * @param fromLangDir  Whether the file is located in the language directory.
     * @param useDefaults  Whether to enable default values from the resource.
     * @param hasResource  Whether a corresponding resource exists inside the plugin jar.
     * @param versionKey   Optional YAML versioning key (null to disable).
     * @return Loaded YamlDocument instance.
     */
    private YamlDocument loadYaml(String fileName, boolean fromLangDir, boolean useDefaults, boolean hasResource, String versionKey) {

        fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";

        File baseFolder = fromLangDir ? languageFolder : pluginFolder;
        File targetFile = new File(baseFolder, fileName);

        // Adjust resource path if needed
        String resourcePath = fileName;
        if (fromLangDir) {
            resourcePath = "lang/" + fileName;
        }

        try (InputStream resourceStream = rpgPlugin.getResource(resourcePath)) {
            LogUtils.verbose(logger, "Loading YAML file: " + fileName);

            GeneralSettings generalSettings = GeneralSettings.builder()
                    .setUseDefaults(useDefaults)
                    .build();

            UpdaterSettings updaterSettings = (versionKey != null)
                    ? UpdaterSettings.builder().setVersioning(new BasicVersioning(versionKey)).build()
                    : UpdaterSettings.DEFAULT;

            YamlDocument yamlDocument = (resourceStream != null)
                    ? YamlDocument.create(targetFile, resourceStream, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, updaterSettings)
                    : YamlDocument.create(targetFile, generalSettings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, updaterSettings);

            LogUtils.verbose(logger,"Successfully loaded YAML: " + targetFile.getName());
            yamlDocument.update();
            return yamlDocument;

        } catch (IOException | NullPointerException e) {
            String msg = "Couldn't load YAML file: " + fileName;
            LogUtils.error(logger, msg, e);
            throw new ConfigurationException(msg);
        }
    }


    /**
     * Loads the selected language file as well as all supported languages.
     * Falls back to "en-US" if the selected one cannot be found or loaded.
     */
    private void loadLanguageFiles() {
        String selectedCode = getLanguageCode();
        boolean selectedLoaded = false;

        // Load all supported language files
        for (String langCode : SUPPORTED_LANGUAGE_CODES) {
            boolean isSelected = langCode.equalsIgnoreCase(selectedCode);
            try {
                YamlDocument langFile = loadYaml(langCode, true, true, true, languageVersionString);
                langFile.update();
                if (isSelected) {
                    selectedLanguageFile = langFile;
                    selectedLoaded = true;
                }
            } catch (IOException e) {
                LogUtils.error(logger, "Could not load language file: " + langCode, e);
            }
        }

        // Check for user-provided custom language file
        if (!selectedLoaded) {
            File[] customFiles = languageFolder.listFiles();
            if (customFiles != null) {
                LogUtils.verbose(logger,"Searching for custom language files...");
                for (File file : customFiles) {
                    if (file.isFile() && file.getName().equalsIgnoreCase(selectedCode)) {
                        selectedLanguageFile = loadYaml(file.getName(), true, false, false, null);
                        selectedLoaded = true;

                        logger.warn("Custom language file '{}' loaded (not officially supported).", file.getName());
                        logger.warn("Using custom files is not recommended — missing strings may occur after plugin updates.");
                        logger.warn("If you'd like this language to be officially supported, please open an issue on GitHub and attach your .yml file.");

                        break;
                    }
                }
            }
        }

        // Fallback to en-US
        if (!selectedLoaded) {
            logger.warn("Language file for code '{}' not found in the 'lang' directory.", getLanguageCode());
            logger.warn("Falling back to default language: '{}'.", SUPPORTED_LANGUAGE_CODES.getFirst());
            selectedLanguageFile = loadYaml(SUPPORTED_LANGUAGE_CODES.getFirst(), true, true, true, languageVersionString);
        }

        try {
            selectedLanguageFile.update();
            logger.info("Selected language file: {}", selectedLanguageFile.getFile().getName());
        } catch (IOException e) {
            throw new ConfigurationException("Could not update selected language file.", e);
        }
    }

    /**
     * Reloads all configuration and language files.
     * Uses write lock to ensure thread safety during reload.
     */
    public void reloadAllYAMLFiles() {
        logger.warn("Reloading config and language files...");
        configLock.writeLock().lock();
        try {
            config.reload();
            config.update();

            // Set logging levels and update logger
            LogUtils.setDebug(isDebug());
            LogUtils.setVerbose(isVerbose());
            logger = LogUtils.getLogger(this.getClass());

            biomeGroupsFile.reload();
            biomeGroupsFile.update();

            growthModifiersFile.reload();
            growthModifiersFile.update();

            selectedLanguageFile.reload();
            selectedLanguageFile.update();

            loadLanguageFiles(); // Re-select correct language

            LogUtils.info(logger, Component.text("All configuration and language files reloaded.").color(NamedTextColor.GREEN));
        } catch (IOException | YAMLException e) {
            LogUtils.error(logger, "Error reloading config files", e);
            throw new ConfigurationException("Error reloading config files.", e);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Dumps the raw YAML contents of all major config files to a log file.
     * Uses read lock to ensure thread safety.
     */
    public void dumpConfigData() {
        configLock.readLock().lock();
        try {
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== " + configName + " =====");
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, config.dump());
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== " + biomeGroupsName + " =====");
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, biomeGroupsFile.dump());
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== " + growthModifiersName + " =====");
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, growthModifiersFile.dump());
        } finally {
            configLock.readLock().unlock();
        }
    }

    // ----------------------------
    // Thread-safe Getters
    // ----------------------------

    /**
     * Thread-safe access to main configuration document
     * @return Main configuration document
     */
    public YamlDocument getConfig() {
        configLock.readLock().lock();
        try {
            return config;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to biome groups configuration
     * @return Biome groups configuration document
     */
    public YamlDocument getBiomeGroupsFile() {
        configLock.readLock().lock();
        try {
            return biomeGroupsFile;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to growth modifiers configuration
     * @return Growth modifiers configuration document
     */
    public YamlDocument getGrowthModifiersFile() {
        configLock.readLock().lock();
        try {
            return growthModifiersFile;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to language file
     * @return Currently selected language file
     */
    public YamlDocument getSelectedLanguageFile() {
        configLock.readLock().lock();
        try {
            return selectedLanguageFile;
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to language code
     * @return The configured language code, or the default ("en-US") if none is set.
     */
    public String getLanguageCode() {
        configLock.readLock().lock();
        try {
            if (config == null) {
                return SUPPORTED_LANGUAGE_CODES.getFirst();
            }

            String configuredCode = config.getString(ConfigPath.GENERAL_LANGUAGE_CODE.getPath());
            return (configuredCode != null) ? configuredCode : SUPPORTED_LANGUAGE_CODES.getFirst();

        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to debug setting
     * @return True if debug logging is enabled
     */
    public boolean isDebug() {
        configLock.readLock().lock();
        try {
            return config != null && config.getBoolean(ConfigPath.LOGGING_DEBUG_LOG.getPath());
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe access to verbose setting
     * @return True if verbose logging is enabled
     */
    public boolean isVerbose() {
        configLock.readLock().lock();
        try {
            return config != null && config.getBoolean(ConfigPath.VERBOSE.getPath());
        } finally {
            configLock.readLock().unlock();
        }
    }
}