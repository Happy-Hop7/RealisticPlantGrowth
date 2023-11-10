package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlantGrow(BlockGrowEvent e){
        Block eventBlock = e.getBlock();
        World world = eventBlock.getWorld();

        if(configManager.getEnabled_worlds().contains(world.getName()))
            return;

    }

}
