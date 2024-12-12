package de.nightevolution.realisticplantgrowth.plant.data;

import de.nightevolution.realisticplantgrowth.utils.enums.ModifierType;

public class GrowthRates {
    private final ModifierType modifierType;
    private final int growthRate;
    private final int deathChance;
    private final int fertilizerBoost;

    public GrowthRates(ModifierType modifierType, int growthRate, int deathChance, int fertilizerBoost) {
        this.modifierType = modifierType;
        this.growthRate = growthRate;
        this.deathChance = deathChance;
        this.fertilizerBoost = fertilizerBoost;
    }

    // Getters and setters
    public ModifierType getModifierType() {
        return modifierType;
    }

    public int getGrowthRate() {
        return growthRate;
    }

    public int getDeathChance() {
        return deathChance;
    }

    public int getFertilizerBoost() {
        return fertilizerBoost;
    }
}
