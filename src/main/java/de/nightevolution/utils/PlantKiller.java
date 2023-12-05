package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitScheduler;

public class PlantKiller {

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private final Logger logger;

    private final BukkitScheduler scheduler;

    public PlantKiller(){
        this.instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        scheduler = Bukkit.getScheduler();
    }

    public void killPlant (Block plantToKill){
        Material plantType = plantToKill.getType();

        if(instance.isAPlant(plantToKill)){

            // Bamboo

            // Melon / Pumpkin

            // farmland plants
            if (instance.isAgriculturalPlant(plantToKill)){
                logger.verbose("Killing Agricultural Plant.");
                plantToKill.setType(Material.AIR);


                double randomMaterial = Math.random()*100;

                if(randomMaterial < 5){
                    randomDestroyFarmland(plantToKill, 0.85);
                    replacePlantWith(plantToKill, Material.TALL_GRASS);
                }

                else if (randomMaterial < 25) {
                    destroyFarmland(plantToKill);
                    plantToKill.setType(Material.DEAD_BUSH);
                }

                else if (randomMaterial < 27) {
                    randomDestroyFarmland(plantToKill, 0.5);
                    // The plant is already replaced with AIR
                }

                else {
                    randomDestroyFarmland(plantToKill, 0.85);
                    plantToKill.setType(Material.GRASS);
                }
            }

            // ...

        } else if (instance.isAnAquaticPlant(plantToKill)) {

        }
    }


    /**
     * Destroys farmland below the broken block, replacing it with coarse dirt after a 1-tick delay.
     *
     * @param blockAboveFarmland Block above the Farmland to be destroyed.
     */
    public void destroyFarmland(Block blockAboveFarmland){
        Block u = blockAboveFarmland.getRelative(BlockFace.DOWN);
        if (u.getType().equals(Material.FARMLAND)) {
            double random = Math.random();
            // Schedule the replacement of farmland with coarse dirt with a 1-tick delay
            scheduler.runTaskLater(instance, () ->{
                logger.verbose("Replacing Farmland.");
                if(random < 0.65)
                    u.setType(Material.COARSE_DIRT);
                else
                    u.setType(Material.DIRT);
            },1 ); // 1 Tick delay
        }
    }

    public void randomDestroyFarmland(Block blockAboveFarmland, double destroyChance){
        double farmlandDestroyChance = Math.random();
        if(farmlandDestroyChance < destroyChance){
            destroyFarmland(blockAboveFarmland);
        }
    }

    public void replacePlantWith(Block plant, Material replaceWith){
        scheduler.runTaskLater(instance, () ->{
           plant.setType(replaceWith);
        },3 ); // 3 Ticks delay
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
        if(configManager.isFertilizer_passiv() || composterToDrain == null) {
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
        int newFillLevel;


        if (oldFillLevel == composter.getMaximumLevel()){
            newFillLevel = oldFillLevel-2;
        }else {
            newFillLevel = oldFillLevel-1;
        }

        composter.setLevel(newFillLevel);
        composterToDrain.setBlockData(composter);

    }

}
