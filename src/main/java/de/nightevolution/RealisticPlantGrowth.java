package de.nightevolution;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RealisticPlantGrowth extends JavaPlugin {

    // For convenience, a reference to the instance of this plugin
    private static RealisticPlantGrowth instance;
    // Plugin prefix is getting read from the YAML Config.
    // This is the default value.
    private static String pluginPrefix = "[RealisticPlantGrowth] ";
    private static String classPrefix = pluginPrefix + "RealisticPlantGrowth: ";
    private static boolean verboseMode = false;

    public static File dataFolder;

    private ConfigManager configManager;
    private MessageManager messageManager;

    // TODO: Check what of this Stuff is used
    // declare some stuffs to be used later
    public ArrayList<Integer> softBlocks = new ArrayList<Integer>();
    public static Boolean logEnabled;
    public static Boolean logTreeEnabled;
    public static Boolean logPlantEnabled;
    public static Boolean logBonemealEnabled;
    public static Boolean logCoords;
    public static Boolean logVerbose;
    public static Boolean blockWaterBucket;
    public static Boolean blockWaterDispenser;
    public static int naturalLight;
    public static List<String> darkGrow;
    public static List<String> enabledWorlds;
    public static List<String> plantTypes;
    public static List<String> seedTypes;
    static Random randomNumberGenerator = new Random();

    // TODO: What is this?
    // Weedkiller
    public static int wkradius;
    public static String weedKiller;
    public static Boolean wkenabled;

    // Fertilizer
    public static int fertilizerRadius;
    public static int fertilizerRate;
    public static String fertilizer;
    public static Boolean fertilizerEnabled;

    // UV
    public static int uvRadius;
    public static String uv;
    public static Boolean uvEnabled;

    // Bonemeal setting
    public static Boolean limitBonemeal;

    // Report growth setting
    public static Boolean reportGrowth;

    // Message format from config
    public static String msgFormat;

    // Common messages
    public static String fertilizerFound;
    public static String weedKillerFound;
    public static String uvFound;


    @Override
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        MiniMessage miniMessage = MiniMessage.miniMessage();
        this.configManager = ConfigManager.get();

        verboseMode = configManager.getDebug();
        pluginPrefix = configManager.getPluginPrefix();

        this.messageManager = new MessageManager(this, this.configManager , miniMessage);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the Bukkit plugin manager.
     */
    void disablePlugin(){
        Bukkit.getLogger().warning(pluginPrefix + "Disabling Plugin...");
        getServer().getPluginManager().disablePlugin(this);
    }

    // Getters
    public static String getPluginPrefix(){
        return pluginPrefix;
    }
    public static RealisticPlantGrowth getInstance(){
        return instance;
    }
    public static boolean getDebugStatus() {
        return verboseMode;
    }

    public ConfigManager getConfigManager(){
        return this.configManager;
    }

    public MessageManager getMessageManager(){
        return this.messageManager;
    }

    // Setters
    public void setPluginPrefix(String pluginPrefix){
        RealisticPlantGrowth.pluginPrefix = pluginPrefix;
    }

}
