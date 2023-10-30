package de.nightevolution.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleterImpl implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        List<String> commands = new ArrayList<>();
        List<String> shownTabs = new ArrayList<>();
        List<String> allPermissions = new ArrayList<>();

        // List of all Permissions
        allPermissions.add("rpg.help");
        allPermissions.add("rpg.info");
        allPermissions.add("rpg.admin");

        boolean hasAtLeastOnePermission = false;

        // Checks if the CommandSender has at least one Permission
        for (String permission : allPermissions){
            if(commandSender.hasPermission(permission)){
                hasAtLeastOnePermission = true;
                break;
            }
        }

        // Show plugin base command if commandSender has at least one rpg permission
        if(args == null || args.length == 0){
            if(hasAtLeastOnePermission) {
                commands.add("realisticPlantGrowth");
                commands.add("rpg");
            }
            StringUtil.copyPartialMatches(s, commands, shownTabs);
        }

        // Show sub commands of the base command
        else if(args.length == 1){
            if(commandSender.hasPermission("rpg.help")){
                commands.add("help");
            }
            if(commandSender.hasPermission("rpg.info")){
                commands.add("info");
            }
            if(commandSender.hasPermission("rpg.admin")){
                commands.add("reload");
            }
            StringUtil.copyPartialMatches(args[0], commands, shownTabs);

        }

        Collections.sort(shownTabs);

        return shownTabs;
    }
}