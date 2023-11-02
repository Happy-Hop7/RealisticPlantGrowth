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
    private static boolean verbose = false;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private BukkitAudiences adventure;

    private Logger logger;

    @Override
    //TODO: Add Startup Messages
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        // Initialize an audiences instance for the plugin
        this.adventure = BukkitAudiences.create(this);

        this.configManager = ConfigManager.get();

        verbose = configManager.isVerbose();
        logger = new Logger(this.getClass().getTypeName(), this, verbose);

        registerCommands();
        registerTabCompleter();

        logger.log("");
        logger.log("&2Plugin successfully enabled.");
        logger.log("");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }


    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the Bukkit plugin manager.
     */
    void disablePlugin(){
        Bukkit.getLogger().warning(Color.RED + "Disabling Plugin...");
        getServer().getPluginManager().disablePlugin(this);
    }

    private void registerCommands(){
        instance.getCommand("rpg").setExecutor(CommandManager.get());
    }
    private void registerTabCompleter(){
        instance.getCommand("rpg").setTabCompleter(new TabCompleterImpl());
    }


    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(classPrefix + "Tried to access Adventure API when the plugin was disabled!");
        }
        return this.adventure;
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
