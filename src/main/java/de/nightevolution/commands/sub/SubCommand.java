package de.nightevolution.commands.sub;

import de.nightevolution.ConfigManager;
import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

/**
 * Abstract base class for subcommands in the {@link RealisticPlantGrowth} plugin.
 * Subclasses of this class represent individual commands and their specific functionalities.
 * Each subclass must implement its own command logic while leveraging the common functionality
 * provided by this base class.
 */
public abstract class SubCommand {

    /** The main plugin instance */
    protected RealisticPlantGrowth instance;

    /** Manager for handling messages to the user */
    protected MessageManager msgManager;

    /** The sender of the command (e.g., a player or console) */
    protected CommandSender commandSender;

    /** Manager for handling plugin configuration */
    protected ConfigManager configManager;

    /** Mapper for handling version-specific operations */
    protected VersionMapper mapper;

    /** Logger for logging actions and events, especially in verbose mode */
    protected Logger superLogger;

    /** The arguments passed with the command */
    protected String[] args;

    /** The permission required to execute the subcommand */
    protected Permission permission;

    /** Flag to indicate if verbose logging is enabled */
    protected boolean verbose;

    /** File name for logging verbose output */
    protected String logFile = "verbose";

    /**
     * Constructor for the abstract 'SubCommand' class.
     *
     * @param commandSender The sender of the command.
     * @param args          The arguments passed with the command.
     * @param instance      The main plugin instance.
     */
    public SubCommand(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        this.instance = instance;
        this.msgManager = instance.getMessageManager();
        this.configManager = instance.getConfigManager();
        this.mapper = instance.getVersionMapper();
        this.commandSender = commandSender;
        this.args = args;
        this.verbose = RealisticPlantGrowth.isVerbose();
        this.superLogger = new Logger(this.getClass().getSimpleName(), verbose, RealisticPlantGrowth.isDebug());

        if (verbose) {
            superLogger.logToFile("", logFile);
            superLogger.logToFile("-------------------- SubCommand --------------------", logFile);
        }
    }

    /**
     * Executes the subcommand, performing a specific permission check for each subclass.
     * Subclasses should override this method to implement their specific command logic.
     *
     * @return True if the permission check passes, false otherwise.
     */
    public boolean executeCommand() {
        if (!commandSender.hasPermission(this.permission)) {
            msgManager.sendNoPermissionMessage(this.commandSender);

            if (verbose)
                superLogger.logToFile("Permission Denied: User '" + commandSender.getName() +
                        "' lacks permission for subcommand '" + this.getClass().getSimpleName() + "'", logFile);
            return false;
        }

        if (verbose)
            superLogger.logToFile("Executing SubCommand: User '" + commandSender.getName() +
                    "' is executing subcommand '" + this.getClass().getSimpleName() + "'", logFile);
        return true;
    }

}
