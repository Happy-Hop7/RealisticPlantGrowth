package de.nightevolution;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

public class MessageManager {

    //todo: support locals and minimassage format

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private static MessageManager messageManager;

    private final MiniMessage miniMessage;

    private String player_msg_format;
    private String no_permissions;
    private String rpg_help;
    private String rpg_info;
    private String rpg_reload;


    private MessageManager(){
        this.instance = RealisticPlantGrowth.getInstance();
        this.configManager = instance.getConfigManager();
        this.miniMessage = MiniMessage.miniMessage();
        messageManager = this;
    }

    protected static MessageManager get(){
        if(messageManager == null)
            new MessageManager();
        return messageManager;
    }
    public Component deserializeMiniMessage(String message){
        return miniMessage.deserialize(message);
    }
    public String serializeMiniMessage(Component message){
                return ChatColor.translateAlternateColorCodes('&', miniMessage.serialize(message));
    }
    private void readMessageStringsFromConfig(){

    }
}
