package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.event.Listener;

/**
 * Listens to player block interactions in order to give information
 * about crops growing rates in the current biome.
 */
public class PlayerListener implements Listener {

    private final RealisticPlantGrowth instance;
    private Logger logger;
    public PlayerListener(RealisticPlantGrowth instance){
        this.instance = instance;
        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

}
