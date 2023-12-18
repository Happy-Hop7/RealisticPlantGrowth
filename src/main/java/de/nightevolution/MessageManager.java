package de.nightevolution;

import de.nightevolution.utils.Logger;
import de.nightevolution.utils.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MessageManager {
    private final RealisticPlantGrowth instance;
    private static ConfigManager configManager;
    private static MessageManager messageManager;
    private final MiniMessage miniMessage;
    private Logger logger;


    private static String prefix;
    private static EnumMap<MessageType, String> localizedMessagePair;

    private MessageManager(){
        this.instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        this.miniMessage = MiniMessage.miniMessage();
        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        messageManager = this;
    }

    @NotNull
    protected static MessageManager get(){
        if(messageManager == null)
            new MessageManager();
        checkAndUpdateMessages();
        return messageManager;
    }

    protected static void checkAndUpdateMessages(){

        localizedMessagePair = new EnumMap<>(MessageType.class);

        for (MessageType s : MessageType.values()){
            localizedMessagePair.put(s, configManager.getSelectedLanguageString(s.toString()));
        }

        prefix = localizedMessagePair.get(MessageType.MSG_HEADER);

    }

    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType,
                                 @Nullable List<String> placeholders, @Nullable List<Object> replacements, boolean sendHeader) {

        String message = localizedMessagePair.get(messageType);

        if (placeholders != null && replacements != null) {
            if (placeholders.size() != replacements.size()) {
                throw new IllegalArgumentException("MessageManager.getLocalizedMsg() to less arguments provided!");
            }

            // Replace Plugin Placeholders
            for (int i = 0; i < placeholders.size(); i++) {
                message = message.replace(placeholders.get(i), replacements.get(i).toString());
            }
        }

        logger.verbose(message);

        if (sendHeader)
            sendMessageHeader(target);

        Component messageComponent = miniMessage.deserialize(message);
        target.spigot().sendMessage(BungeeComponentSerializer.get().serialize(messageComponent));

    }

    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType,
                                 @NotNull String placeholder, @NotNull Object replacement, boolean sendHeader) {
        List<String> placeholders = new ArrayList<>(1);
        List<Object> replacements = new ArrayList<>(1);
        placeholders.add(placeholder);
        replacements.add(replacement);

        sendLocalizedMsg(target, messageType, placeholders, replacements, sendHeader);
    }

    public void sendLocalizedMsg(@NotNull CommandSender target, @NotNull MessageType messageType, boolean sendHeader){
        sendLocalizedMsg(target, messageType, new ArrayList<>(), new ArrayList<>(), sendHeader);
    }


    public void sendMessageHeader(CommandSender target){
        Component headerComponent = miniMessage.deserialize(prefix);
        target.spigot().sendMessage(BungeeComponentSerializer.get().serialize(headerComponent));
    }

    public Component deserializeMiniMessage(String message){
        return miniMessage.deserialize(message);
    }
    public String serializeMiniMessage(Component message){
                return ChatColor.translateAlternateColorCodes('&', miniMessage.serialize(message));
    }

    /**
     * Sends a Message to a player
     */
    public void sendHelpMenu(CommandSender sender){
        sendLocalizedMsg(sender, MessageType.HELP_CMD_MSG, true);
        sendLocalizedMsg(sender, MessageType.INFO_CMD_MSG, false);
        sendLocalizedMsg(sender, MessageType.RELOAD_CMD_MSG, false);
    }
    public void sendNoPermissionMessage(CommandSender sender){
        sendLocalizedMsg(sender, MessageType.NO_PERMISSIONS, false);
    }

}
