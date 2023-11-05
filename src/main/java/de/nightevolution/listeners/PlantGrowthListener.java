package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.event.Listener;


public abstract class PlantGrowthListener  implements Listener{
    protected RealisticPlantGrowth instance;
    protected Logger logger;
    public PlantGrowthListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Abstract PlantGrowthListener Constructor.");

        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    protected void checkForSpecialBlocks(){

    }


}
