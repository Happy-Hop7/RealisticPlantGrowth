package de.nightevolution.listeners;

import de.nightevolution.RealisticPlantGrowth;

/**
 * Listens to organic structure attempts to grow (Sapling -> Tree), (Mushroom -> Huge Mushroom), naturally or using bonemeal.
 */
public class StructureGrowListener extends PlantGrowthListener{
    public StructureGrowListener(RealisticPlantGrowth instance) {
        super(instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


}
