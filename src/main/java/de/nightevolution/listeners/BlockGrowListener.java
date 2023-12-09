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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent e){

        if(!initEventData(e)) return;

        logger.verbose("BlockGrowEvent:");

        if (eventBlockType == Material.AIR) {
            logger.verbose("AIR Block Grow Event!");
            eventBlock = getAttachedStem();

            if(eventBlock == null) {
                e.setCancelled(true);
                return;
            }
            eventBlockType = eventBlock.getType();
        }

        if(!processEvent())
            return;

        if(isDeathChanceTooHigh()) {
            e.setCancelled(true);
            return;
        }

        if(cancelDueToGrowthRate()){
            e.setCancelled(true);
            logger.verbose("Event cancelled due to growth rate.");
            return;
        }

        if(cancelDueToDeathChance()){
            e.setCancelled(true);
            logger.verbose("Event cancelled due to death chance.");
            killPlant();
            return;
        }

        if(growthRate > 100){
            // double growth
            logger.verbose("Double Growth!");
        }


        checkFertilizerUsage();
        logger.verbose("Normal event");

    }

}
