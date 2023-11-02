package de.nightevolution.utils;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Logger {
    private static RealisticPlantGrowth instance;

    private static boolean verbose;
    private static boolean debug;
    private static String pluginPrefix = "[RealisticPlantGrowth] ";

    private final String classPrefix;

    private java.util.logging.Logger javaLogger;


    public Logger(String classPrefix, RealisticPlantGrowth instance, boolean verbose) {
        this.classPrefix = classPrefix.strip() + ": ";
        Logger.instance = instance;
        Logger.verbose = verbose;
        debug = verbose;
    }

    public void log(String msg) {
        msg = StringUtils.translateColor(pluginPrefix + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public void debug(String msg) {
        if (isDebug())
            log("&c&lDEBUG >>&r " + classPrefix + msg);
    }
    public void verbose(String msg) {
        if (isVerbose())
            log("&4&lVERBOSE >>&r " + classPrefix + msg);
    }

    public String getPluginPrefix(){
        return pluginPrefix;
    }
    public boolean isDebug() {
        return debug;
    }
    public boolean isVerbose() {
        return verbose;
    }

    public void setInstance(RealisticPlantGrowth instance){
        Logger.instance = instance;
    }
    public void setPluginPrefix(String pluginPrefix){
        Logger.pluginPrefix = pluginPrefix;
    }
    public void setDebug(boolean debug) {
        Logger.debug = debug;
    }
    public void setVerbose(boolean verbose) {
        Logger.verbose = verbose;
    }
}
