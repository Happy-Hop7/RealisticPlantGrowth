package de.nightevolution.listeners.plant;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.listeners.PlantGrowthListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;

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

        initEventData(e);
        if (instance.isWorldDisabled(eventWorld)) return;

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


    private void initEventData(@NotNull StructureGrowEvent e) {
        // Get coordinates and information of the event block for logging
        eventLocation = e.getLocation();
        eventBlock = eventLocation.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();
    }

}
