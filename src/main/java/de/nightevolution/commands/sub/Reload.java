package de.nightevolution.commands.sub;


import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.enums.MessageType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;


/**
 * Subcommand class for the 'reload' command, responsible for reloading the plugin configuration.
 */
public class Reload extends SubCommand {

    /**
     * Constructor for the 'reload' subcommand.
     *
     * @param commandSender The sender of the command.
     * @param args          The arguments passed with the command.
     * @param instance      The main plugin instance.
     */
    public Reload(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = new Permission("rpg.reload");
    }

    /**
     * Executes the 'reload' command, reloading the plugin configuration.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand() {
        if (!super.executeCommand())
            return false;

        // Reload the plugin configuration
        instance.reload();

        // Send a message indicating that the reload is complete
        msgManager.sendLocalizedMsg(commandSender, MessageType.RELOAD_COMPLETE_MSG, false);
        return true;
    }
}
