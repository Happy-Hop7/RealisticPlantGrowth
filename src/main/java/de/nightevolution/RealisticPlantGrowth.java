package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.listeners.*;
import de.nightevolution.utils.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public final class RealisticPlantGrowth extends JavaPlugin {

    // For convenience, a reference to the instance of this plugin
    private static RealisticPlantGrowth instance;

    private static final String classPrefix = "RealisticPlantGrowth: ";

    private static boolean verbose = false;
    private static boolean debug = false;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private BukkitAudiences bukkitAudiences;

    private Logger logger;

    @Override
    //TODO: Add Startup Messages
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        // Initialize an audiences instance for the plugin
        this.bukkitAudiences = BukkitAudiences.create(this);

        this.configManager = ConfigManager.get();
        verbose = configManager.isVerbose();
        debug = configManager.isDebug_log();

        logger = new Logger(this.getClass().getSimpleName(), this, verbose, debug);

        registerCommands();
        registerTabCompleter();
        registerListeners();

        logger.log("");
        logger.log("&2" + this.getClass().getSimpleName() + "&2 successfully enabled.");
        logger.log("");


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }
    }


    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the Bukkit plugin manager.
     */
    void disablePlugin(){
        logger.log("");
        logger.error("&cDisabling " + this.getClass().getSimpleName() + "...");
        logger.log("");
        getServer().getPluginManager().disablePlugin(this);
    }

    private void registerCommands(){
        Objects.requireNonNull(instance.getCommand("rpg")).setExecutor(CommandManager.get());
    }
    private void registerTabCompleter(){
        Objects.requireNonNull(instance.getCommand("rpg")).setTabCompleter(new TabCompleterImpl());
    }

    private void registerListeners(){
        new BlockGrowListener(instance);
        new StructureGrowListener(instance);
        new PlayerListener(instance);
        new BlockSpreadListener(instance);
        new BlockFertilizeListener(instance);
    }


    public @NonNull BukkitAudiences getBukkitAudiences() {
        if (this.bukkitAudiences == null) {
            throw new IllegalStateException(classPrefix + "Tried to access Adventure API when the plugin was disabled!");
        }
        return this.bukkitAudiences;
    }

    // Getters

    public static RealisticPlantGrowth getInstance(){
        return instance;
    }
    public static boolean isVerbose() {
        return verbose;
    }
    public static boolean isDebug() {
        return debug;
    }
    public ConfigManager getConfigManager(){
        return this.configManager;
    }

    public MessageManager getMessageManager(){
        return this.messageManager;
    }


}
