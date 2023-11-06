package de.nightevolution.utils;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.*;

/**
 * This class represents the surrounding of a Block, which triggered an PlantGrowthEvent
 */
public class Surrounding {
    private final Block centerBlock;
    private Block closestComposter;
    private final List<Block> uvSources;
    private final List<Block> fertilizerSources;
    private final Logger logger;

    public Surrounding(Block centerBlock, List<Block> uvBlocks, List<Block> fertilizerBlocks) {
        this.centerBlock = centerBlock;
        uvSources = uvBlocks;
        fertilizerSources = fertilizerBlocks;
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.getInstance(),
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

    }

    public Block getCenterBlock() {
        return centerBlock;
    }

    public List<Block> getUvSources() {
        if(uvSources.isEmpty())
            return null;

        return uvSources;
    }

    public List<Block> getFertilizerSources() {
        if(fertilizerSources.isEmpty())
            return null;

        return fertilizerSources;
    }

    /**
     *
     * @return
     */
    public Block getClosestComposter() {

        if(!(closestComposter == null))
            return closestComposter;

        if(fertilizerSources.isEmpty())
            return null;

        Location centerBlockLocation = centerBlock.getLocation();
        List<Block> sortedBlockList = new ArrayList<>();

        Comparator<Block> blockComparator = new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
                int comparison = Double.compare(b1.getLocation().distanceSquared(centerBlockLocation),
                                                b2.getLocation().distanceSquared(centerBlockLocation));

                // If 2 Blocks have the same distance to the centerBlock, a random one is chosen to prioritize.
                if(comparison == 0) {
                    logger.verbose("Fertilizer at same distance -> random decision.");
                    return Math.random() < 0.5 ? -1 : 1;
                }

                return comparison;
            }
        };

        // Sorting the List
        fertilizerSources.sort(blockComparator);

        for (Block block : fertilizerSources){
            logger.verbose("Sorted Distance List:" + block.getLocation());
        }

        closestComposter = fertilizerSources.get(0);

        return closestComposter;
    }


// TODO: implement toString() method for logging


}
