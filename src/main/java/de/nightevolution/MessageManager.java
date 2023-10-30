package de.nightevolution;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

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


    public MessageManager(RealisticPlantGrowth instance, ConfigManager configManager, MiniMessage miniMessage){
        this.instance = instance;
        this.configManager = configManager;
        this.miniMessage = miniMessage;
    }

    public Component parseMessage(String message){
        return miniMessage.deserialize(message);
    }

    // Todo: read lang yaml from config
    // Todo: create all language files in lang directory
    private void readMessageStringsFromConfig(){

    }
}
