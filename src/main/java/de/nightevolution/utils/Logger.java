package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static RealisticPlantGrowth instance;

    private static boolean verbose;
    private static boolean debug;
    private static String pluginPrefix = "[RealisticPlantGrowth] ";

    private static final String DEBUG = "&4DEBUG >>&r ";
    private static final String VERBOSE = "&cVERBOSE >>&r ";
    private static final String WARN = "&eWARNING >> ";
    private static final String ERROR = "&cERROR >> ";

    private final String classPrefix;

    public Logger(String classPrefix, RealisticPlantGrowth instance, boolean verbose, boolean debug) {
        this.classPrefix = classPrefix.strip() + ": ";
        Logger.instance = instance;
        Logger.verbose = verbose;
        Logger.debug = debug;
    }

    public static String getDate() {
        Date date = new Date();
        Format formatter = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");

        return formatter.format(date);
    }

    /**
     * Stores a String with a Timestamp to a log file
     * Calls {@link ConfigManager} to perform the file I/O tasks.
     * @param msg String to write into the file.
     * @param fileName String representing the name of a File.
     */
    public void logToFile(String msg, String fileName) {

        ConfigManager cm = instance.getConfigManager();
        if(cm == null){
            error("&cCould not log into File: ConfigManager not initialized yet.");
            return;
        }

        cm.writeToLogFile(getDate() + " " + msg, fileName);

    }

    /**
     * Formats and logs a given message provided as String to the console.
     * Messages containing {@link MiniMessage} tags get serialized using the {@link ANSIComponentSerializer}.
     * '&' color cotes get resolved by using the {@link ChatColor} functions of {@link Bukkit}.
     * @param msg Message to send to the console.
     */
    public void log(String msg) {
        Component c = MiniMessage.miniMessage().deserialize(pluginPrefix + msg);
        String msgInANSI = ANSIComponentSerializer.ansi().serialize(c);

        // Translate & in § Legacy ColorCodes for Console
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msgInANSI));
    }

    /**
     * Special message format for debugging messages in console.
     * Calls log() in order to resolve {@link MiniMessage} tags and to send the message.
     * @param msg Debugging message.
     */
    public void debug(String msg) {
        if (debug)
            log(DEBUG + classPrefix + msg);
    }

    /**
     * Special message format for extra verbose messages in console.
     * Calls log() in order to resolve {@link MiniMessage} tags and to send the message.
     * @param msg Verbose message.
     */
    public void verbose(String msg) {
        if (verbose)
            log(VERBOSE + classPrefix + msg);
    }

    /**
     * Special message format for warning messages in console.
     * Calls log() in order to resolve {@link MiniMessage} tags and to send the message.
     * @param msg Warning message.
     */
    public void warn(String msg) {
        if(verbose)
            log( WARN + classPrefix + msg);
        else
            log( WARN + msg);
    }

    /**
     * Special message format for error messages in console.
     * Calls log() in order to resolve {@link MiniMessage} tags and to send the message.
     * @param msg Error message.
     */
    public void error(String msg) {
        log( ERROR + classPrefix + msg);
    }




    public boolean isDebug() {
        return debug;
    }
    public boolean isVerbose() {
        return verbose;
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
