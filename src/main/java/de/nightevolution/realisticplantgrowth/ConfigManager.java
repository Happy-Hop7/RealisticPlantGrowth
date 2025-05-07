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
    private volatile YamlDocument config;
    private volatile YamlDocument biomeGroupsFile;
    private volatile YamlDocument growthModifiersFile;
    private volatile YamlDocument selectedLanguageFile;

    // List of officially supported language codes
    private static final List<String> SUPPORTED_LANGUAGE_CODES = List.of(
            "de-DE", "en-US", "fi-FI", "ru-RU", "zh-Hant"
    );

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
        loadConfigs();
    }

    /**
     * Ensures the language folder exists, creating it if necessary.
     * Throws ConfigurationException if the folder cannot be created.
     */
    private void createLanguageFolder() {
        if (!languageFolder.exists() && !languageFolder.mkdirs()) {
            String error = "Failed to create language directory: " + languageFolder.getAbsolutePath();
            LogUtils.error(logger, error);
            throw new ConfigurationException(error);
        }
    }

    /**
     * Loads all core configuration files, sets logging modes, and loads language files.
     */
    private void loadConfigs() {
        configLock.writeLock().lock();
        try {
            this.config = loadYaml("Config.yml", true, true, "config-version");

            // Set logging levels and update logger
            LogUtils.setDebug(isDebug());
            LogUtils.setVerbose(isVerbose());
            logger = LogUtils.getLogger(this.getClass());

            this.biomeGroupsFile = loadYaml("BiomeGroups.yml", false, true, null);
            this.growthModifiersFile = loadYaml("GrowthModifiers.yml", false, true, null);

            loadLanguageFiles();
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Loads a YamlDocument from the file system or bundled resources.
     *
     * @param fileName    Name of the file
     * @param useDefaults Whether to enable default YAML values
     * @param hasResource Whether the file should be loaded from the plugin jar
     * @param versionKey  Optional YAML key for versioning/updating
     * @return YamlDocument instance
     */
    private YamlDocument loadYaml(String fileName, boolean useDefaults, boolean hasResource, String versionKey) {
        try {
            File file = new File(fileName.endsWith(".yml") ? pluginFolder : languageFolder, fileName);
            return YamlDocument.create(
                    file,
                    hasResource ? Objects.requireNonNull(rpgPlugin.getResource(
                            fileName.contains("/") ? fileName : fileName.startsWith("lang/") ? fileName : "lang/" + fileName
                    )) : null,
                    useDefaults ? GeneralSettings.DEFAULT : GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    versionKey != null ? UpdaterSettings.builder().setVersioning(new BasicVersioning(versionKey)).build() : UpdaterSettings.DEFAULT
            );
        } catch (IOException e) {
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
                YamlDocument langFile = loadYaml(langCode + ".yml", true, true, "version");
                langFile.update();
                if (isSelected) {
                    selectedLanguageFile = langFile;
                    selectedLoaded = true;
                }
            } catch (IOException e) {
                logger.warn("Could not load language file: {}", langCode);
                logger.error("Could not load language file: {}", langCode, e);
            }
        }

        // Check for user-provided custom language file
        if (!selectedLoaded) {
            File[] customFiles = languageFolder.listFiles();
            if (customFiles != null) {
                for (File file : customFiles) {
                    if (file.isFile() && file.getName().equalsIgnoreCase(selectedCode + ".yml")) {
                        selectedLanguageFile = loadYaml(file.getName(), false, false, "version");
                        selectedLoaded = true;
                        break;
                    }
                }
            }
        }

        // Fallback to en-US
        if (!selectedLoaded) {
            logger.warn("Falling back to default language: en-US");
            selectedLanguageFile = loadYaml("en-US.yml", true, true, "version");
        }

        try {
            selectedLanguageFile.update();
        } catch (IOException e) {
            throw new ConfigurationException("Could not update selected language file.", e);
        }

        logger.info("Language file loaded: {}", selectedCode);
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

            // Set logging levels and update logger
            LogUtils.setDebug(isDebug());
            LogUtils.setVerbose(isVerbose());
            logger = LogUtils.getLogger(this.getClass());

            biomeGroupsFile.reload();
            growthModifiersFile.reload();
            selectedLanguageFile.reload();
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
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== Config.yml =====");
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, config.dump());
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== BiomeGroups.yml =====");
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, biomeGroupsFile.dump());
            LogUtils.logToFileAsync(LogUtils.LogFile.CONFIG_DUMP, "===== GrowthModifiers.yml =====");
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
     * @return Language code selected in the main config, or "en-US" if not set
     */
    public String getLanguageCode() {
        configLock.readLock().lock();
        try {
            String code = config != null ? config.getString(MainConfigPath.GENERAL_LANGUAGE_CODE.getPath()) : null;
            return code != null ? code : "en-US";
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
            return config != null && config.getBoolean(MainConfigPath.LOGGING_DEBUG_LOG.getPath());
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
            return config != null && config.getBoolean(MainConfigPath.VERBOSE.getPath());
        } finally {
            configLock.readLock().unlock();
        }
    }
}