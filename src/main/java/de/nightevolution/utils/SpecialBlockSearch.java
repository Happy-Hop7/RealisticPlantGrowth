package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SpecialBlockSearch {

    SpecialBlockSearch specialBlockSearch;
    ConfigManager configManager;

    Logger logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.getInstance(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

    private int radiusUV;
    private int radiusFertilizer;

    private SpecialBlockSearch() {
        specialBlockSearch = this;
    }

    public SpecialBlockSearch get() {
        if (specialBlockSearch == null)
            new SpecialBlockSearch();

        configManager = ConfigManager.get();
        radiusUV = configManager.getUv_radius();
        radiusFertilizer = configManager.getFertilizer_radius();

        return specialBlockSearch;
    }

    /**
     * Searches for UV- and Fertilizer-blocks in a given radius around a given {@link Block}.
     * The Radius is set in Config.yml and updated when getting a new instance with get().
     *
     * @param startingBlock {@link Block} in the center of the search radius.
     * @return The {@link Surrounding} of a {@link Block} containing all UV- and Fertilizer-blocks in the radius,
     * defined in the config.
     * Null if, no search was required.
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
     * This Method is required for differatiating the two possible diffrent radi.
     * We dont't wont to iterate 2 times with this o(n^3) search so we have to check, that
     * the smaller radius isn't überschritten
     * @param startingLocation Starting {@link Location}
     * @param relativeLocation Location
     * @param radius UV-Radius or fertilizerRadius to check.
     * @return true, if the radius is within the radius set in the config.
     *         false, otherwise.
     */
    private boolean isBlockWithinRadius(Location startingLocation, Location relativeLocation, int radius){
        int radiusSquared = radius * radius;
        return startingLocation.distanceSquared(relativeLocation) <= radiusSquared;
    }

}