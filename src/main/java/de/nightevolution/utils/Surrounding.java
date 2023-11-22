package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class representing the environment around a block that triggered a PlantGrowthEvent.
 * It holds information about the central block of the event, sources of ultraviolet (UV) light, and sources of fertilizer.
 * A logger is also included for outputting verbose and debug information.
 */
public class Surrounding {
    private final static RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final static ConfigManager configManager = instance.getConfigManager();
    private final BiomeChecker biomeChecker;
    private final static HashMap<Material, List<String>> biomeGroupsCache = new HashMap<>();


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
     * Logger for outputting information about the surrounding environment.
     */
    private final Logger logger;

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
    public Surrounding(Block centerBlock, List<Block> uvBlocks, List<Block> fertilizerBlocks) {
        this.centerBlock = centerBlock;
        this.plantType = centerBlock.getType();
        this.biome = centerBlock.getBiome();
        this.plantLocation = centerBlock.getLocation();

        uvSources = uvBlocks;
        fertilizerSources = fertilizerBlocks;

        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

        biomeChecker = new BiomeChecker(instance);
        validBiome = calcIsInValidBiome();
    }

    public static void clearCache(){
        biomeGroupsCache.clear();
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

        if(!(closestComposter == null))
            return closestComposter;

        if(fertilizerSources.isEmpty())
            return null;

        Comparator<Block> blockComparator = getBlockDistanceComparator();

        // Sorting the List
        fertilizerSources.sort(blockComparator);

        for (Block block : fertilizerSources){
            logger.verbose("Sorted Distance List:" + block.getLocation());
        }

        closestComposter = fertilizerSources.get(0);

        return closestComposter;
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
            if(comparison == 0) {
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
        for(Block uvSource : uvSources) {
            builder.append(uvSource).append(", ");
        }
        if (!uvSources.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }
        builder.append("]");

        builder.append(", fertilizerSources=[");
        for(Block fertilizerSource : fertilizerSources) {
            builder.append(fertilizerSource).append(", ");
        }
        if (!fertilizerSources.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }
        builder.append("]");

        if(closestComposter != null) {
            builder.append(", closestComposter=").append(closestComposter);
        } else {
            builder.append(", closestComposter=None");
        }

        builder.append('}');
        return builder.toString();
    }


    /**
     * Retrieves the biome of the center block.
     *
     * @return The biome of the center block.
     */
    public Biome getBiome(){
        return biome;
    }


    /**
     * Retrieves the material type of the center block.
     *
     * @return The material type of the center block.
     */
    public Material getType(){
        return plantType;
    }

    public boolean hasUVLightAccess(){
        List<Material> allValidUVBlocks = configManager.getUV_Blocks();

        if(uvSources == null || uvSources.isEmpty()){
            logger.verbose("No UV-Light access!");
            return false;
        }

        if(configManager.getRequire_All_UV_Blocks()){
            HashSet<Material> uvBlockMix = new HashSet<>();
            for (Block b : uvSources){
                uvBlockMix.add(b.getType());
            }
            return uvBlockMix.containsAll(allValidUVBlocks);

        }else{
            return true;
        }
    }

    public double getGrowthRate(){
        return instance.getGrowthModifierFor(this);

    }

    public double getDeathChance(){
        return instance.getDeathChanceFor(this);
    }


    private boolean calcIsInValidBiome(){

        if(biomeGroupsCache.containsKey(plantType)){

            if(biomeGroupsCache.get(plantType).isEmpty())
                return false;

            for (String biomeGroup : biomeGroupsCache.get(plantType)){
                Optional<Set<Biome>> biomeSet = biomeChecker.getBiomeListOf(biomeGroup);
                if (biomeSet.isPresent() && biomeSet.get().contains(biome)){
                    return true;
                }
            }
            return false;
        } // else search valid biome groups

        Optional<List<String>> allBiomeGroups = biomeChecker.getAllBiomeGroupsOf(biome);

        if(allBiomeGroups.isEmpty() || allBiomeGroups.get().isEmpty()){
            // new cache entry
            biomeGroupsCache.put(plantType, new ArrayList<>());
            return false;
        }

        // new cache entry
        biomeGroupsCache.put(plantType, allBiomeGroups.get());
        return calcIsInValidBiome(); // recursive call of this method
    }

    public boolean isInValidBiome(){
        return validBiome;
    }



    public boolean canApplyFertilizerBoost(){
        if(configManager.isFertilizer_enabled() && !getFertilizerSources().isEmpty()) {
            if (isInValidBiome()) { //TODO: Check closest Composter Fill level
                return true;
            }
            return configManager.isFertilizer_Enables_Growth_In_Bad_Biomes();
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
    public boolean isInDarkness(){
        int skyLightLevel = centerBlock.getLightFromSky();
        boolean hasNotMinSkyLight =  (configManager.getMin_Natural_Light() > skyLightLevel);
        return (hasNotMinSkyLight && !instance.canGrowInDark(centerBlock));
    }



}
