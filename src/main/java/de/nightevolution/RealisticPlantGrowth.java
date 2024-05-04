package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.listeners.BlockFromToListener;
import de.nightevolution.listeners.BlockPistonListener;
import de.nightevolution.listeners.plant.*;
import de.nightevolution.listeners.player.PlayerInteractListener;
import de.nightevolution.listeners.player.PlayerQuitListener;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.UpdateChecker;
import de.nightevolution.utils.biome.BiomeChecker;
import de.nightevolution.utils.mapper.VersionMapper;
import de.nightevolution.utils.mapper.versions.Version_1_20;
import de.nightevolution.utils.mapper.versions.Version_1_20_4;
import de.nightevolution.utils.rest.ModrinthVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The main class for the {@link RealisticPlantGrowth} plugin.
 * This class serves as the entry point for the plugin and handles initialization, configuration, and event listening.
 */
public final class RealisticPlantGrowth extends JavaPlugin {

    /**
     * The main class for the {@link RealisticPlantGrowth} plugin, serving as a singleton instance.
     */
    private static RealisticPlantGrowth instance;

    /**
     * The {@link ConfigManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private static ConfigManager cm;

    /**
     * The {@link VersionMapper} used by the {@link RealisticPlantGrowth} plugin.
     */
    private static VersionMapper versionMapper;

    /**
     * The {@link Logger} instance for recording log messages in the {@link RealisticPlantGrowth} plugin.
     */
    private Logger logger;

    /**
     * The {@link CommandManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private CommandManager cmdManager;

    /**
     * The {@link Metrics} instance for collecting anonymous usage data for the {@link RealisticPlantGrowth} plugin.
     */
    private Metrics metrics;

    /**
     * The name of the debug log file used by the {@link RealisticPlantGrowth} plugin.
     */
    private static final String logFile = "debug";

    private String pluginVersion;

    @Override
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;
        this.pluginVersion = this.getDescription().getVersion();

        cm = ConfigManager.get();

        logger = new Logger(this.getClass().getSimpleName(), cm.isVerbose(), cm.isDebug_log());

        if (checkServerVersion()) {
            logger.log("Version check passed.");
        } else {
            logger.error("Server version not supported!");
            disablePlugin();
        }

        updateVariables();
        registerMetrics();
        drawLogo();
    }

    /**
     * Checks the server version and initializes the appropriate {@link VersionMapper}.<p>
     * This method determines the server version by extracting it from the Bukkit server class package name.
     * It then sets the corresponding version mapper based on the extracted version.
     *
     * @return {@code true} if the version check and initialization are successful, {@code false} otherwise.
     */
    private boolean checkServerVersion() {

        int minorReleaseVersion;
        int microReleaseVersion;

        try {

            String[] versionString = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            minorReleaseVersion = Integer.parseInt(versionString[1]);
            microReleaseVersion = Integer.parseInt(versionString[2]);

            logger.log("Your server is running version 1." + minorReleaseVersion + "." + microReleaseVersion);

        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            // Unable to extract version, log the exception and return false
            logger.error("Error while extracting server version!");
            return false;
        }


        if (pluginVersion.contains("SNAPSHOT")) {
            logger.warn("You are using a snapshot version!");
        }

        // Version below Minecraft 1.20 are not supported.
        if (minorReleaseVersion < 20)
            return false;

        // Initialize version-specific mappers based on the detected version
        if (microReleaseVersion <= 3)
            versionMapper = new Version_1_20();

        else
            versionMapper = new Version_1_20_4();

        return true;
    }


    /**
     * Registers the primary command executor for the {@link RealisticPlantGrowth} plugin.
     * Associates the "{@code /rpg}" command with the corresponding {@link CommandManager}.
     */
    private void registerCommands() {
        Objects.requireNonNull(instance.getCommand("rpg")).setExecutor(cmdManager);
    }

    /**
     * Registers the tab completer for the {@link RealisticPlantGrowth} plugin's primary command.
     * Associates the "{@code /rpg}" command with the provided {@link TabCompleterImpl} implementation.
     */
    private void registerTabCompleter() {
        Objects.requireNonNull(instance.getCommand("rpg")).setTabCompleter(new TabCompleterImpl());
    }

    /**
     * Registers various event-listeners to handle plant growth, structure growth, block spread,
     * fertilization, block-breaking, player interactions, and player quit events.
     * Each listener is associated with the provided instance of the {@link RealisticPlantGrowth} plugin.
     */
    private void registerListeners() {
        new BlockGrowListener(instance);
        new StructureGrowListener(instance);
        new BlockSpreadListener(instance);
        new BlockFertilizeListener(instance);
        new BlockBreakListener(instance);
        new PlayerInteractListener(instance);
        new PlayerQuitListener(instance);
        new BlockFromToListener(instance);
        new BlockPistonListener(instance);
    }

    /**
     * Registers metrics using bStats if the corresponding configuration setting is enabled.
     * <p> bStats is a service that provides statistics and insights about plugin usage. </p>
     * <p> For more information about bStats, visit the bStats page:
     * <a href="https://bstats.org/plugin/bukkit/Realistic%20Plant%20Growth/20634">bStats Page</a>
     * </p>
     */
    private void registerMetrics() {
        // Check if the use_metrics configuration setting is enabled
        if (cm.use_metrics()) {
            // Log that bStats is enabled
            logger.log("bStats enabled.");

            // Create a new Metrics instance with the plugin and the unique plugin ID (20634)
            metrics = new Metrics(this, 20634);
        } else
            logger.log("bStats disabled.");
    }

    /**
     * Reloads the plugin by refreshing YAML files, unregistering event handlers, and updating variables.
     * This method is intended for use when reloading plugin configurations or making runtime adjustments.
     */
    public void reload() {
        cm.reloadAllYAMLFiles();
        versionMapper.reload();
        HandlerList.unregisterAll(instance);
        updateVariables();
    }

    /**
     * Updates the plugin variables and configurations based on the latest settings.
     * This method refreshes the {@link ConfigManager}, {@link MessageManager}, logging settings,
     * and various cached data used by the plugin.
     */
    public void updateVariables() {

        logger.setVerbose(cm.isVerbose());
        logger.setDebug(cm.isDebug_log());

        cmdManager = new CommandManager();

        registerCommands();
        registerTabCompleter();
        BiomeChecker.clearCache();
        registerListeners();
        checkForUpdates();

    }

    /**
     * Checks for updates of the {@link RealisticPlantGrowth} plugin.
     * This method uses an {@link UpdateChecker} to compare the current version
     * of the plugin with the latest available version and logs messages accordingly.
     */
    private void checkForUpdates() {
        new UpdateChecker().getVersion(version -> {
            ModrinthVersion thisPluginVersion = new ModrinthVersion();
            thisPluginVersion.setVersion_number(pluginVersion);

            if (thisPluginVersion.compareTo(version) >= 0) {
                // Log a message if there is no new update available.
                logger.log("Your RealisticPlantGrowth plugin is up to date (version " + pluginVersion + ").");
            } else {
                // Log messages if a new update is available.
                logger.warn("A new version of RealisticPlantGrowth is available!");
                logger.warn("Current version: " + pluginVersion);
                logger.warn("Latest version: " + version.getVersion_number());
                logger.warn("Download the latest version at:");
                logger.warn("https://modrinth.com/plugin/realistic-plant-growth/version/latest");
            }
        });
    }


    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the {@link Bukkit} {@link PluginManager}.
     */
    void disablePlugin() {
        logger.log("");
        logger.error("&cDisabling " + this.getClass().getSimpleName() + "...");
        logger.log("");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {

    }


    private void drawLogo() {
        String logo = System.lineSeparator() +
                System.lineSeparator() +
                "&2     .{{}}}}}}." + "&r" + System.lineSeparator() +
                "&2    {{{{{{(`)}}}." + "&r" + System.lineSeparator() +
                "&2   {{{(`)}}}}}}}}}" + "&r" + System.lineSeparator() +
                "&2  }}}}}}}}}{{(`){{{" + "&b     Realistic &aPlant &bGrowth" + "&r" + System.lineSeparator() +
                "&2  }}}}{{{{(`)}}{{{{" + "&b       by &6TheRealPredator" + "&r" + System.lineSeparator() +
                "&2 {{{(`)}}}}}}}{}}}}}" + "&r" + System.lineSeparator() +
                "&2{{{{{{{{(`)}}}}}}}}}}" + "&r" + System.lineSeparator() +
                "&2{{{{{{{}{{{{(`)}}}}}}" + "&a    ... successfully enabled." + "&r" + System.lineSeparator() +
                "&2 {{{{{(`&r)   {&2{{{(`)}'" + "&r" + System.lineSeparator() +
                "&2  `\"\"'\" &r|   | &2\"'\"'`" + "&r" + System.lineSeparator() +
                "       &r/     \\" + "&r" + System.lineSeparator() +
                "&a~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + "&r" + System.lineSeparator();
        logger.log(logo);

    }

    // Getters

    /**
     * Provides access to the singleton instance of the {@link RealisticPlantGrowth} plugin.
     *
     * @return The singleton instance of the {@link RealisticPlantGrowth} plugin.
     */
    @NotNull
    public static RealisticPlantGrowth getInstance() {
        return instance;
    }

    /**
     * Retrieves the configuration manager associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link ConfigManager} instance managing plugin configurations.
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return cm;
    }

    /**
     * Retrieves the message manager associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link MessageManager} instance handling plugin messages.
     */
    @NotNull
    public MessageManager getMessageManager() {
        return MessageManager.get();
    }

    /**
     * Retrieves the {@link VersionMapper} associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link VersionMapper} instance handling different minecraft versions.
     */
    @NotNull
    public VersionMapper getVersionMapper() {
        return versionMapper;
    }

    /**
     * Checks if plant growth modification is disabled for the specified world.
     *
     * @param world The {@link World} to check for plant growth modification.
     * @return {@code true} if growth modification is enabled for the world, {@code false} otherwise.
     */
    public boolean isWorldDisabled(@NotNull World world) {
        if (cm.getEnabled_worlds().contains(world.getName())) {
            return cm.isUse_enabled_worlds_as_world_blacklist();
        }
        return !cm.isUse_enabled_worlds_as_world_blacklist();
    }


    /**
     * Checks whether the debug mode is enabled.
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public static boolean isDebug() {
        return cm.isDebug_log();
    }

    /**
     * Checks whether the verbose mode is enabled.
     *
     * @return {@code true} if verbose mode is enabled, {@code false} otherwise.
     */
    public static boolean isVerbose() {
        return cm.isVerbose();
    }

}
