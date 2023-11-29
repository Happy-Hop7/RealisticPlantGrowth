package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;

public class Modifier {

    private final RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final ConfigManager cm = instance.getConfigManager();
    private double growthModifier;
    private double deathChance;
    private boolean fertilizerUsed;

    public Modifier(double growthModifier, double deathChance, boolean fertilizerUsed){
        this.growthModifier = getCheckedGrowthModifier(growthModifier);
        this.deathChance = getCheckedDeathChance(deathChance);
        this.fertilizerUsed = fertilizerUsed;
    }

    public double getGrowthModifier(){
        return growthModifier;
    }

    public double getDeathChance(){
        return deathChance;
    }

    public boolean isFertilizerUsed(){
        return fertilizerUsed;
    }

    public void setGrowthModifier(double growthModifier){
        this.growthModifier = getCheckedGrowthModifier(growthModifier);
    }

    public void setDeathChance(double deathChance){
        this.deathChance = getCheckedDeathChance(deathChance);
    }

    public void setFertilizerUsed(boolean fertilizerUsed){
        this.fertilizerUsed = fertilizerUsed;
    }

    private double getCheckedGrowthModifier(double growthModifier){
        if(growthModifier > 100.0 && !cm.isFertilizer_allow_growth_rate_above_100()){
            return 100.0;
        }
        return growthModifier;
    }

    private double getCheckedDeathChance(double deathChance){
        if(deathChance < 0.0)
            return 0.0;

        return Math.min(deathChance, 100.0);

    }

}
