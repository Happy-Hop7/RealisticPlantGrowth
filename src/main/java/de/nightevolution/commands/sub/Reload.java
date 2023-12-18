package de.nightevolution.commands.sub;


import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.MessageType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class Reload extends SubCommand {

    private final Permission PERMISSION = new Permission("rpg.reload");

    public Reload(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = PERMISSION;
    }

    @Override
    public boolean executeCommand() {
        if (!super.executeCommand())
            return false;

        instance.reload();

        msgManager.sendLocalizedMsg(commandSender, MessageType.RELOAD_COMPLETE_MSG, false);
        return true;
    }
}
