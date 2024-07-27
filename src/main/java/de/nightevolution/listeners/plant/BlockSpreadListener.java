package de.nightevolution.listeners.plant;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.listeners.PlantGrowthListener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.List;

/**
 * Listens for plant growth events caused by block spreading.
 * <p>
 * This listener is used by various plant types that spread, such as:
 * <ul>
 *     <li>Chorus Plants</li>
 *     <li>Bamboo</li>
 *     <li>Red and Brown Mushrooms</li>
 * </ul>
 * </p>
 */
public class BlockSpreadListener extends PlantGrowthListener {

    // TODO: Implement method to find the source block for chorus plants.
    private List<Block> plantStem;

    /**
     * Constructs a new BlockSpreadListener instance.
     * <p>
     * Initializes the BlockSpreadListener with the provided RealisticPlantGrowth instance.
     * </p>
     *
     * @param instance The RealisticPlantGrowth instance to associate with the listener.
     */
    public BlockSpreadListener(RealisticPlantGrowth instance) {
        super(instance);
    }

    /**
     * Handles the BlockSpreadEvent to manage plant growth.
     * <p>
     * Processes the event based on the block type and environmental conditions.
     * </p>
     *
     * @param e The BlockSpreadEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(BlockSpreadEvent e) {
        if (logEvent) {
            superLogger.logToFile("", logFile);
            superLogger.logToFile("-------------------- Block Spread Event --------------------", logFile);
        }

        // Initialize event data from the BlockSpreadEvent
        if (!initEventData(e)) {
            if (logEvent) {
                superLogger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
            return;
        }

        if (logEvent) {
            logEventData();
            superLogger.logToFile("  Initialized BlockSpreadEvent.", logFile);
        }

        // Process the event and determine if it should proceed
        if (!processEvent()) {
            return;
        }

        // Check if the event should be cancelled
        if (shouldEventBeCancelled()) {
            e.setCancelled(true);
            return;
        }

        // Check for fertilizer usage
        checkFertilizerUsage();

        if (logEvent) {
            superLogger.logToFile("  -> Event handled normally.", logFile);
        }
    }

    /**
     * Initializes event data from the BlockSpreadEvent.
     * <p>
     * Sets the relevant fields such as event block, location, world, biome, and block type.
     * </p>
     *
     * @param e The BlockSpreadEvent to initialize data from.
     * @return {@code true} if the world is not disabled for plant growth, {@code false} otherwise.
     */
    private boolean initEventData(BlockSpreadEvent e) {
        // Get the source block of the spread event
        eventBlock = getRootBlockOf(e.getSource());
        eventLocation = eventBlock.getLocation();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();

        // Return whether the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }
}
