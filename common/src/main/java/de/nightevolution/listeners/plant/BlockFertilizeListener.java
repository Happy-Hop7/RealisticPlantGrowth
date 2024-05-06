package de.nightevolution.listeners.plant;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.listeners.PlantGrowthListener;

/**
 * Listens to block changes resulting from a player fertilizing a given block with bonemeal.
 */
public class BlockFertilizeListener extends PlantGrowthListener {
    public BlockFertilizeListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }
}
