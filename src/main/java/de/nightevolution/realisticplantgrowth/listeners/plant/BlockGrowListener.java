package de.nightevolution.realisticplantgrowth.listeners.plant;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.listeners.PlantGrowthListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles natural block growth events in the world.
 * <p>
 * This listener is triggered when blocks grow naturally, such as:
 * <ul>
 *     <li>Wheat</li>
 *     <li>Sugar Cane</li>
 *     <li>Cactus</li>
 *     <li>Watermelon</li>
 *     <li>Pumpkin</li>
 * </ul>
 * This class is used to manipulate the growth rates of plants.
 * </p>
 */
public class BlockGrowListener extends PlantGrowthListener {

    /**
     * Constructs a new BlockGrowListener instance.
     *
     * @param instance The RealisticPlantGrowth instance to associate with the listener.
     */
    public BlockGrowListener(RealisticPlantGrowth instance) {
        super(instance);
    }

    /**
     * Handles the BlockGrowEvent to process plant growth modifications.
     * <p>
     * This method initializes event data, processes the growth event, and applies any configured
     * modifications to growth rates or cancellations.
     * </p>
     *
     * @param e The BlockGrowEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent e) {
        if (logEvent) {
            superLogger.logToFile("", logFile);
            superLogger.logToFile("-------------------- Block Grow Event --------------------", logFile);
        }

        // Initialize event data and check if the world is enabled for plant growth modifications.
        if (!initEventData(e)) {
            return;
        }

        if (logEvent) {
            superLogger.verbose("  Initialized BlockGrowEvent");
        }

        // Handle cases where the event block is AIR by finding the source block.
        if (eventBlockType == Material.AIR) {
            if (logEvent) {
                superLogger.log("  AIR Block Grow Event.");
            }

            eventBlock = getSourceFromAirBlock();
            eventBlock = getRootBlockOf(eventBlock);
            eventBlockType = eventBlock.getType();
        }

        if (logEvent) {
            logEventData();
        }

        // Process the event and determine if it should be cancelled.
        if (!processEvent()) {
            return;
        }

        if (shouldEventBeCancelled()) {
            e.setCancelled(true);
            return;
        }

        // Handle cases where the growth rate is above 100%.
        if (growthRate > 100) {
            if (logEvent) {
                superLogger.logToFile("  Growth rate above 100% not implemented yet!", logFile);
            }
        }

        // Check if fertilizer was used and adjust the composter fill level if necessary.
        checkFertilizerUsage();

        if (logEvent) {
            superLogger.logToFile("  -> Event handled normally.", logFile);
        }
    }

    /**
     * Initializes event-related data for plant growth modification.
     * <p>
     * Retrieves and sets information such as block type, location, world, and biome.
     * Checks if plant growth modifications are enabled for the current world.
     * </p>
     *
     * @param e The BlockEvent containing information about the block growth.
     * @return {@code true} if the event data was initialized successfully and the world is enabled;
     *         {@code false} otherwise.
     */
    private boolean initEventData(@NotNull BlockEvent e) {
        eventBlock = e.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();
        eventLocation = eventBlock.getLocation();

        return !instance.isWorldDisabled(eventWorld);
    }

    /**
     * Retrieves the source {@link Block} from the surrounding blocks when the event block is AIR.
     * <p>
     * Checks neighboring blocks in all directions to find a source block that corresponds to a plant,
     * which can return AIR as part of a growth event.
     * </p>
     *
     * @return The source {@link Block} found in the vicinity of the AIR block.
     * @throws IllegalStateException If no source block is found.
     */
    @NotNull
    private Block getSourceFromAirBlock() {
        for (BlockFace blockFace : blockFaceArray) {
            Block relativeEventBlock = eventBlock.getRelative(blockFace);
            if (versionMapper.isGrowEventReturnsAirBlockPlant(relativeEventBlock.getType())) {
                if (logEvent) {
                    superLogger.logToFile("  getSourceFromAirBlock(): Found source block: " + relativeEventBlock, logFile);
                }
                return relativeEventBlock;
            }
        }
        throw new IllegalStateException("BlockGrowListener.getSourceFromAirBlock(): " +
                "Could not find source block from eventBlock: " + eventBlock);
    }
}
