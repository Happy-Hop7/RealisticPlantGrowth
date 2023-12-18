package de.nightevolution.commands.sub;


import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public class Info extends SubCommand {

    private final Permission PERMISSION = new Permission("rpg.info");
    public Info(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = PERMISSION;
    }

    @Override
    public boolean executeCommand() {
        if(!super.executeCommand())
            return false;

        // TODO: Implement Command logic
        return true;
    }

}
