package de.nightevolution.realisticplantgrowth.utils.plant;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for finding special blocks like UV light sources and composters (as fertilizer sources)
 * within a specified radius around a given block. The radius values are obtained from the {@link ConfigManager}.
 * It uses a singleton pattern to ensure only one instance is used throughout the application.
 */
public class SpecialBlockSearch {

    /**
     * Singleton instance ensuring only one SpecialBlockSearch exists per plugin lifecycle.
     * Initialized lazily on first access via {@link #get()}.
     */
    private static SpecialBlockSearch specialBlockSearch;

    /**
     * Reference to the main plugin instance for accessing global plugin state.
     */
    private static RealisticPlantGrowth instance;

    /**
     * Configuration manager providing access to plugin settings and block definitions.
     */
    private static ConfigManager configManager;

    /**
     * Logger instance for debugging and monitoring search operations.
     * Configured based on plugin verbosity and debug settings.
     */
    private final Logger logger;

    /**
     * Log file identifier for debugging output related to special block searches.
     */
    private static final String logFile = "debug";

    /**
     * Maximum search radius for UV light sources in blocks.
     * Updated from configuration on each {@link #get()} call to ensure current settings.
     */
    private static int radiusUV;

    /**
     * Maximum search radius for fertilizer sources in blocks.
     * Updated from configuration on each {@link #get()} call to ensure current settings.
     */
    private static int radiusFertilizer;

    /**
     * Optimized search radius representing the maximum of UV and fertilizer radii.
     * Used to minimize the search area when only one feature is enabled or when
     * radii differ significantly. Precomputed for performance optimization.
     */
    private static int searchRadius;

    /**
     * Configuration flag indicating whether UV light source detection is enabled.
     * When false, UV-related searches are skipped entirely for better performance.
     */
    private static boolean uvEnabled;

    /**
     * Configuration flag indicating whether fertilizer source detection is enabled.
     * When false, fertilizer-related searches are skipped entirely for better performance.
     */
    private static boolean fertilizerEnabled;

    /**
     * Private constructor enforcing singleton pattern.
     * Initializes logger with appropriate verbosity and debug settings from the main plugin.
     */
    private SpecialBlockSearch() {
        specialBlockSearch = this;
        logger = new Logger(this.getClass().getSimpleName(),
                RealisticPlantGrowth.isVerbose(),
                RealisticPlantGrowth.isDebug());

    }

    /**
     * Debug logging flag from configuration. When true, detailed search results
     * are written to the debug log file for troubleshooting purposes.
     */
    private static boolean debug_log;


    /**
     * Retrieves the singleton instance of SpecialBlockSearch with up-to-date configuration.
     *
     * <p>This method performs the following operations on each call:
     * <ul>
     *   <li>Creates the singleton instance if it doesn't exist</li>
     *   <li>Updates all configuration-dependent fields from the current config</li>
     *   <li>Optimizes the search radius based on enabled features</li>
     * </ul>
     *
     * @return the singleton SpecialBlockSearch instance with current configuration applied
     */
    public static SpecialBlockSearch get() {
        if (specialBlockSearch == null)
            new SpecialBlockSearch();

        // Update instance references and configuration
        instance = RealisticPlantGrowth.getInstance();
        configManager = instance.getConfigManager();

        // Load search parameters from configuration
        radiusUV = configManager.getUV_Radius();
        radiusFertilizer = configManager.getFertilizer_radius();
        uvEnabled = configManager.isUV_Enabled();
        fertilizerEnabled = configManager.isFertilizer_enabled();
        debug_log = configManager.isDebug_log();

        // Optimize search radius based on enabled features
        // This prevents unnecessary iteration when features are disabled
        if (uvEnabled && fertilizerEnabled) {
            searchRadius = Math.max(radiusUV, radiusFertilizer);
        } else if (uvEnabled) {
            searchRadius = radiusUV;
        } else if (fertilizerEnabled) {
            searchRadius = radiusFertilizer;
        } else {
            searchRadius = 0; // No search needed when both features are disabled
        }

        return specialBlockSearch;
    }

    /**
     * Searches for UV light source and fertilizer blocks within a configured radius around the provided center block.
     * The search radius is determined by the configuration settings. This method performs an O(n^3) search
     * in the area around the block, which may be inefficient for large radii.
     *
     * @param startingBlock The block from which the search radius extends.
     * @param blockState    The (not placed) {@link BlockState} of startingBlock. (Used for growth rate checks.)
     * @return A {@link Surrounding} object containing all found UV light sources and fertilizer blocks within the radius.
     */
    public Surrounding surroundingOf(Block startingBlock, BlockState blockState) {

        // Early exit optimization - no search needed when features are disabled
        if (searchRadius <= 0 || (!uvEnabled && !fertilizerEnabled)) {
            // Return empty surrounding when no special block search is required
            return new Surrounding(startingBlock, blockState, null, null);
        }

        // Pre-calculate loaded chunks to prevent chunk loading during search
        // This addresses issue #26 where accessing unloaded chunks could throw some errors
        Set<Chunk> loadedChunks = getLoadedChunks(startingBlock);

        // Get UV material definitions from configuration
        Set<Material> uvMaterials = configManager.getUV_Blocks();

        // Initialize result collections with reasonable initial capacities
        List<Block> fertilizerSources = new ArrayList<>(10);  // Composters are typically sparse
        List<Block> uvSources = new ArrayList<>(50);          // Light sources are more common

        // Pre-calculate squared radii to avoid expensive square root operations in distance checks
        int uvRadiusSquared = radiusUV * radiusUV;
        int fertilizerRadiusSquared = radiusFertilizer * radiusFertilizer;

        // Cache starting coordinates for performance
        int startX = startingBlock.getX();
        int startY = startingBlock.getY();
        int startZ = startingBlock.getZ();

        // We are searching around a block, that triggered a PlantGrowthEvent.
        // This requires an o(n^3) area scan.
        // This code gets obsolete in the 1.0 release of the plugin
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {

                    // Calculate absolute coordinates
                    int blockX = startX + x;
                    int blockY = startY + y;
                    int blockZ = startZ + z;

                    World world = startingBlock.getWorld();
                    Block relativeBlock = world.getBlockAt(blockX, blockY, blockZ);

                    // Skip blocks in unloaded chunks to prevent forced chunk loading
                    if (!loadedChunks.contains(relativeBlock.getChunk())) {
                        continue;
                    }

                    Material blockType = relativeBlock.getType();

                    // Pre-calculate distance squared once
                    double distanceSquared = (x * x) + (y * y) + (z * z);

                    // Check UV sources
                    if (uvEnabled) {
                        if (uvMaterials.contains(blockType) && distanceSquared <= uvRadiusSquared){
                            uvSources.add(relativeBlock);
                            if (debug_log)
                                logger.logToFile("[" + relativeBlock.getLocation() + "] Located UV-Source: " + blockType, logFile);
                        }
                    }

                    // TODO: Check Composter for fertilizer nbt tag
                    // Check fertilizer sources
                    if (fertilizerEnabled) {
                        if (blockType == Material.COMPOSTER && distanceSquared <= fertilizerRadiusSquared){
                            fertilizerSources.add(relativeBlock);
                            if (debug_log)
                                logger.logToFile("[" + relativeBlock.getLocation() + "] Located Fertilizer-Source: " + blockType, logFile);
                        }
                    }
                }
            }
        }

        // Create and return the surrounding data structure
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
     *
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
     * Retrieves all loaded and generated chunks within a specified search radius around a starting block.
     *
     * <p>This method computes the chunk boundaries based on the configured search radius and returns
     * only those chunks that are both loaded in memory and fully generated. This is essential for
     * safe block operations to prevent triggering unwanted chunk loading or accessing incomplete terrain.</p>
     *
     * <p>The search area is determined by the {@code searchRadius} field, creating a square region
     * centered on the starting block. Chunk coordinates are calculated using bit shifting for
     * optimal performance (dividing by 16).</p>
     *
     * @param startingBlock the center block around which to search for loaded chunks.
     *                     Must not be null and should be in a valid world.
     *
     * @return a {@code Set<Chunk>} containing all chunks within the search radius that are
     *         both loaded and generated. Returns an empty set if no chunks meet the criteria
     *         or if the search radius is 0 or negative.
     *
     * @apiNote This method addresses issue #26 by ensuring only safe, loaded chunks are processed
     */
    public Set<Chunk> getLoadedChunks(Block startingBlock) {
        Set<Chunk> loadedChunks = new HashSet<>(25);
        World world = startingBlock.getWorld();

        // Compute chunk bounds
        int minChunkX = (startingBlock.getX() - searchRadius) >> 4;
        int maxChunkX = (startingBlock.getX() + searchRadius) >> 4;
        int minChunkZ = (startingBlock.getZ() - searchRadius) >> 4;
        int maxChunkZ = (startingBlock.getZ() + searchRadius) >> 4;

        // Precompute generated and loaded chunks
        // Fixes #26
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (world.isChunkLoaded(chunkX, chunkZ) && world.isChunkGenerated(chunkX, chunkZ)) {
                    loadedChunks.add(world.getChunkAt(chunkX, chunkZ));
                }
            }
        }
        return loadedChunks;
    }


}