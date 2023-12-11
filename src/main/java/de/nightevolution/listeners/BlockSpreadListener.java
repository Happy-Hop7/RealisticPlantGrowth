package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.List;

/**
 * Listens for plant growth from block spread.
 * Used by chorus plant, bamboo, red- and brown-mushrooms.
 */
public class BlockSpreadListener extends PlantGrowthListener{
    private List<Block> plantStem;

    public BlockSpreadListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(BlockSpreadEvent e){
        logger.verbose("-------------------- BEGIN BlockSpreadEvent --------------------");

        if(!initEventData(e)) return;

        logger.verbose("initialized BlockSpreadEvent");

        if(!processEvent())
            return;

        if(shouldEventBeCancelled()){
            e.setCancelled(true);
            return;
        }
        checkFertilizerUsage();
        logger.verbose("-------------------- Normal END BlockSpreadEvent --------------------");
    }

    private boolean initEventData(BlockSpreadEvent e){
        // Get coordinates and information of the event block for logging
        eventBlock = getRootBlockOf(e.getSource());
        eventLocation = eventBlock.getLocation();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();

        // Check if the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }

}
