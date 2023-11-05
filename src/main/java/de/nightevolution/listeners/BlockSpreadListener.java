package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;

/**
 * Listens for plant growth from block spread (e.g. chorus plant).
 */
public class BlockSpreadListener extends PlantGrowthListener{
    public BlockSpreadListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }
}
