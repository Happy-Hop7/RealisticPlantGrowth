package de.nightevolution.realisticplantgrowth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The TabCompleterImpl class handles tab completion for plugin commands.
 */
public class TabCompleterImpl implements TabCompleter {

    /**
     * Handles tab completion for plugin commands.
     *
     * @param commandSender The sender of the command.
     * @param command       The executed command.
     * @param s             The alias used for the command.
     * @param args          The arguments provided with the command.
     * @return A list of tab-completed options for the current input.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String s, @Nullable String[] args) {

        List<String> commands = new ArrayList<>();
        List<String> shownTabs = new ArrayList<>();
        List<String> allPermissions = new ArrayList<>();

        // List of all Permissions
        allPermissions.add("rpg.help");
        allPermissions.add("rpg.info");
        allPermissions.add("rpg.info.interact");
        allPermissions.add("rpg.reload");

        boolean hasAtLeastOnePermission = false;

        // Checks if the CommandSender has at least one Permission
        for (String permission : allPermissions) {
            if (commandSender.hasPermission(permission)) {
                hasAtLeastOnePermission = true;
                break;
            }
        }

        // Show plugin base command if commandSender has at least one rpg permission
        if (args == null || args.length == 0) {
            if (hasAtLeastOnePermission) {
                commands.add("realisticPlantGrowth");
                commands.add("rpg");
            }
            StringUtil.copyPartialMatches(s, commands, shownTabs);
        }

        // Show sub commands of the base command
        else if (args.length == 1) {
            if (commandSender.hasPermission("rpg.help")) {
                commands.add("help");
            }
            if (commandSender.hasPermission("rpg.info")) {
                commands.add("info");
            }
            if (commandSender.hasPermission("rpg.reload")) {
                commands.add("reload");
            }
            StringUtil.copyPartialMatches(args[0], commands, shownTabs);
        }

        Collections.sort(shownTabs);

        return shownTabs;
    }
}
