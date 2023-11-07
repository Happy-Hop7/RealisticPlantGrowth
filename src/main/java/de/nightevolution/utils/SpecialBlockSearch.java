package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

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
    SpecialBlockSearch specialBlockSearch;

    /**
     * The ConfigManager instance used to access configuration settings such as radii for UV and fertilizer searches,
     * and to check if UV light sources and fertilizer (composters) are enabled.
     */
    ConfigManager configManager;

    /**
     * Logger instance for logging verbose and debug information.
     * The logger is configured based on the application's settings for verbosity and debug mode.
     */
    Logger logger;

    /**
     * The search radius for UV light sources, specified in blocks.
     * This value is set based on the configuration settings.
     */
    private int radiusUV;

    /**
     * The search radius for fertilizer sources, specified in blocks.
     * This value is set based on the configuration settings.
     */
    private int radiusFertilizer;

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private SpecialBlockSearch() {
        specialBlockSearch = this;
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.getInstance(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    /**
     * Provides access to the singleton instance of SpecialBlockSearch, initializing it if it doesn't exist.
     * Upon each invocation, it updates the search radii for UV light sources and fertilizer sources from the config.
     *
     * @return The singleton instance of SpecialBlockSearch with updated search parameters.
     */
    public SpecialBlockSearch get() {
        if (specialBlockSearch == null)
            new SpecialBlockSearch();

        configManager = ConfigManager.get();
        radiusUV = configManager.getUv_radius();
        radiusFertilizer = configManager.getFertilizer_radius();

        return specialBlockSearch;
    }

    /**
     * Searches for UV light source and fertilizer blocks within a configured radius around the provided center block.
     * The search radius is determined by the configuration settings. This method performs an O(n^3) search
     * in the area around the block, which may be inefficient for large radii.
     *
     * @param startingBlock The block from which the search radius extends.
     * @return A {@link Surrounding} object containing all found UV light sources and fertilizer blocks within the radius,
     * or null if no search is required due to configuration.
     */
    public Surrounding surroundingOf(Block startingBlock) {

        int radius;

        boolean uvEnabled = configManager.isUv_enabled();
        boolean fertilizerEnabled = configManager.isFertilizer_enabled();

        List<Material> uvMaterials = configManager.getUv_blocks();

        // Determine the search radius
        if (uvEnabled && fertilizerEnabled) {
            radius = Math.max(radiusUV, radiusFertilizer);
        } else if (uvEnabled) {
            radius = radiusUV;
        } else if (fertilizerEnabled) {
            radius = radiusFertilizer;
        } else {
            // No special block search required.
            return null;
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

                    logger.verbose("Relative Block: " + x + " " + y + " " + z);

                    if (uvEnabled && isBlockWithinRadius(startingBlockLocation, relativeBlockLocation, radiusUV) && uvMaterials.contains(relativeBlock.getType())) {
                        uvSources.add(startingBlock.getRelative(x, y, z));
                        logger.verbose("  - Found UV-Source.");
                    }

                    // TODO: Check Composter for fertilizer nbt tag
                    if (fertilizerEnabled && isBlockWithinRadius(startingBlockLocation,relativeBlockLocation, radiusFertilizer) && relativeBlock.getType() == Material.COMPOSTER) {
                        fertilizerSources.add(startingBlock.getRelative(x, y, z));
                        logger.verbose("  - Found Fertilizer-Source.");
                    }
                }
            }
        }


        return new Surrounding(startingBlock, uvSources, fertilizerSources);
    }

    /**
     * Helper method to determine if a block is within a specified radius of a starting location.
     * It compares the squared distances to avoid the computational cost of square root calculations.
     *
     * @param startingLocation The central location from which the radius extends.
     * @param relativeLocation The location of the block to check.
     * @param radius           The search radius to compare against, which can be for either UV or fertilizer.
     * @return true if the block is within or equal the specified radius, false otherwise.
     */
    private boolean isBlockWithinRadius(Location startingLocation, Location relativeLocation, int radius){
        int radiusSquared = radius * radius;
        return startingLocation.distanceSquared(relativeLocation) <= radiusSquared;
    }

}