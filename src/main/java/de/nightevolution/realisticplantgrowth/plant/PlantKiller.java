package de.nightevolution.realisticplantgrowth.plant;

import de.nightevolution.realisticplantgrowth.ConfigManagerOld;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * The PlantKiller class is responsible for managing the destruction and replacement of plants
 * based on specified configurations and probabilities. It is part of the RealisticPlantGrowth plugin.
 */
public class PlantKiller {

    private final RealisticPlantGrowth instance;
    private final ConfigManagerOld cm;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final VersionMapper vm;

    private final Set<Material> plantReplacementMaterials = new HashSet<>();

    /**
     * Constructs a new PlantKiller instance, initializing necessary dependencies.
     */
    public PlantKiller() {
        this.instance = RealisticPlantGrowth.getInstance();
        this.cm = instance.getConfigManager();
        this.vm = instance.getVersionMapper();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        this.scheduler = Bukkit.getScheduler();

        // Initialize plantReplacementMaterials
        plantReplacementMaterials.addAll(Set.of(
                vm.getGrassMaterial(),
                Material.TALL_GRASS,
                Material.DEAD_BUSH,
                Material.AIR
        ));
    }

    /**
     * Kills a plant by replacing it with a random {@link Material} based on its type.
     * Different replacement probabilities are applied based on the plant type.
     *
     * @param plantToKill The {@link Block} representing the plant to be killed.
     */
    public void killPlant(@NotNull Block plantToKill) {
        Material plantType = plantToKill.getType();

        if (vm.isAgriculturalPlant(plantToKill)) {

            // For Melon or Pumpkin stems, replace with a random material (5% tall grass, 2% air, 78% dead bush, 22% grass)
            if (plantType == Material.MELON_STEM || plantType == Material.PUMPKIN_STEM) {
                logger.verbose("Killing Melon / Pumpkin Stem");
                replaceWithRandomMaterial(plantToKill, 5, 7, 85, 3);
            } else {
                // For agricultural plants, replace with a random material (10% tall grass, 2% air, 38% dead bush, 62% grass)
                logger.verbose("Killing Agricultural Plant.");
                replaceWithRandomMaterial(plantToKill, 32, 3, 30, 1);
            }


        } else if (vm.isSapling(plantToKill)) {
            logger.verbose("Killing Sapling");
            replaceWithRandomMaterial(plantToKill, 0, 0, 1, 0);
        } else if (vm.isAnAquaticPlant(plantToKill.getType())) {
            logger.verbose("Killing AquaticPlant");
            replaceWithRandomAquaticMaterial(plantToKill, 35, 15, 1);
        } else if (plantType == Material.BROWN_MUSHROOM || plantType == Material.RED_MUSHROOM) {
            logger.verbose("Killing Mushroom");
            replaceWithRandomMaterial(plantToKill, 15, 1, 20, 1);
        } else {
            logger.verbose("No specific killing modifier for: " + plantType);
            replaceWithRandomMaterial(plantToKill, 7, 2, 30, 3);
        }

        // Play the death sound for the plant
        playPlantDeathSound(plantToKill);
    }

    /**
     * Replaces a specified plant {@link Block} with a randomly chosen {@link Material} based on configured weights.
     * The replacement {@link Material} can be short grass, tall grass, dead bush, or air, and the choice is influenced
     * by the specified weights.
     * The replacement also considers the type of the supporting {@link Block} beneath the plant.
     *
     * @param plantToKill      The {@link Block} representing the plant to be replaced.
     * @param shortGrassWeight The weight for short grass replacement.
     * @param tallGrassWeight  The weight for tall grass replacement.
     * @param deadBushWeight   The weight for dead bush replacement.
     * @param airWeight        The weight for air replacement.
     */
    private void replaceWithRandomMaterial(@NotNull Block plantToKill,
                                           double shortGrassWeight, double tallGrassWeight,
                                           double deadBushWeight, double airWeight) {

        plantToKill.setType(Material.AIR);

        Block supportingBlock = plantToKill.getRelative(BlockFace.DOWN);
        Material supportingBlockType = supportingBlock.getType();

        double maxWight = (shortGrassWeight + tallGrassWeight + airWeight + deadBushWeight);
        double randomMaterial = Math.random() * maxWight;

        // Using switch for a more readable structure
        Material selectedMaterial;

        if (randomMaterial < shortGrassWeight) {
            selectedMaterial = vm.getGrassMaterial();
        } else if (randomMaterial < (shortGrassWeight + tallGrassWeight)) {
            selectedMaterial = Material.TALL_GRASS;
        } else if (randomMaterial < (shortGrassWeight + tallGrassWeight + deadBushWeight)) {
            selectedMaterial = Material.DEAD_BUSH;
        } else {
            selectedMaterial = Material.AIR;
        }

        switch (selectedMaterial) {
            case AIR:
                replacePlantWith(plantToKill, Material.AIR);
                destroyFarmland(plantToKill);
                break;

            case TALL_GRASS:

                if (supportingBlockType == Material.FARMLAND || Tag.DIRT.getValues().contains(supportingBlockType)) {
                    replacePlantWith(plantToKill, Material.TALL_GRASS);
                    randomDestroyFarmland(plantToKill, 0.85);
                } else {
                    // Recursive call
                    replaceWithRandomMaterial(plantToKill, 0, 0, airWeight, deadBushWeight);
                }
                break;

            case DEAD_BUSH:
                if (supportingBlockType == Material.FARMLAND || Tag.DEAD_BUSH_MAY_PLACE_ON.getValues().contains(supportingBlockType)) {
                    replacePlantWith(plantToKill, Material.DEAD_BUSH);
                    destroyFarmland(plantToKill);
                } else {
                    // Recursive call
                    replaceWithRandomMaterial(plantToKill, shortGrassWeight, tallGrassWeight, airWeight, 0);
                }
                break;

            default: // GRASS || SHORT_GRASS
                if (supportingBlockType == Material.FARMLAND || Tag.DIRT.getValues().contains(supportingBlockType)) {
                    replacePlantWith(plantToKill, vm.getGrassMaterial());
                    randomDestroyFarmland(plantToKill, 0.75);
                } else {
                    // Recursive call
                    replaceWithRandomMaterial(plantToKill, 0, 0, airWeight, deadBushWeight);
                }
                break;
        }

    }

    /**
     * Replaces a specified aquatic plant {@link Block} with a randomly chosen {@link Material} based on configured weights.
     * The replacement {@link Material} can be sea grass, tall sea grass, or water, and the choice is influenced
     * by the specified weights.
     *
     * @param plantToKill        The {@link Block} representing the aquatic plant to be replaced.
     * @param seaGrassWeight     The weight for sea grass replacement.
     * @param tallSeaGrassWeight The weight for tall sea grass replacement.
     * @param waterWeight        The weight for water replacement.
     */
    private void replaceWithRandomAquaticMaterial(Block plantToKill,
                                                  double seaGrassWeight, double tallSeaGrassWeight, double waterWeight) {

        double maxWeight = (seaGrassWeight + tallSeaGrassWeight + waterWeight);
        double randomMaterial = Math.random() * maxWeight;

        Material selectedMaterial;

        if (randomMaterial < seaGrassWeight) {
            selectedMaterial = Material.SEAGRASS;
        } else if (randomMaterial < (seaGrassWeight + tallSeaGrassWeight)) {
            selectedMaterial = Material.TALL_SEAGRASS;
        } else {
            selectedMaterial = Material.WATER;
        }

        replacePlantWith(plantToKill, selectedMaterial);

    }


    /**
     * Destroys farmland by replacing it with either coarse dirt or dirt based on a random chance.
     *
     * @param blockAboveFarmland The {@link Block} representing the plant above the farmland to be destroyed.
     */
    public void destroyFarmland(Block blockAboveFarmland) {
        Block u = blockAboveFarmland.getRelative(BlockFace.DOWN);
        if (u.getType().equals(Material.FARMLAND)) {
            double random = Math.random();
            // Schedule the replacement of farmland with coarse dirt with a 1-tick delay
            scheduler.runTaskLater(instance, () -> {
                logger.verbose("Replacing Farmland.");
                if (random < 0.75)
                    u.setType(Material.COARSE_DIRT);
                else
                    u.setType(Material.DIRT);
            }, 1); // 1 Tick delay
        }
    }

    /**
     * Randomly destroys farmland based on the specified destroy-chance.
     *
     * @param blockAboveFarmland The {@link Block} representing the block above the farmland to be destroyed.
     * @param destroyChance      The probability of destroying the farmland, ranging from 0.0 to 1.0.
     */
    public void randomDestroyFarmland(Block blockAboveFarmland, double destroyChance) {
        double farmlandDestroyChance = Math.random();
        if (farmlandDestroyChance < destroyChance) {
            destroyFarmland(blockAboveFarmland);
        }
    }

    /**
     * Replaces a plant block with the specified material after a delay.
     *
     * @param plant       The {@link Block} representing the plant to be replaced.
     * @param replaceWith The {@link Material} to replace the plant block with.
     */
    public void replacePlantWith(Block plant, Material replaceWith) {
        scheduler.runTaskLater(instance, () -> plant.setType(replaceWith), 2); // 2 Ticks delay
    }


    /**
     * Plays a {@link Sound} and a visual {@link Effect} when a plant dies.
     * <p>
     * Only active, if plant_death_sound_effect is enabled in the configuration.
     * </p>
     *
     * @param plantToKill The {@link Block} representing the plant that is about to be killed.
     */
    public void playPlantDeathSound(@NotNull Block plantToKill) {

        Section soundEffectSection = cm.getPlant_death_sound_effect();

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
    public void reduceComposterFillLevelOf(Block composterToDrain) {
        // Check if passive fertilizer passiv mode is enabled
        if (cm.isFertilizer_passive() || composterToDrain == null) {
            return;
        }

        // Check if the provided block is a composter
        if (composterToDrain.getType() != Material.COMPOSTER) {
            logger.warn("Could not reduce fill level on Block: " + composterToDrain.getType());
            return;
        }

        // Check if the composter's fill level is already at 0
        Levelled composter = (Levelled) composterToDrain.getBlockData();
        if (composter.getLevel() == 0) {
            logger.warn("Composter fill level already at 0!");
            return;
        }

        // Reduce the composter's fill level
        int oldFillLevel = composter.getLevel();
        int newFillLevel;


        if (oldFillLevel == composter.getMaximumLevel()) {
            newFillLevel = oldFillLevel - 2;
        } else {
            newFillLevel = oldFillLevel - 1;
        }

        composter.setLevel(newFillLevel);
        composterToDrain.setBlockData(composter);

    }

}
