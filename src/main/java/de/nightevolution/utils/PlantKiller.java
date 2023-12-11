package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

/**
 * The PlantKiller class is responsible for managing the destruction and replacement of plants
 * based on specified configurations and probabilities. It is part of the RealisticPlantGrowth plugin.
 */
public class PlantKiller {

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private final Logger logger;

    private final BukkitScheduler scheduler;

    /**
     * Constructs a new PlantKiller instance, initializing necessary dependencies.
     */
    public PlantKiller(){
        this.instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        scheduler = Bukkit.getScheduler();
    }

    /**
     * Kills a plant by replacing it with a random {@link Material} based on its type.
     * Different replacement probabilities are applied based on the plant type.
     *
     * @param plantToKill The {@link Block} representing the plant to be killed.
     */
    public void killPlant (Block plantToKill){
        Material plantType = plantToKill.getType();

        if(instance.isAgriculturalPlant(plantToKill)){

            // For Melon or Pumpkin stems, replace with a random material (5% tall grass, 2% air, 78% dead bush, 22% grass)
            if (plantType == Material.MELON_STEM || plantType == Material.PUMPKIN_STEM) {
                logger.verbose("Killing Melon / Pumpkin Stem");
                replacePlantWithRandomReplacementMaterial(plantToKill, 5, 7, 85);
            }

            else {
                // For agricultural plants, replace with a random material (10% tall grass, 2% air, 38% dead bush, 62% grass)
                logger.verbose("Killing Agricultural Plant.");
                replacePlantWithRandomReplacementMaterial(plantToKill, 10, 12, 50);
            }


        }

        else if (instance.isSapling(plantToKill)) {
            // For saplings, replace always with dead bush (0% tall grass, 0% air, 100% dead bush, 0% grass)
            logger.verbose("Killing Sapling");
            replacePlantWithRandomReplacementMaterial(plantToKill, 0, 0, 100);
        }

        else if (instance.isAnAquaticPlant(plantToKill)) {
            logger.verbose("Killing AquaticPlant");
            replacePlantWithRandomAquaticReplacementMaterial(plantToKill, 5, 45);
            // Handle aquatic plants if implemented...
        }

        else if (plantType == Material.BROWN_MUSHROOM || plantType == Material.RED_MUSHROOM) {
            // For mushrooms, replace with a random material (10% tall grass, 2% air, 73% dead bush, 15% grass)
            logger.verbose("Killing Mushroom");
            replacePlantWithRandomReplacementMaterial(plantToKill, 10, 12, 85);
        }

        else {
            logger.verbose("No specific killing modifier for: " + plantType);
            replacePlantWithRandomReplacementMaterial(plantToKill, 10, 12, 85);
        }

        // Play the death sound for the plant
        playPlantDeathSound(plantToKill);
    }

    /**
     * Replaces a plant {@link Block} with a random replacement {@link Material} based on specified probabilities.
     * TODO: Make this method more flexible
     *
     * @param plantToKill The Block representing the plant to be replaced.
     * @param tallGrass The probability of replacing with tall grass (0 to air).
     * @param air The probability of removing the plant (setting to AIR) (tallGrass to deadBush).
     * @param deadBush The probability of replacing with a dead bush (air to 100).
     */
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
            plantToKill.setType(Material.SHORT_GRASS);
        }


        Block supportingBlock = plantToKill.getRelative(BlockFace.DOWN);
        if(!supportingBlock.canPlace(plantToKill.getBlockData())){
            plantToKill.setType(Material.AIR);
        }

    }

    private void replacePlantWithRandomAquaticReplacementMaterial(Block plantToKill,
                                                           double tallSeeGrass, double seaGrass){
        plantToKill.setType(Material.WATER);

        double randomMaterial = Math.random()*100;

        if(randomMaterial < tallSeeGrass){
            replacePlantWith(plantToKill, Material.TALL_SEAGRASS);
        }

        else if (randomMaterial < seaGrass) {
            plantToKill.setType(Material.SEAGRASS);
        }

        else {
            plantToKill.setType(Material.WATER);
        }

        Block supportingBlock = plantToKill.getRelative(BlockFace.DOWN);
        if(!supportingBlock.canPlace(plantToKill.getBlockData())){
            plantToKill.setType(Material.WATER);
        }

    }

    /**
     * Destroys farmland by replacing it with either coarse dirt or dirt based on a random chance.
     *
     * @param blockAboveFarmland The {@link Block} representing the plant above the farmland to be destroyed.
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

    /**
     * Randomly destroys farmland based on the specified destroy chance.
     *
     * @param blockAboveFarmland The {@link Block} representing the block above the farmland to be destroyed.
     * @param destroyChance The probability of destroying the farmland, ranging from 0.0 to 1.0.
     */
    public void randomDestroyFarmland(Block blockAboveFarmland, double destroyChance){
        double farmlandDestroyChance = Math.random();
        if(farmlandDestroyChance < destroyChance){
            destroyFarmland(blockAboveFarmland);
        }
    }

    /**
     * Replaces a plant block with the specified material after a delay.
     *
     * @param plant The {@link Block} representing the plant to be replaced.
     * @param replaceWith The {@link Material} to replace the plant block with.
     */
    public void replacePlantWith(Block plant, Material replaceWith){
        scheduler.runTaskLater(instance, () ->{
           plant.setType(replaceWith);
        },3 ); // 3 Ticks delay
    }


    /**
     * Plays a {@link Sound} and a visual {@link Effect} when a plant dies.
     * <p>
     * Only active, if plant_death_sound_effect is enabled in the configuration.
     * </p>
     * @param plantToKill The {@link Block} representing the plant that is about to be killed.
     */
    public void playPlantDeathSound(@NotNull Block plantToKill) {

        Section soundEffectSection = configManager.getPlant_death_sound_effect();

        // Check if the sound effect is enabled in the configuration
        if (soundEffectSection.getBoolean("enabled")) {

            Location loc = plantToKill.getLocation();
            World world = loc.getWorld();
            if (world == null) return;

            // Extract sound effect parameters from the configuration file
            Sound sound = Sound.valueOf(soundEffectSection.getString("sound"));
            Effect effect = Effect.valueOf(soundEffectSection.getString("effect"));
            Float volume = soundEffectSection.getFloat("volume");
            Float pitch = soundEffectSection.getFloat("pitch");
            int data = soundEffectSection.getInt("data");

            // Play the sound effect at the specified location with the given parameters
            world.playSound(loc, sound, volume, pitch);

            // Play the visual effect at the specified location with the given parameters
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
