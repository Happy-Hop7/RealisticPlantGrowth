package de.nightevolution.realisticplantgrowth;

import de.nightevolution.realisticplantgrowth.commands.TabCompleterImpl;
import de.nightevolution.realisticplantgrowth.listeners.other.*;
import de.nightevolution.realisticplantgrowth.listeners.plant.*;
import de.nightevolution.realisticplantgrowth.listeners.player.*;
import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.MetricsHandler;
import de.nightevolution.realisticplantgrowth.utils.version.ServerEnvironmentChecker;
import de.nightevolution.realisticplantgrowth.utils.version.UpdateChecker;
import de.nightevolution.realisticplantgrowth.utils.biome.BiomeChecker;
import de.nightevolution.realisticplantgrowth.utils.enums.ConfigPath;
import de.nightevolution.realisticplantgrowth.utils.version.mapper.VersionMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
    private static ConfigManagerOld cmOld;

    /**
     * The {@link ConfigManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private ConfigManager cm;

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
        LogUtils.initialize(this, false, false);

        cm = new ConfigManager(this);
        logger = LogUtils.getLogger(this.getClass());


//        ServerEnvironmentChecker serverEnvironmentChecker = new ServerEnvironmentChecker(pluginVersion);
//        versionMapper = serverEnvironmentChecker.checkVersion();
//        if (versionMapper != null) {
//            logger.info("Version check passed.");
//        } else {
//            logger.error("Server version not supported!");
//            disablePlugin();
//        }
//
//        isPaperFork = serverEnvironmentChecker.checkFork();
//        if(!isPaperFork) {
//            logger.error("Server fork not supported!");
//            disablePlugin();
//        }

        // Starts bStats metrics, if enabled in the config
        new MetricsHandler(this, cm);

        // Check for plugin updates
        if (cm.getConfig().getBoolean(ConfigPath.PLUGIN_UPDATES_CHECK_FOR_UPDATES.getPath())) {
            updateChecker = new UpdateChecker(this, cm.getConfig().getInt(ConfigPath.PLUGIN_UPDATES_INTERVAL_HOURS.getPath()));
        }

//        updateVariables();

        drawLogo();

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

        LogUtils.setVerbose(cm.isVerbose());
        LogUtils.setDebug(cm.isDebug());

        cmdManager = new CommandManager();

        registerCommands();
        registerTabCompleter();
        BiomeChecker.clearCache();
        registerListeners();

        if (updateChecker != null) {
            updateChecker.cancelScheduledTask();
            updateChecker = null;
        }

        if (cm.getConfig().getBoolean(ConfigPath.PLUGIN_UPDATES_CHECK_FOR_UPDATES.getPath())) {
            updateChecker = new UpdateChecker(this, cm.getConfig().getInt(ConfigPath.PLUGIN_UPDATES_INTERVAL_HOURS.getPath()));
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
    @Deprecated(forRemoval = true)
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
    @Deprecated(forRemoval = true)
    @NotNull
    public ConfigManagerOld getConfigManager() {
        return ConfigManagerOld.get();
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
    @Deprecated(forRemoval = true)
    public boolean isWorldDisabled(@NotNull World world) {
        return true;
    }


    /**
     * Checks whether the debug mode is enabled.
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    @Deprecated(forRemoval = true)
    public static boolean isDebug() {
        return true;
    }

    /**
     * Checks whether the verbose mode is enabled.
     *
     * @return {@code true} if verbose mode is enabled, {@code false} otherwise.
     */
    @Deprecated(forRemoval = true)
    public static boolean isVerbose() {
        return true;
    }

}
