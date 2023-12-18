package de.nightevolution.commands.sub;

import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public abstract class SubCommand {

    protected RealisticPlantGrowth instance;
    protected MessageManager msgManager;
    protected CommandSender commandSender;
    protected String[] args;
    protected Permission permission;

    public SubCommand(CommandSender commandSender, String[] args, RealisticPlantGrowth instance){
        this.instance = instance;
        this.msgManager = instance.getMessageManager();
        this.commandSender = commandSender;
        this.args = args;
    }

    public boolean executeCommand() {
        if(!commandSender.hasPermission(permission)) {
            msgManager.sendNoPermissionMessage(commandSender);
            return false;
        }

        return true;
    }

}
