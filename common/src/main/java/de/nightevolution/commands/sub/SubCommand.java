package de.nightevolution.commands.sub;

import de.nightevolution.ConfigManager;
import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

/**
 * Abstract base class for subcommands in the {@link RealisticPlantGrowth} plugin.
 * Subclasses of this class represent individual commands and their specific functionalities.
 */
public abstract class SubCommand {

    protected RealisticPlantGrowth instance;
    protected MessageManager msgManager;
    protected CommandSender commandSender;
    protected ConfigManager configManager;
    protected VersionMapper mapper;
    protected String[] args;
    protected Permission permission;

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
    }

    /**
     * Executes the subcommand, performing a specific permission check for each subclass.
     *
     * @return True if the check was successfully, false otherwise.
     */
    public boolean executeCommand() {
        if (!commandSender.hasPermission(permission)) {
            msgManager.sendNoPermissionMessage(commandSender);
            return false;
        }

        return true;
    }

}
