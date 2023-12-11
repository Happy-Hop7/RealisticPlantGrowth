package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to calls when a block grows naturally in the world.
 *  Examples:
 *     Wheat,
 *     Sugar Cane,
 *     Cactus,
 *     Watermelon,
 *     Pumpkin,
 * Used to manipulate growth rates of plants.
 */
public class BlockGrowListener extends PlantGrowthListener{
    public BlockGrowListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent e){
        logger.verbose("-------------------- BEGIN BlockGrowEvent: --------------------");

        if(!initEventData(e)) return;

        logger.verbose("initialized BlockGrowEvent");

        if (eventBlockType == Material.AIR) {
            logger.verbose("AIR Block Grow Event!");
            eventBlock = getSoureFromAirBlock();
            eventBlockType = eventBlock.getType();
        }

        if(!processEvent())
            return;

        if(shouldEventBeCancelled()){
            e.setCancelled(true);
            return;
        }

        if(growthRate > 100){
            // double growth
            logger.verbose("Double Growth!");
        }


        checkFertilizerUsage();
        logger.verbose("-------------------- Normal END BlockGrowEvent --------------------");

    }

    /**
     * Initializes event-related data for plant growth modification.
     */
    private boolean initEventData(@NotNull BlockEvent e) {
        // Get coordinates and information of the event block for logging
        eventBlock = e.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();
        eventLocation = eventBlock.getLocation();

        logEventData();
        // Check if the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }


    /**
     * Retrieves the source {@link Block} from the surrounding blocks of the event {@link Block}.
     * This method checks neighboring {@link Block}s in specified directions ({@link BlockFace}es) to
     * find a source {@link Block} that corresponds to a plant where growth events return an air {@link Block}.
     *
     * @return The source {@link Block} found within the surrounding of the air {@link Block}.
     * @throws IllegalStateException If no source block is found.
     */
    private Block getSoureFromAirBlock() {
        for (BlockFace blockFace : blockFaceArray){
            Block relativeEventBlock = eventBlock.getRelative(blockFace);
            if (instance.isGrowEventReturnsAirBlockPlant(relativeEventBlock.getType())){
                return relativeEventBlock;
            }
        }
        throw new IllegalStateException("Could not get SourceBlock from eventBlock: " + eventBlock);
    }

    public void logEventData() {
        logger.verbose("Event data:");
        logger.verbose("  - eventBlockType: " + eventBlockType);
        logger.verbose("  - eventLocation: " + eventLocation);
        logger.verbose("  - eventWorld: " + eventWorld);
        logger.verbose("  - eventBiome: " + eventBiome);
    }
}
