package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;

/**
 * Listens to calls when a block grows naturally in the world.
 * Used to manipulate growth rates of plants.
 */
public class BlockGrowListener extends PlantGrowthListener{

    public BlockGrowListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }



}
