package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.*;
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
            if (plantType == Material.MELON_STEM || plantType == Material.PUMPKIN_STEM) {
                logger.verbose("Melon / Pumpkin Stem");
                replacePlantWithRandomReplacementMaterial(plantToKill, 5, 7, 85);
            }
            // farmland plants
            else if (instance.isAgriculturalPlant(plantToKill)){
                logger.verbose("Killing Agricultural Plant.");
                replacePlantWithRandomReplacementMaterial(plantToKill, 10, 12, 50);
            }

            // ...

        } else if (instance.isAnAquaticPlant(plantToKill)) {

        }

        playPlantDeathSound(plantToKill);
    }

    private void replacePlantWithRandomReplacementMaterial(Block plantToKill,
                                                           double tallGrass,  double air, double deadBush){
        plantToKill.setType(Material.AIR);

        double randomMaterial = Math.random()*100;

        if(randomMaterial < tallGrass){
            randomDestroyFarmland(plantToKill, 0.85);
            replacePlantWith(plantToKill, Material.TALL_GRASS);
        }

        else if (randomMaterial < air) {
            destroyFarmland(plantToKill);
            plantToKill.setType(Material.AIR);
        }

        else if (randomMaterial < deadBush) {
            destroyFarmland(plantToKill);
            plantToKill.setType(Material.DEAD_BUSH);
        }

        else {
            randomDestroyFarmland(plantToKill, 0.75);
            plantToKill.setType(Material.GRASS);
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
                if(random < 0.75)
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

    public void playPlantDeathSound (Block plantToKill){
        Section soundEffectSection = configManager.getPlant_death_sound_effect();
        if (soundEffectSection.getBoolean("enabled")) {
            Location loc = plantToKill.getLocation();
            World world = loc.getWorld();
            if (world == null) return;

            Sound sound = Sound.valueOf(soundEffectSection.getString("sound"));
            Effect effect = Effect.valueOf(soundEffectSection.getString("effect"));
            Float volume = soundEffectSection.getFloat("volume");
            Float pitch = soundEffectSection.getFloat("pitch");
            int data = soundEffectSection.getInt("data");

            world.playSound(loc, sound, volume, pitch);
            world.playEffect(loc, effect, data);
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
