package de.nightevolution.utils.plant;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.enums.DeathChanceType;
import de.nightevolution.utils.enums.GrowthModifierType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing the environment around a block that triggered a PlantGrowthEvent.
 * It holds information about the central block of the event, sources of ultraviolet (UV) light, and sources of fertilizer.
 * A logger is also included for outputting verbose and debug information.
 */
public class Surrounding {
    private final static RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final static ConfigManager cm = instance.getConfigManager();
    private final BiomeChecker biomeChecker;
    private final Modifier modifier;
    private final Logger logger;


    /**
     * The central block associated with the plant growth event.
     */
    private final Block centerBlock;

    private final Material plantType;

    private final Location plantLocation;

    private final Biome biome;

    /**
     * The closest composter block to the central block.
     */
    private Block closestComposter;

    /**
     * A list of UV light Blocks in the surrounding of the centerBlock.
     */
    private final List<Block> uvSources;

    /**
     * A list of Fertilizer (Composter) Blocks in the surrounding of the centerBlock.
     */
    private final List<Block> fertilizerSources;

    /**
     * Is the centerBlock in a valid (allowed) Biome?
     */
    private final boolean validBiome;


    /**
     * Constructs a Surrounding object representing the environmental conditions around a central block.
     * This class encapsulates information about the center block, UV light sources, fertilizer sources,
     * and the darkness status of the environment.
     *
     * @param centerBlock      The central block around which the environmental conditions are assessed.
     * @param uvBlocks         The list of blocks representing UV light sources in the surrounding area.
     * @param fertilizerBlocks The list of blocks representing fertilizer sources in the surrounding area.
     */
    public Surrounding(Block centerBlock, BlockState blockState, List<Block> uvBlocks, List<Block> fertilizerBlocks) {
        this.centerBlock = centerBlock;
        this.plantType = blockState.getType();
        this.biome = centerBlock.getBiome();
        this.plantLocation = centerBlock.getLocation();

        uvSources = uvBlocks;
        fertilizerSources = fertilizerBlocks;

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

        biomeChecker = new BiomeChecker(plantType, biome);
        validBiome = biomeChecker.isValid();

        modifier = getModifier();

    }


    /**
     * Retrieves the modifier for plant growth based on various conditions such as biome, light, and fertilizer effects.
     *
     * @return A Modifier object representing the calculated growth and death modifiers for the plant.
     * @throws YAMLException If there is an error while reading modifier values from the configuration file.
     */
    private Modifier getModifier() {

        boolean uvLightEnabled = cm.isUV_Enabled();
        boolean fertilizerEnabled = cm.isFertilizer_enabled();
        boolean isDark = isInDarkness();


        if (logger.isVerbose()) {
            logger.verbose("PlantType: " + plantType);
            logger.verbose("CheckForSpecialCases:");
            logger.verbose("  - uvLightEnabled: " + uvLightEnabled);
            logger.verbose("  - fertilizerEnabled: " + fertilizerEnabled);
            logger.verbose("  - isDark: " + isDark);
            logger.verbose("  - validBiome: " + validBiome);
        }

        Modifier tempModifier;

        // Kill Plant Case
        if ((!validBiome && !fertilizerEnabled) || (isDark && !uvLightEnabled)) {
            logger.verbose("Death Modifier selected.");
            return new Modifier();
        }

        // Fertilizer Case
        else if (!validBiome && !isDark && fertilizerEnabled) {
            logger.verbose("Invalid Biome Modifier selected.");

            if (!canApplyFertilizerBoost()) {
                return new Modifier();
            }

            return new Modifier(plantType, biomeChecker.getMatchingBiomeGroup(),
                    GrowthModifierType.FERTILIZER_INVALID_BIOME,
                    DeathChanceType.FERTILIZER_INVALID_BIOME);
        }

        // UV-Light Case
        else if (validBiome && isDark && uvLightEnabled) {
            logger.verbose("UV-Light Modifier selected.");
            if (hasUVLightAccess()) {
                tempModifier = new Modifier(plantType, biomeChecker.getMatchingBiomeGroup(),
                        GrowthModifierType.UVLightGrowthRate,
                        DeathChanceType.UVLightDeathChance);
            } else {
                return new Modifier();
            }
        }

        // Special Case
        else if (!validBiome && isDark && uvLightEnabled && fertilizerEnabled) {
            logger.verbose("Special Case Modifier selected.");
            if (hasUVLightAccess() && canApplyFertilizerBoost()) {
                tempModifier = new Modifier(plantType, biomeChecker.getMatchingBiomeGroup(),
                        GrowthModifierType.UVLightGrowthRate,
                        DeathChanceType.UVLightDeathChance);
                tempModifier.setSpecialCase(true);
            } else {
                return new Modifier();
            }
        }

        // Normal-Case (Fertilizer-Boost can still be applied)
        else {
            tempModifier = new Modifier(plantType, biomeChecker.getMatchingBiomeGroup(),
                    GrowthModifierType.GrowthRate,
                    DeathChanceType.NaturalDeathChance);
        }


        logger.verbose("Using growthModifierType: " + tempModifier.getGrowthModifierType());
        logger.verbose("Using deathModifierType: " + tempModifier.getDeathChanceType());


        if (canApplyFertilizerBoost()) {
            logger.verbose("Applying fertilizer effects.");
            tempModifier.applyFertilizerEffects();
        }

        return tempModifier;

    }

    /**
     * Retrieves the central block.
     *
     * @return The central block around which the environment is defined.
     */
    public Block getCenterBlock() {
        return centerBlock;
    }

    /**
     * Retrieves the biome of the center block.
     *
     * @return The biome of the center block.
     */
    public Biome getBiome() {
        return biome;
    }

    /**
     * Retrieves the list of UV source blocks.
     *
     * @return A list of blocks that provide UV light.
     */
    public List<Block> getUvSources() {
        return uvSources;
    }

    /**
     * Retrieves the list of fertilizer source blocks.
     *
     * @return A list of blocks that provide fertilizer.
     */
    public List<Block> getFertilizerSources() {
        return fertilizerSources;
    }

    /**
     * Identifies and retrieves the closest composter block to the center block from the list of fertilizer sources.
     * If there are multiple blocks at the same distance, one is randomly chosen. Returns null if no fertilizers are present.
     *
     * @return The closest composter block or null if there are no fertilizer sources.
     */
    public Block getClosestComposter() {

        if (!(closestComposter == null))
            return closestComposter;

        if (fertilizerSources.isEmpty())
            return null;

        Comparator<Block> blockComparator = getBlockDistanceComparator();

        // Sorting the List
        fertilizerSources.sort(blockComparator);

        if (cm.isVerbose()) {
            logger.verbose("Sorted fertilizer blocks location list:");
            for (Block block : fertilizerSources) {
                logger.verbose("  - " + block.getLocation());
            }
        }
        if (cm.isFertilizer_passiv()) {
            closestComposter = fertilizerSources.getFirst();
            return closestComposter;

        }

        for (Block composterBlock : fertilizerSources) {
            Levelled composter = (Levelled) composterBlock.getBlockData();
            if (composter.getLevel() > 0) {
                closestComposter = composterBlock;
                return closestComposter;
            }
        }
        logger.verbose("All Composters fill levels are empty.");
        return null;
    }


    public boolean hasUVLightAccess() {
        Set<Material> allValidUVBlocks = cm.getUV_Blocks();

        if (uvSources == null || uvSources.isEmpty()) {
            logger.verbose("No UV-Light access!");
            return false;
        }

        if (cm.getRequire_All_UV_Blocks()) {
            HashSet<Material> uvBlockMix = new HashSet<>();
            for (Block b : uvSources) {
                uvBlockMix.add(b.getType());
            }
            return uvBlockMix.containsAll(allValidUVBlocks);

        } else {
            return true;
        }
    }


    /**
     * Checks whether a fertilizer boost can be applied based on the plugin's configuration and surrounding conditions.
     *
     * @return True if a fertilizer boost can be applied, false otherwise.
     */
    public boolean canApplyFertilizerBoost() {
        if (cm.isFertilizer_enabled() && !getFertilizerSources().isEmpty()) {

            if (cm.isFertilizer_passiv()) {
                if (isInValidBiome())
                    return true;

                return cm.isFertilizer_Enables_Growth_In_Invalid_Biomes();
            }

            return getClosestComposter() != null;

        }
        return false;
    }


    /**
     * Calculates the darkness status of a block based on its natural sky light level and configuration settings.
     * The environment is considered dark if the natural sky light is lower than the set value in the configuration
     * and the block type does not allow growth in the dark.
     *
     * @return {@code true} if the environment is dark; {@code false} otherwise.
     */
    // TODO: Check Light level based on plant. (current version don't work for vines)
    public boolean isInDarkness() {
        int skyLightLevel = centerBlock.getRelative(BlockFace.UP).getLightFromSky();
        logger.verbose("skyLightLevel: " + skyLightLevel);
        boolean hasNotMinSkyLight = (cm.getMin_Natural_Light() > skyLightLevel);
        logger.verbose("hasNotMinSkyLight: " + hasNotMinSkyLight);
        logger.verbose("canGrowInDark: " + instance.canGrowInDark(plantType));
        return (hasNotMinSkyLight && !instance.canGrowInDark(plantType));
    }

    /**
     * If Fertilizer was used to calculate the GrowthRate and DeathChance, this method return true.
     * Use this method to check, when to drain the composter fill level.
     *
     * @return true if fertilizer was used false if not
     */
    public boolean usedFertilizer() {
        return modifier.isFertilizerUsed();
    }

    /**
     * Checks whether the current location is in a valid biome for plant growth.
     *
     * @return True if the current location is in a valid biome, false otherwise.
     */
    public boolean isInValidBiome() {
        return validBiome;
    }

    /**
     * Gets the current growth rate modifier for the plant.
     *
     * @return The growth rate modifier value.
     */
    public double getGrowthRate() {
        return modifier.getGrowthModifier();
    }

    /**
     * Gets the current death chance modifier for the plant.
     *
     * @return The death chance modifier value.
     */
    public double getDeathChance() {
        return modifier.getDeathChance();
    }

    /**
     * Creates and returns a comparator for Block objects based on their squared distance from the center block.
     * If two blocks have the same squared distance to the center block, the comparator will randomly choose one to prioritize.
     * This randomness is logged as a verbose message. The method is guaranteed not to return null.
     *
     * @return A not-null Comparator object that compares Block objects based on their squared distance to the center block.
     */
    @NotNull
    private Comparator<Block> getBlockDistanceComparator() {
        Location centerBlockLocation = plantLocation;

        return (b1, b2) -> {

            int comparison = Double.compare(b1.getLocation().distanceSquared(centerBlockLocation),
                    b2.getLocation().distanceSquared(centerBlockLocation));

            // If 2 Blocks have the same distance to the centerBlock, a random one is chosen to prioritize.
            if (comparison == 0) {
                logger.verbose("Fertilizer at same distance -> random decision.");
                return Math.random() < 0.5 ? -1 : 1;
            }

            return comparison;
        };
    }

    /**
     * Returns a string representation of the Surrounding object.
     * The string includes representations of the center block, UV sources, and fertilizer sources.
     * If the list of UV sources or fertilizer sources is not empty, each element is included in the representation.
     * If the closest composter is not null, its representation is included; otherwise, "None" is indicated.
     *
     * @return A {@code String} that textually represents the current state of the Surrounding object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Surrounding{").append(System.lineSeparator());
        builder.append("centerBlock=[");
        builder.append(plantLocation.getBlockX()).append(" | ");
        builder.append(plantLocation.getBlockY()).append(" | ");
        builder.append(plantLocation.getBlockZ()).append("] ");
        builder.append(System.lineSeparator());

        builder.append(", uvSources=[");
        for (Block uvSource : uvSources) {
            builder.append(uvSource).append(", ");
        }
        if (!uvSources.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }
        builder.append("]");

        builder.append(", fertilizerSources=[");
        for (Block fertilizerSource : fertilizerSources) {
            builder.append(fertilizerSource).append(", ");
        }
        if (!fertilizerSources.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }
        builder.append("]");

        if (closestComposter != null) {
            builder.append(", closestComposter=").append(closestComposter);
        } else {
            builder.append(", closestComposter=None");
        }

        builder.append('}');
        return builder.toString();
    }

}
