package de.nightevolution.realisticplantgrowth.plant.data;

import java.util.List;

// Main Plant Class
public class PlantGrowthModifier {
    private final String plantConfigKey;
    private final GrowthSettings growthSettings;
    private final HarvestingRequirements harvestingRequirements;
    private final List<BiomeGroupData> biomeGroupData;

    public PlantGrowthModifier(String plantConfigKey, GrowthSettings growthSettings, HarvestingRequirements harvestingRequirements, List<BiomeGroupData> biomeGroupData) {
        this.plantConfigKey = plantConfigKey;
        this.growthSettings = growthSettings;
        this.harvestingRequirements = harvestingRequirements;
        this.biomeGroupData = biomeGroupData;
    }


    // Getters and Setters
    public GrowthSettings getGrowthSettings() {
        return growthSettings;
    }

    public HarvestingRequirements getHarvestingRequirements() {
        return harvestingRequirements;
    }

    public List<BiomeGroupData> getBiomeGroups() {
        return biomeGroupData;
    }


    @Override
    public String toString() {
        return "PlantGrowthModifier{" +
                "plantConfigKey=" + plantConfigKey +
                "growthSettings=" + growthSettings +
                ", harvestingRequirements=" + harvestingRequirements +
                ", biomeGroupData=" + biomeGroupData +
                '}';
    }


}
