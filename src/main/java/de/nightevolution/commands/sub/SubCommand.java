package de.nightevolution.commands.sub;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public abstract class SubCommand {

    CommandSender commandSender;
    String[] args;
    RealisticPlantGrowth instance;
    Permission permission;

    public static void executeCommand(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {

    }
}
