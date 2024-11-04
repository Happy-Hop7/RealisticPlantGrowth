package de.nightevolution.realisticplantgrowth.commands.sub;


import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;


/**
 * Subcommand class for the 'help' command, responsible for displaying the plugin's help menu.
 */
public class Help extends SubCommand {

    /**
     * Constructor for the 'help' subcommand.
     *
     * @param commandSender The sender of the command.
     * @param args          The arguments passed with the command.
     * @param instance      The main plugin instance.
     */
    public Help(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = new Permission("rpg.help");
    }

    /**
     * Executes the 'help' command, displaying the plugin's help menu.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand() {
        if (!super.executeCommand())
            return false;

        // Send the plugin's help menu to the command sender
        msgManager.sendHelpMenu(commandSender);
        return true;
    }
}
