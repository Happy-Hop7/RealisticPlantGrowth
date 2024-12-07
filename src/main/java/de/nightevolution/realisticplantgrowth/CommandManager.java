package de.nightevolution.realisticplantgrowth;


import de.nightevolution.realisticplantgrowth.commands.sub.Help;
import de.nightevolution.realisticplantgrowth.commands.sub.Info;
import de.nightevolution.realisticplantgrowth.commands.sub.Reload;
import de.nightevolution.realisticplantgrowth.commands.sub.SubCommand;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.exception.ConfigurationException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * The {@link CommandManager} class handles the execution of plugin commands and delegates
 * the execution to the appropriate subcommands.
 */
public class CommandManager implements CommandExecutor {

    private final RealisticPlantGrowth instance;
    private final MessageManager msgManager;
    private final Logger logger;
    private final String logFile = "debug";
    private final boolean logEvent;

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
        logEvent = RealisticPlantGrowth.isDebug();
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

            if (logEvent) {
                logger.logToFile("", logFile);
                logger.logToFile("-------------------- RealisticPlantGrowth Command --------------------", logFile);
                logger.logToFile("  Command sender: " + commandSender.getName(), logFile);
                logger.logToFile("  Used Command: " + cmd.getName(), logFile);
                logger.logToFile("  Provided Arguments: {" + Arrays.toString(arguments) + "}", logFile);
            }

            // Input validation
            if (arguments == null || arguments.length == 0) {
                if (commandSender.hasPermission("rpg.help")) {

                    if (logEvent) {
                        logger.logToFile("  No arguments provided with /rpg command.", logFile);
                        logger.logToFile("  Sending Plugin help message.", logFile);
                    }
                    msgManager.sendHelpMenu(commandSender);
                } else {

                    if (logEvent)
                        logger.logToFile("  Permission Denied: User '" + commandSender.getName() +
                                "' lacks permission for command '/rpg help'", logFile);

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
                        SubCommand help = new Help(commandSender, args, instance);
                        help.executeCommand();
                        break;
                    case "info":
                        SubCommand info = new Info(commandSender, args, instance);
                        info.executeCommand();
                        break;
                    case "reload":
                        try {
                            SubCommand reload = new Reload(commandSender, args, instance);
                            reload.executeCommand();
                        } catch (ConfigurationException e) {
                            logger.error("Failed to execute the reload command: Configuration error.");
                            instance.disablePlugin();
                        }
                        break;
                    default:
                        if (logEvent) {
                            logger.logToFile("  User: " + commandSender.getName() +
                                    " used unknown RealisticPlantGrowth command.", logFile);
                        }

                        if (commandSender.hasPermission("rpg.help")) {
                            if (logEvent)
                                logger.logToFile("  Sending Plugin help message.", logFile);

                            msgManager.sendHelpMenu(commandSender);
                        } else {
                            if (logEvent)
                                logger.logToFile("  Permission Denied: User '" + commandSender.getName() +
                                        "' lacks permission for command '/rpg help'", logFile);

                            msgManager.sendNoPermissionMessage(commandSender);
                        }
                }
            }
        }
        return true;
    }
}
