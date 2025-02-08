package de.nightevolution.realisticplantgrowth.listeners.plant;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.listeners.PlantGrowthListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for organic structures attempting to grow, such as:
 * <ul>
 *     <li>Saplings growing into Trees</li>
 *     <li>Mushrooms growing into Huge Mushrooms</li>
 * </ul>
 * This can occur naturally or through the use of bonemeal.
 */
public class StructureGrowListener extends PlantGrowthListener {

    /**
     * Constructs a new StructureGrowListener instance.
     * <p>
     * Initializes the listener with the provided RealisticPlantGrowth instance and sets up logging.
     * </p>
     *
     * @param instance The RealisticPlantGrowth instance to associate with the listener.
     */
    public StructureGrowListener(RealisticPlantGrowth instance) {
        super(instance);
        // Configure logging based on the debug settings and configuration
        logEvent = RealisticPlantGrowth.isDebug() && instance.getConfigManager().isStructure_log();
        // Set log file name for this listener
        logFile = "StructureGrowEvent";
    }

    /**
     * Handles the StructureGrowEvent to manage organic structure growth.
     * <p>
     * Processes the event based on the block type and environmental conditions, and checks if it should be cancelled.
     * </p>
     *
     * @param e The StructureGrowEvent to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(StructureGrowEvent e) {
        if (logEvent) {
            superLogger.logToFile("", logFile);
            superLogger.logToFile("-------------------- Structure Grow Event --------------------", logFile);
        }

        // Initialize event data from the StructureGrowEvent
        if (!initEventData(e)) {
            if (logEvent) {
                superLogger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
            return;
        }

        // Process the event and determine if it should proceed
        if (!processEvent())
            return;

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
     * Initializes event data from the StructureGrowEvent.
     * <p>
     * Sets the relevant fields such as location, block, world, biome, and block type.
     * </p>
     *
     * @param e The StructureGrowEvent to initialize data from.
     * @return {@code true} if the world is not disabled for plant growth, {@code false} otherwise.
     */
    private boolean initEventData(@NotNull StructureGrowEvent e) {
        // Get the location of the event and related block information
        eventLocation = e.getLocation();
        eventBlock = eventLocation.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();

        // If Chunk is not fully generated ignore the Event (Fixes #26)
        if (!eventBlock.getChunk().isGenerated()) {
            return false;
        }

        // Return whether the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }
}
