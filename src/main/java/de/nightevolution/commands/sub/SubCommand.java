package de.nightevolution.commands.sub;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public abstract class SubCommand {

    protected CommandSender commandSender;
    protected String[] args;
    protected RealisticPlantGrowth instance;
    protected Permission permission;

    public SubCommand(CommandSender commandSender, String[] args, RealisticPlantGrowth instance){
        this.commandSender = commandSender;
        this.args = args;
        this.instance = instance;
    }

    public void executeCommand() {
        if(!commandSender.hasPermission(permission))
            return;
    }
}
