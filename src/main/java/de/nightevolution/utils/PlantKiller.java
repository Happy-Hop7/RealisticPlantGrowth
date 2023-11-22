package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.block.Block;

public class PlantKiller {

    private RealisticPlantGrowth instance;
    private ConfigManager configManager;

    public PlantKiller(RealisticPlantGrowth instance){
        this.instance = instance;
        configManager = instance.getConfigManager();
    }

    public void killPlant (Block plantToKill){
        if(instance.isAPlant(plantToKill)){
            // Bamboo

            // Melon / Pumpkin

            // farmland plants

            // ...

        } else if (instance.isAnAquaticPlant(plantToKill)) {

        }
    }

}
