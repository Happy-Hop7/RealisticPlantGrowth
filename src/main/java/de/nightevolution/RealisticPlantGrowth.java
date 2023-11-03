package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.utils.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class RealisticPlantGrowth extends JavaPlugin {

    // For convenience, a reference to the instance of this plugin
    private static RealisticPlantGrowth instance;

    private static final String classPrefix = "RealisticPlantGrowth: ";

    // TODO: Set verbose and debug values of logger to false
    private static boolean verbose = true;
    private static boolean debug = true;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private BukkitAudiences bukkitAudiences;

    private Logger logger;

    @Override
    //TODO: Add Startup Messages
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        logger = new Logger(this.getClass().getSimpleName(), this, verbose, debug);

        // Initialize an audiences instance for the plugin
        this.bukkitAudiences = BukkitAudiences.create(this);

        this.configManager = ConfigManager.get();

        registerCommands();
        registerTabCompleter();

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
        instance.getCommand("rpg").setExecutor(CommandManager.get());
    }
    private void registerTabCompleter(){
        instance.getCommand("rpg").setTabCompleter(new TabCompleterImpl());
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
    public static boolean getDebugStatus() {
        return verbose;
    }

    public ConfigManager getConfigManager(){
        return this.configManager;
    }

    public MessageManager getMessageManager(){
        return this.messageManager;
    }


}
