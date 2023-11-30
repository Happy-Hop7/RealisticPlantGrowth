package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
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

        if(deathChance == 100.0 || growthRate == 0.0){
            //kill plant
            logger.verbose("Event: Kill plant.");
            e.getBlock().setType(Material.DEAD_BUSH); // TODO: kill plant
            e.setCancelled(true);
            return;
        }

        int maxAge;

        if(e.getBlock().getBlockData() instanceof Ageable crop) {
            maxAge = crop.getMaximumAge();
        }else {
            maxAge = 1;
        }

        double partialDeathChance = deathChance / maxAge;


        logger.verbose("deathChance: " + deathChance);
        logger.verbose("partialDeathChance: " + partialDeathChance);
        logger.verbose("GrowthRate: " + growthRate);
        logger.verbose("maxAge: " + maxAge);
        logger.verbose("Biome: " + e.getBlock().getBiome());

        if((Math.random() * 100) > growthRate){
            e.setCancelled(true);
            logger.verbose("Event cancelled.");
            return;
        }

        if((Math.random() * 100) < partialDeathChance){
            e.getBlock().setType(Material.DEAD_BUSH); // TODO: kill plant
            logger.verbose("Event: Kill plant.");
            e.setCancelled(true);
            return;
        }

        if(growthRate > 100){
            // double growth
            logger.verbose("Double Growth!");
        }


        logger.verbose("Normal event");

    }

}
