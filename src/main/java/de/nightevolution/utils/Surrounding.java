package de.nightevolution.utils;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class representing the environment around a block that triggered a PlantGrowthEvent.
 * It holds information about the central block of the event, sources of ultraviolet (UV) light, and sources of fertilizer.
 * A logger is also included for outputting verbose and debug information.
 */
public class Surrounding {
    /**
     * The central block associated with the plant growth event.
     */
    private final Block centerBlock;

    /**
     * The closest composter block to the central block.
     */
    private Block closestComposter;

    /**
     * A list of blocks that act as sources of UV light for plant growth.
     */
    private final List<Block> uvSources;

    /**
     * A list of blocks that act as sources of fertilizer for plant growth.
     */
    private final List<Block> fertilizerSources;

    /**
     * Logger for outputting information about the surrounding environment.
     */
    private final Logger logger;


    /**
     * Constructs a Surrounding object with specified center block, UV sources, and fertilizer sources.
     * Initializes a logger for the Surrounding class.
     *
     * @param centerBlock The central block around which the environment is defined.
     * @param uvBlocks A list of blocks that provide UV light.
     * @param fertilizerBlocks A list of blocks that provide fertilizer.
     */
    public Surrounding(Block centerBlock, List<Block> uvBlocks, List<Block> fertilizerBlocks) {
        this.centerBlock = centerBlock;
        uvSources = uvBlocks;
        fertilizerSources = fertilizerBlocks;
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.getInstance(),
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

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
     * Retrieves the list of UV source blocks. Returns null if the list is empty.
     *
     * @return A list of blocks that provide UV light or null if there are none.
     */
    public List<Block> getUvSources() {
        if(uvSources.isEmpty())
            return null;

        return uvSources;
    }

    /**
     * Retrieves the list of fertilizer source blocks. Returns null if the list is empty.
     *
     * @return A list of blocks that provide fertilizer or null if there are none.
     */
    public List<Block> getFertilizerSources() {
        if(fertilizerSources.isEmpty())
            return null;

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
        Location centerBlockLocation = centerBlock.getLocation();

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
     * @return A string that textually represents the current state of the Surrounding object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Surrounding{");
        builder.append("centerBlock=").append(centerBlock);

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


}
