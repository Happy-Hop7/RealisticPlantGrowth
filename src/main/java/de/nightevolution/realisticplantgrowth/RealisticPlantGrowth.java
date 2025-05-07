package de.nightevolution.realisticplantgrowth;

import de.nightevolution.realisticplantgrowth.commands.TabCompleterImpl;
import de.nightevolution.realisticplantgrowth.listeners.other.*;
import de.nightevolution.realisticplantgrowth.listeners.plant.*;
import de.nightevolution.realisticplantgrowth.listeners.player.*;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.UpdateChecker;
import de.nightevolution.realisticplantgrowth.utils.biome.BiomeChecker;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.utils.mapper.versions.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
public class RealisticPlantGrowth extends JavaPlugin {

    /**
     * The main class for the {@link RealisticPlantGrowth} plugin, serving as a singleton instance.
     */
    private static RealisticPlantGrowth instance;

    /**
     * The {@link ConfigManagerOld} used by the {@link RealisticPlantGrowth} plugin.
     */
    private static ConfigManagerOld cm;

    /**
     * The {@link VersionMapper} used by the {@link RealisticPlantGrowth} plugin.
     */
    private static VersionMapper versionMapper;

    /**
     * The {@link org.slf4j.Logger} instance for recording log messages in the {@link RealisticPlantGrowth} plugin.
     */
    private org.apache.logging.log4j.Logger logger;

    /**
     * The {@link CommandManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private CommandManager cmdManager;

    /**
     * The {@link UpdateChecker} used by the {@link RealisticPlantGrowth} plugin.
     */
    private UpdateChecker updateChecker;

    /**
     * The {@link Metrics} instance for collecting anonymous usage data for the {@link RealisticPlantGrowth} plugin.
     */
    private Metrics metrics;

    /**
     * The name of the debug log file used by the {@link RealisticPlantGrowth} plugin.
     */
    private static final String logFile = "debug";

    private String pluginVersion;
    private boolean isPaperFork;

    @Override
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;
        this.pluginVersion = this.getDescription().getVersion();

        // new paper way to do this:
        //this.pluginVersion = getPluginMeta().getVersion();

        LogUtils.initialize(this.getDataFolder(), true, true);
        logger = LogUtils.getLogger(this.getClass());

        logger.info("Info1");
        logger.warn("Warning1");
        logger.error("Error1");


//        try {
//            cm = ConfigManagerOld.get();
//        } catch (ConfigurationException e) {
//            disablePlugin();
//            return;
//        }
//
//        logger = new Logger(this.getClass().getSimpleName(), cm.isVerbose(), cm.isDebug_log());
//
//
//        checkServerFork();
//
//        if (checkServerVersion()) {
//            logger.info("Version check passed.");
//        } else {
//            logger.error("Server version not supported!");
//            disablePlugin();
//        }
//        updateVariables();
//        registerMetrics();
        drawLogo();
    }


    /**
     * Checks the server implementation to determine if it is running on a Paper or Spigot server.
     * <p>
     * This method attempts to load a Paper-specific class. If the class is found, it indicates that
     * the server is a Paper fork, and the corresponding flag is set. If the class cannot be found,
     * it assumes the server is running on Spigot or another non-Paper implementation.
     * </p>
     */
    private void checkServerFork() {
        try {
            logger.info("Checking server version...");
            // Attempt to load a Paper-specific class to verify if running on a Paper fork
            Class.forName("io.papermc.paper.util.Tick");
            isPaperFork = true;
            logger.info("... using Paper implementation.");
        } catch (ClassNotFoundException ignored) {
            isPaperFork = false;
            logger.info("... using Spigot implementation.");
        }
    }

    /**
     * Checks the server version and initializes the appropriate {@link VersionMapper}.
     * <p>
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

            if (versionString.length >= 3) {
                microReleaseVersion = Integer.parseInt(versionString[2]);
            } else {
                microReleaseVersion = 0;
            }

            logger.info("Your server is running version 1.{}.{}", minorReleaseVersion, microReleaseVersion);

        } catch (ArrayIndexOutOfBoundsException | NumberFormatException whatVersionAreYouUsingException) {
            LogUtils.error(logger, "Error extracting server version: Unable to parse Bukkit version format.");
            return false;
        }

        // Warn if the server version is a snapshot version
        if (pluginVersion.contains("SNAPSHOT")) {
            logger.warn("You are using a snapshot version of RealisticPlantGrowth!");
        }

        // Version below Minecraft 1.20.1 are not supported (due to createBlockState API change).
        if (minorReleaseVersion < 20 || (minorReleaseVersion == 20 && microReleaseVersion == 0)) {
            logger.error("Unsupported server version: This plugin requires Minecraft 1.20.1 or higher.");
            return false;
        }

        // Assign the correct VersionMapper based on the server version
        if (minorReleaseVersion == 20 && microReleaseVersion <= 3) {
            versionMapper = new Version_1_20();
            logger.info("Implementation initialized for Minecraft 1.20.1 - 1.20.3.");
        }

        // Version 1.20.4 - 1.21.3
        if (minorReleaseVersion <= 21 && microReleaseVersion <= 3) {
            versionMapper = new Version_1_20_4();
            logger.info("Implementation initialized for Minecraft 1.20.4 - 1.21.3.");
        }

        // Version >= 1.21.4
        else {
            versionMapper = new Version_1_21_4();
            logger.info("Implementation initialized for Minecraft 1.21.4 and above.");
        }

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
        new BlockBreakListener(instance);
        new BlockFromToListener(instance);
        new BlockGrowListener(instance);
        new BlockPistonListener(instance);
        new BlockSpreadListener(instance);
        new BonemealListener(instance);
        new HopperCompostListener(instance);
        new PlayerInteractListener(instance);
        new PlayerQuitListener(instance);
        new StructureGrowListener(instance);
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
            logger.info("bStats enabled.");

            // Create a new Metrics instance with the plugin and the unique plugin ID (20634)
            metrics = new Metrics(this, 20634);
        } else
            logger.info("bStats disabled.");
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
     * This method refreshes the {@link ConfigManagerOld}, {@link MessageManager}, logging settings,
     * and various cached data used by the plugin.
     */
    public void updateVariables() {

        LogUtils.setVerbose(cm.isVerbose());
        LogUtils.setDebug(cm.isDebug_log());

        cmdManager = new CommandManager();

        registerCommands();
        registerTabCompleter();
        BiomeChecker.clearCache();
        registerListeners();

        //TODO: Read Update Interval from ConfigManagerOld
        if (updateChecker != null) {
            updateChecker.cancelScheduledTask();
            updateChecker = null;
        }

        if (cm.check_for_updates()) {
            updateChecker = new UpdateChecker(12);
        }

    }


    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the {@link Bukkit} {@link PluginManager}.
     */
    void disablePlugin() {
        instance = null;
        cm = null;
        versionMapper = null;
        logger = null;
        cmdManager = null;
        metrics = null;
        pluginVersion = null;

        if (updateChecker != null) {
            updateChecker.cancelScheduledTask();
            updateChecker = null;
        }

        HandlerList.unregisterAll(this);
        Objects.requireNonNull(this.getCommand("rpg")).setTabCompleter(null);
        Objects.requireNonNull(this.getCommand("rpg")).setExecutor(null);
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        LogUtils.shutdown();
    }


    private void drawLogo() {
        final TextComponent logo = Component.text().appendNewline()
                .appendNewline().append(Component.text("     .{{}}}}}}.", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text("    {{{{{{(`)}}}.", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text("   {{{(`)}}}}}}}}}", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text("  }}}}}}}}}{{(`){{{", NamedTextColor.DARK_GREEN))
                                .append(Component.text("     Realistic", NamedTextColor.AQUA))
                                .append(Component.text(" Plant", NamedTextColor.GREEN))
                                .append(Component.text(" Growth", NamedTextColor.AQUA))
                .appendNewline().append(Component.text("  }}}}{{{{(`)}}{{{{", NamedTextColor.DARK_GREEN))
                                .append(Component.text("       by", NamedTextColor.AQUA))
                                .append(Component.text(" TheRealPredator", NamedTextColor.LIGHT_PURPLE))
                .appendNewline().append(Component.text(" {{{(`)}}}}}}}{}}}}}", NamedTextColor.DARK_GREEN))
                                .append(Component.text("         v" + pluginVersion, NamedTextColor.GOLD))
                .appendNewline().append(Component.text("{{{{{{{{(`)}}}}}}}}}}", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text("{{{{{{{}{{{{(`)}}}}}}", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text(" {{{{{(`", NamedTextColor.DARK_GREEN))
                                .append(Component.text(")   {", TextColor.color(121, 96, 76))
                                .append(Component.text("{{{(`)}'", NamedTextColor.DARK_GREEN))
                                .append(Component.text("    ... successfully enabled.", NamedTextColor.GREEN))
                .appendNewline().append(Component.text("  `\"\"'\"", NamedTextColor.DARK_GREEN))
                                .append(Component.text(" |   | ", TextColor.color(121, 96, 76)))
                                .append(Component.text("\"'\"'`", NamedTextColor.DARK_GREEN))
                .appendNewline().append(Component.text("       /     \\" , TextColor.color(121, 96, 76)))
                .appendNewline().append(Component.text("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", NamedTextColor.GREEN)))
                .build();

        LogUtils.info(logger, logo);

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
     * Checks if the server is running a Paper or Paper fork implementation.
     *
     * @return {@code true} if the server is identified as a Paper or Paper fork, <p>
     *         {@code false} if it is a Spigot or other non-Paper implementation.
     */
    public boolean isPaperFork(){
        return isPaperFork;
    }

    /**
     * Retrieves the configuration manager associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link ConfigManagerOld} instance managing plugin configurations.
     */
    @NotNull
    public ConfigManagerOld getConfigManager() {
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
