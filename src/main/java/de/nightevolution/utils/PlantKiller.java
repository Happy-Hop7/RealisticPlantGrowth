package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

public class PlantKiller {

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private final Logger logger;

    public PlantKiller(){
        this.instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    public void killPlant (Block plantToKill){
        Material plantType = plantToKill.getType();

        if(instance.isAPlant(plantToKill)){
            plantToKill.setType(Material.DEAD_BUSH);
            // Bamboo

            // Melon / Pumpkin

            // farmland plants

            // ...

        } else if (instance.isAnAquaticPlant(plantToKill)) {

        }
    }

    /**
     * Reduces the fill level of a composter block if passive fertilizer mode is not enabled.
     * <p>
     * In non-passive fertilizer mode, this method decreases the fill level of the specified composter block by one.
     * If passive fertilizer mode is enabled, no action is taken.
     * </p>
     *
     * @param composterToDrain The composter block to reduce the fill level.
     */
    public void reduceComposterFillLevelOf(Block composterToDrain){
        // Check if passive fertilizer passiv mode is enabled
        if(configManager.isFertilizer_passiv()) {
            return;
        }

        // Check if the provided block is a composter
        if(composterToDrain.getType() != Material.COMPOSTER) {
            logger.warn("Could not reduce fill level on Block: " + composterToDrain.getType());
            return;
        }

        // Check if the composter's fill level is already at 0
        Levelled composter = (Levelled) composterToDrain.getBlockData();
        if(composter.getLevel() == 0){
            logger.warn("Composter fill level already at 0!");
            return;
        }

        // Reduce the composter's fill level
        int oldFillLevel = composter.getLevel();
        composter.setLevel(oldFillLevel-1);
    }

}
