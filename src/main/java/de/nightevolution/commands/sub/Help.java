package de.nightevolution.commands.sub;


import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class Help extends SubCommand {
    private final Permission PERMISSION = new Permission("rpg.help");

    public Help(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = PERMISSION;
    }

    @Override
    public boolean executeCommand() {
        if (!super.executeCommand())
            return false;

        msgManager.sendHelpMenu(commandSender);
        return true;
    }
}
