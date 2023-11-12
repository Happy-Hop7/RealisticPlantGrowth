package de.nightevolution;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

public class MessageManager {

    //todo: support locals and minimassage format

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private final MiniMessage miniMessage;

    private String player_msg_format;
    private String no_permissions;
    private String rpg_help;
    private String rpg_info;
    private String rpg_reload;


    public MessageManager(RealisticPlantGrowth instance){
        this.instance = instance;
        this.configManager = instance.getConfigManager();
        this.miniMessage = MiniMessage.miniMessage();
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
