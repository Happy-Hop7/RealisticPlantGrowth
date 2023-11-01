package de.nightevolution.commands;


import de.nightevolution.commands.sub.Help;
import de.nightevolution.commands.sub.Info;
import de.nightevolution.commands.sub.Reload;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

    private static RealisticPlantGrowth instance;
    private static CommandManager commandManager;


    private CommandManager(){
        instance = RealisticPlantGrowth.getInstance();
    }

    public static CommandManager get(){
        if(commandManager == null)
            commandManager = new CommandManager();
        return commandManager;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String s, String[] arguments) {
        instance = RealisticPlantGrowth.getInstance();

        if(s.equalsIgnoreCase("rpg") || s.equalsIgnoreCase("realisticPlantGrowth")){
            // input validation
            if(arguments == null || arguments.length == 0) {
                if (commandSender.hasPermission("rpg.help")) {
                    sendHelpMessage(commandSender);
                }else{
                    sendNoPermissionMessage(commandSender);
                }
                return true;
            }
            // prepare arguments for switch case
            String[] args = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++) args[i] = arguments[i].toLowerCase();

            if(args.length == 1){
                switch (args[0]){
                    case "help":
                        Help.executeCommand(commandSender, args, instance);
                        break;
                    case "info":
                        Info.executeCommand(commandSender, args, instance);
                        break;
                    case "reload":
                        Reload.executeCommand(commandSender, args, instance);
                        break;
                    default:
                        if(commandSender.hasPermission("rpg.help")) {
                            sendHelpMessage(commandSender);
                        }else {
                            sendNoPermissionMessage(commandSender);
                        }
                }
            }
        }
        return true;
    }


    /**
     * Sends a Message to a player
     * Todo: Auslagern in Message Builder
     */
    protected static void sendHelpMessage(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + "/rpg help" + ChatColor.RESET + " - Shows this help message");
        sender.sendMessage(ChatColor.GREEN + "/rpg info" + ChatColor.RESET + " - Gives information about plant currently in holding in hand");
        sender.sendMessage(ChatColor.GREEN + "/rpg reload" + ChatColor.RESET + " - Reloads the plugin and all related configs");
    }
    protected static void sendNoPermissionMessage(CommandSender sender){
        sender.sendMessage(ChatColor.RED + "You don't have the required permissions to perform this command!");
    }

}
