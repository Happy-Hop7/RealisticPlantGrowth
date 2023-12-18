package de.nightevolution.commands;


import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.commands.sub.Help;
import de.nightevolution.commands.sub.Info;
import de.nightevolution.commands.sub.Reload;
import de.nightevolution.commands.sub.SubCommand;
import de.nightevolution.utils.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandManager implements CommandExecutor {

    private RealisticPlantGrowth instance;
    protected MessageManager msgManager;

    private Logger logger;


    public CommandManager(){
        this.instance = RealisticPlantGrowth.getInstance();
        this.msgManager = instance.getMessageManager();

        this.logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(),
                RealisticPlantGrowth.isDebug());

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command cmd, String s, @Nullable String[] arguments) {
        instance = RealisticPlantGrowth.getInstance();

        if(s.equalsIgnoreCase("rpg") || s.equalsIgnoreCase("RealisticPlantGrowth")){
            // input validation
            if(arguments == null || arguments.length == 0) {
                if (commandSender.hasPermission("rpg.help")) {
                    msgManager.sendHelpMenu(commandSender);
                }else{
                    msgManager.sendNoPermissionMessage(commandSender);
                }
                return true;
            }
            // prepare arguments for the switch case
            String[] args = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++) args[i] = arguments[i].toLowerCase();

            if(args.length == 1){
                switch (args[0]){
                    case "help":
                        SubCommand help = new Help(commandSender, args, instance);
                        help.executeCommand();
                        break;
                    case "info":
                        SubCommand info = new Info(commandSender, args, instance);
                        info.executeCommand();
                        break;
                    case "reload":
                        SubCommand reload = new Reload(commandSender, args, instance);
                        reload.executeCommand();
                        break;
                    default:
                        if(commandSender.hasPermission("rpg.help")) {
                            msgManager.sendHelpMenu(commandSender);
                        }else {
                            msgManager.sendNoPermissionMessage(commandSender);
                        }
                }
            }
        }
        return true;
    }

}
