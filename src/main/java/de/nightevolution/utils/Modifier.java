package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;

/**
 * The Modifier class represents modifiers for plant growth in the RealisticPlantGrowth plugin.
 * It includes properties such as growthModifier, deathChance, and the information if a fertilizer was used.
 */
public class Modifier {

    private final RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final ConfigManager cm = instance.getConfigManager();
    private double growthModifier;
    private double deathChance;
    private boolean fertilizerUsed;

    /**
     * Constructs a Modifier object with specified growthModifier, deathChance, and fertilizer usage.
     *
     * @param growthModifier The growth modifier value.
     * @param deathChance The death chance value.
     * @param fertilizerUsed Indicates whether fertilizer is used.
     */
    public Modifier(double growthModifier, double deathChance, boolean fertilizerUsed){
        this.growthModifier = getCheckedGrowthModifier(growthModifier);
        this.deathChance = getCheckedDeathChance(deathChance);
        this.fertilizerUsed = fertilizerUsed;
    }

    /**
     * Gets the current growth modifier.
     *
     * @return The growth modifier value.
     */
    public double getGrowthModifier(){
        return growthModifier;
    }

    /**
     * Gets the current death chance.
     *
     * @return The death chance value.
     */
    public double getDeathChance(){
        return deathChance;
    }

    /**
     * Checks if fertilizer was used to calculate the modifiers.
     *
     * @return True if fertilizer was used, false otherwise.
     */
    public boolean isFertilizerUsed(){
        return fertilizerUsed;
    }

    /**
     * Sets the growth modifier with the specified value.
     *
     * @param growthModifier The new growth modifier value.
     */
    public void setGrowthModifier(double growthModifier){
        this.growthModifier = getCheckedGrowthModifier(growthModifier);
    }

    /**
     * Sets the death chance with the specified value.
     *
     * @param deathChance The new death chance value.
     */
    public void setDeathChance(double deathChance){
        this.deathChance = getCheckedDeathChance(deathChance);
    }

    /**
     * Sets whether fertilizer was used or not.
     *
     * @param fertilizerUsed True if fertilizer was used, false otherwise.
     */
    public void setFertilizerUsed(boolean fertilizerUsed){
        this.fertilizerUsed = fertilizerUsed;
    }

    /**
     * Ensures that the provided growth modifier is within valid bounds.
     *
     * @param growthModifier The growth modifier to be checked.
     * @return The corrected growth modifier value.
     */
    private double getCheckedGrowthModifier(double growthModifier){
        if(growthModifier <= 0.0)
            return 0.0;
        if(growthModifier > 100.0 && !cm.isFertilizer_allow_growth_rate_above_100()){
            return 100.0;
        }
        return growthModifier;
    }

    /**
     * Ensures that the provided death chance is within valid bounds.
     *
     * @param deathChance The death chance to be checked.
     * @return The corrected death chance value.
     */
    private double getCheckedDeathChance(double deathChance){
        if(deathChance < 0.0)
            return 0.0;

        return Math.min(deathChance, 100.0);

    }

}
