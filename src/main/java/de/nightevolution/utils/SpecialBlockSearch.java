package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for finding special blocks like UV light sources and composters (as fertilizer sources)
 * within a specified radius around a given block. The radius values are obtained from the {@link ConfigManager}.
 * It uses a singleton pattern to ensure only one instance is used throughout the application.
 */
public class SpecialBlockSearch {

    /**
     * A singleton instance of the SpecialBlockSearch class to ensure that only one instance is used throughout the application.
     * This instance is used to carry out searches for special blocks within a certain radius.
     */
    private static SpecialBlockSearch specialBlockSearch;
    private static RealisticPlantGrowth instance;
    private static ConfigManager configManager;
    private final Logger logger;

    private static final String logFile = "debug";

    /**
     * The search radius for UV light sources, specified in blocks.
     * This value is set based on the configuration settings.
     */
    private static int radiusUV;

    /**
     * The search radius for fertilizer sources, specified in blocks.
     * This value is set based on the configuration settings.
     */
    private static int radiusFertilizer;

    /**
     * Private constructor to enforce a Singleton pattern.
     */
    private SpecialBlockSearch() {
        specialBlockSearch = this;
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.getInstance(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    private static boolean debug_log = false;


    /**
     * Provides access to the singleton instance of SpecialBlockSearch, initializing it if it doesn't exist.
     * Upon each invocation, it updates the search radii for UV light sources and fertilizer sources from the config.
     *
     * @return The singleton instance of SpecialBlockSearch with updated search parameters.
     */
    public static SpecialBlockSearch get() {
        if (specialBlockSearch == null)
            new SpecialBlockSearch();

        instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();
        radiusUV = configManager.getUV_Radius();
        radiusFertilizer = configManager.getFertilizer_radius();
        debug_log = configManager.isDebug_log();

        return specialBlockSearch;
    }

    /**
     * Searches for UV light source and fertilizer blocks within a configured radius around the provided center block.
     * The search radius is determined by the configuration settings. This method performs an O(n^3) search
     * in the area around the block, which may be inefficient for large radii.
     *
     * @param startingBlock The block from which the search radius extends.
     * @param blockState The (not placed) {@link BlockState} of startingBlock. (Used for growth rate checks.)
     * @return A {@link Surrounding} object containing all found UV light sources and fertilizer blocks within the radius.
     */
    public Surrounding surroundingOf(Block startingBlock, BlockState blockState) {

        int radius;

        boolean uvEnabled = configManager.isUV_Enabled();
        boolean fertilizerEnabled = configManager.isFertilizer_enabled();

        Set<Material> uvMaterials = configManager.getUV_Blocks();

        // Determine the search radius
        if (uvEnabled && fertilizerEnabled) {
            radius = Math.max(radiusUV, radiusFertilizer);
        } else if (uvEnabled) {
            radius = radiusUV;
        } else if (fertilizerEnabled) {
            radius = radiusFertilizer;
        } else {
            // No special block search required.
            return new Surrounding(startingBlock, blockState, null, null);
        }

        List<Block> fertilizerSources = new ArrayList<>();
        List<Block> uvSources = new ArrayList<>();

        Block relativeBlock;
        Location relativeBlockLocation;
        Location startingBlockLocation = startingBlock.getLocation();

        // We are searching around a block, that triggered a PlantGrowthEvent.
        // This requires an o(n^3) area scan. (Im open for better ideas)
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    relativeBlock = startingBlock.getRelative(x, y, z);
                    relativeBlockLocation = relativeBlock.getLocation();


                    if (uvEnabled && isBlockWithinRadius(startingBlockLocation, relativeBlockLocation, radiusUV) && uvMaterials.contains(relativeBlock.getType())) {
                        uvSources.add(startingBlock.getRelative(x, y, z));
                        if(debug_log)
                            logger.logToFile("[" + relativeBlockLocation + "] Located UV-Source: " + relativeBlock.getType(), logFile);
                    }

                    // TODO: Check Composter for fertilizer nbt tag
                    if (fertilizerEnabled && isBlockWithinRadius(startingBlockLocation,relativeBlockLocation, radiusFertilizer) && relativeBlock.getType() == Material.COMPOSTER) {
                        fertilizerSources.add(startingBlock.getRelative(x, y, z));
                        if(debug_log)
                            logger.logToFile("[" + relativeBlockLocation + "] Located Fertilizer-Source: " + relativeBlock.getType(), logFile);
                    }
                }
            }
        }

        Surrounding s = new Surrounding(startingBlock, blockState, uvSources, fertilizerSources);
        if (debug_log)
            logger.logToFile(s.toString(), logFile);

        return s;
    }

    /**
     * Searches for UV light source and fertilizer blocks within a configured radius around the provided center block.
     * The search radius is determined by the configuration settings. This method performs an O(n^3) search
     * in the area around the block, which may be inefficient for large radii.
     *
     * @param startingBlock The block from which the search radius extends.
     * @return A {@link Surrounding} object containing all found UV light sources and fertilizer blocks within the radius.
     */
    public Surrounding surroundingOf(Block startingBlock) {
        return surroundingOf(startingBlock, startingBlock.getBlockData().createBlockState());
    }

    /**
     * Method only used for debugging purpose
     * @param startingBlock
     * @param radius
     * @return
     */
    public Surrounding surroundingOf(Block startingBlock, int radius) {
        radiusUV = radius;
        radiusFertilizer = radius;
        return surroundingOf(startingBlock);
    }

    /**
     * Helper method to determine if a block is within a specified radius of a starting location.
     * It compares the squared distances to avoid the computational cost of square root calculations.
     *
     * @param startingLocation The central location from which the radius extends.
     * @param relativeLocation The location of the block to check.
     * @param radius           The search radius to compare against, which can be for either UV or fertilizer.
     * @return true if the block is within or equal the specified radius, false otherwise.
     *
     */
    private boolean isBlockWithinRadius(Location startingLocation, Location relativeLocation, int radius){
        double betterRadius = radius + 0.33;
        double radiusSquared = betterRadius * betterRadius;
        return startingLocation.distanceSquared(relativeLocation) <= radiusSquared;
    }

}