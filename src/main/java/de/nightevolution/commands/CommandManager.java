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

/**
 * The {@link CommandManager} class handles the execution of plugin commands and delegates
 * the execution to the appropriate subcommands.
 */
public class CommandManager implements CommandExecutor {

    private final RealisticPlantGrowth instance;
    private final MessageManager msgManager;
    private final Logger logger;


    /**
     * Constructor for the {@link CommandManager} class.
     * Initializes the {@link RealisticPlantGrowth} instance, {@link MessageManager}, and {@link Logger}.
     */
    public CommandManager() {
        this.instance = RealisticPlantGrowth.getInstance();
        this.msgManager = instance.getMessageManager();

        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(),
                RealisticPlantGrowth.isDebug());

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    /**
     * Handles the execution of plugin commands and delegates the execution to subcommands.
     *
     * @param commandSender The sender of the command.
     * @param cmd           The executed command.
     * @param s             The alias used for the command.
     * @param arguments     The arguments provided with the command.
     * @return true if the command was handled successfully, otherwise false.
     */

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command cmd, String s, @Nullable String[] arguments) {

        if (s.equalsIgnoreCase("rpg") || s.equalsIgnoreCase("RealisticPlantGrowth")) {
            // Input validation
            if (arguments == null || arguments.length == 0) {
                if (commandSender.hasPermission("rpg.help")) {
                    msgManager.sendHelpMenu(commandSender);
                } else {
                    msgManager.sendNoPermissionMessage(commandSender);
                }
                return true;
            }

            // Prepare arguments for the switch case
            String[] args = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++) args[i] = arguments[i].toLowerCase();

            if (args.length == 1) {
                switch (args[0]) {
                    case "help":
                        logger.verbose(commandSender.getName() + " used help command.");
                        SubCommand help = new Help(commandSender, args, instance);
                        help.executeCommand();
                        break;
                    case "info":
                        logger.verbose(commandSender.getName() + " used info command.");
                        SubCommand info = new Info(commandSender, args, instance);
                        info.executeCommand();
                        break;
                    case "reload":
                        logger.verbose(commandSender.getName() + " used reload command.");
                        SubCommand reload = new Reload(commandSender, args, instance);
                        reload.executeCommand();
                        break;
                    default:
                        logger.verbose(commandSender.getName() + " used unknown rpg command.");
                        if (commandSender.hasPermission("rpg.help")) {
                            logger.verbose("Showing help menu.");
                            msgManager.sendHelpMenu(commandSender);
                        } else {
                            logger.verbose("Showing no permissions message.");
                            msgManager.sendNoPermissionMessage(commandSender);
                        }
                }
            }
        }
        return true;
    }
}
