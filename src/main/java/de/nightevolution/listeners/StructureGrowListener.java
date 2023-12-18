package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Listens to organic structure attempts to grow (Sapling -> Tree), (Mushroom -> Huge Mushroom), naturally or using bonemeal.
 */
public class StructureGrowListener extends PlantGrowthListener {
    public StructureGrowListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(StructureGrowEvent e) {
        logger.verbose("StructureGrowEvent:");

        if (!initEventData(e)) return;

        logger.verbose("initialized StructureGrowEvent");

        if (!processEvent())
            return;

        if (shouldEventBeCancelled()) {
            e.setCancelled(true);
            return;
        }
        checkFertilizerUsage();
        logger.verbose("Normal event");
    }


    private boolean initEventData(StructureGrowEvent e) {
        // Get coordinates and information of the event block for logging
        eventLocation = e.getLocation();
        eventBlock = eventLocation.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();

        // Check if the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }

}
