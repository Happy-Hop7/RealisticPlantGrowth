package de.nightevolution.utils;

import de.nightevolution.RealisticPlantGrowth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.bukkit.Bukkit;

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

    public void log_old(String msg) {
        msg = StringUtils.translateColor(pluginPrefix + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }
    public void log(String msg) {
        Component c = MiniMessage.miniMessage().deserialize(pluginPrefix + msg);
        String msgInANSI = ANSIComponentSerializer.ansi().serialize(c);

        // Translate & in § Lagacy ColorCodes for Console
        Bukkit.getConsoleSender().sendMessage(StringUtils.translateColor(msgInANSI));

    }

    public void warn_old(String msg) {
        Component c = MiniMessage.miniMessage().deserialize(classPrefix + msg);
        String msgToANSI = ANSIComponentSerializer.ansi().serialize(c);
        Bukkit.getLogger().warning(msgToANSI);

    }
    public void debug(String msg) {
        if (isDebug())
            log(DEBUG + classPrefix + msg);
    }
    public void verbose(String msg) {
        if (isVerbose())
            log(VERBOSE + classPrefix + msg);
    }
    public void warn(String msg) {
        if(verbose)
            log( WARN + classPrefix + msg);
        else
            log( WARN + msg);
    }

    public void error(String msg) {
        log( ERROR + classPrefix + msg);
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
