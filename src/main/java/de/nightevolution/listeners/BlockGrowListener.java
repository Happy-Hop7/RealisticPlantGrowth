package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Material;
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

        if(!super.initEventData(e)) {
            // log info
            return;
        }
        logger.verbose("BlockGrowEvent:");


        double deathChance = surrounding.getDeathChance();
        double growthRate = surrounding.getGrowthRate();

        logger.verbose("deathChance: " + deathChance);
        logger.verbose("growthRate: " + growthRate);
        logger.verbose("Biome: " + e.getBlock().getBiome());

        if((Math.random() * 100) < deathChance){
            e.getBlock().setType(Material.DEAD_BUSH); // TODO: kill plant
            logger.verbose("Event: Kill plant.");
        }

        if((Math.random() * 100) < (100-growthRate)){
            e.setCancelled(true);
            logger.verbose("Event cancelled.");
            return;
        }

        logger.verbose("Normal event");

    }

}
