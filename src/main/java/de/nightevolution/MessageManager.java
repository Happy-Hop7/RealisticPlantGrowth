package de.nightevolution;

import de.nightevolution.utils.Logger;
import de.nightevolution.utils.enums.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * The MessageManager class handles the localization and sending of messages to players.
 * It uses a {@link MiniMessage} instance for text serialization and a {@link Logger} for logging messages.
 */
public class MessageManager {
    private static ConfigManager configManager;
    private static MessageManager messageManager;
    private final MiniMessage miniMessage;
    private final Logger logger;

    private static final String logFile = "debug";
    private static boolean debug;
    private static String prefix;
    private static EnumMap<MessageType, String> localizedMessagePair;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private MessageManager() {
        RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        this.miniMessage = MiniMessage.miniMessage();
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        messageManager = this;
    }

    /**
     * Retrieves the singleton instance of the {@link MessageManager}.
     *
     * @return The {@link MessageManager} instance.
     */
    @NotNull
    protected static MessageManager get() {
        if (messageManager == null)
            new MessageManager();
        checkAndUpdateMessages();
        return messageManager;
    }

    /**
     * Checks and updates the localized message pairs from the configuration.
     */
    protected static void checkAndUpdateMessages() {
        localizedMessagePair = new EnumMap<>(MessageType.class);

        for (MessageType s : MessageType.values()) {
            localizedMessagePair.put(s, configManager.getSelectedLanguageString(s.toString()));
        }

        debug = RealisticPlantGrowth.isDebug();
        prefix = localizedMessagePair.get(MessageType.MSG_HEADER);
    }

    /**
     * Sends a localized message to the specified command sender.
     *
     * @param target       The command sender to receive the message.
     * @param messageType  The type of the message to be sent.
     * @param placeholders List of placeholders in the message.
     * @param replacements List of replacements for the placeholders.
     * @param sendHeader   Whether to send the message header.
     */
    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType,
                                 @Nullable List<String> placeholders, @Nullable List<Object> replacements, boolean sendHeader) {

        if (debug) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Message Sent --------------------", logFile);
            logger.logToFile("  To: " + target.getName(), logFile);
            logger.logToFile("  Message Type: " + messageType, logFile);
        }

        String messageWithPlaceholder = localizedMessagePair.get(messageType);
        String message = processPlaceholder(messageWithPlaceholder, placeholders, replacements);

        if (debug) {
            logger.logToFile("  Message with Placeholder:" + System.lineSeparator() + messageWithPlaceholder, logFile);
            logger.logToFile("  Message without Placeholder:" + System.lineSeparator() + message, logFile);
        }

        if (sendHeader)
            sendMessageHeader(target);

        Component messageComponent = miniMessage.deserialize(message);
        target.spigot().sendMessage(BungeeComponentSerializer.get().serialize(messageComponent));
    }

    /**
     * Sends a localized message to the specified command sender with a single placeholder and replacement.
     *
     * @param target      The command sender to receive the message.
     * @param messageType The type of the message to be sent.
     * @param placeholder The placeholder in the message.
     * @param replacement The replacement for the placeholder.
     * @param sendHeader  Whether to send the message header.
     */
    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType,
                                 @NotNull String placeholder, @NotNull Object replacement, boolean sendHeader) {
        List<String> placeholders = new ArrayList<>(1);
        List<Object> replacements = new ArrayList<>(1);
        placeholders.add(placeholder);
        replacements.add(replacement);

        sendLocalizedMsg(target, messageType, placeholders, replacements, sendHeader);
    }

    /**
     * Sends a localized message to the specified command sender without placeholders and replacements.
     *
     * @param target      The command sender to receive the message.
     * @param messageType The type of the message to be sent.
     * @param sendHeader  Whether to send the message header.
     */
    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType, boolean sendHeader) {
        sendLocalizedMsg(target, messageType, new ArrayList<>(), new ArrayList<>(), sendHeader);
    }

    /**
     * Sends the message header to the specified command sender.
     *
     * @param target The command sender to receive the message header.
     */
    public void sendMessageHeader(CommandSender target) {
        Component headerComponent = miniMessage.deserialize(prefix);
        target.spigot().sendMessage(BungeeComponentSerializer.get().serialize(headerComponent));
    }

    /**
     * Sends the help menu to the specified command sender.
     *
     * @param sender The command sender to receive the help menu.
     */
    public void sendHelpMenu(CommandSender sender) {
        sendLocalizedMsg(sender, MessageType.HELP_CMD_MSG, true);
        sendLocalizedMsg(sender, MessageType.INFO_CMD_MSG, false);
        sendLocalizedMsg(sender, MessageType.RELOAD_CMD_MSG, false);
    }

    /**
     * Sends a no permission message to the specified command sender.
     *
     * @param sender The command sender to receive the no permission message.
     */
    public void sendNoPermissionMessage(CommandSender sender) {
        sendLocalizedMsg(sender, MessageType.NO_PERMISSIONS, false);
    }

    /**
     * Processes placeholders in the message with their corresponding replacements.
     *
     * @param message       The message containing placeholders.
     * @param placeholders  The list of placeholders to be replaced.
     * @param replacements  The list of replacements for the placeholders.
     * @return The message with placeholders replaced by their corresponding values.
     */
    public String processPlaceholder(String message, @Nullable List<String> placeholders, @Nullable List<Object> replacements) {
        if (placeholders != null && replacements != null && !replacements.isEmpty()) {
            if (placeholders.size() != replacements.size()) {
                throw new IllegalArgumentException("MessageManager.processPlaceholder() received an incorrect number of arguments!");
            }

            // Replace Plugin Placeholders
            for (int i = 0; i < placeholders.size(); i++) {
                message = message.replace(placeholders.get(i), replacements.get(i).toString());
            }

            // Check if the Plant Placeholder is a Block or Item (relevant for correct MiniMessage formatting)
            Material plantMaterialType = Material.getMaterial(replacements.get(0).toString().toUpperCase());

            if (plantMaterialType != null && !plantMaterialType.isBlock()) {
                message = message.replace("lang:block.minecraft", "lang:item.minecraft");

                if (debug) {
                    logger.logToFile("  Processed Placeholder Material: " + plantMaterialType, logFile);
                    logger.logToFile("  Placeholder Material is an Item: " + plantMaterialType.isItem(), logFile);
                }
            }
        }

        return message;
    }
}
