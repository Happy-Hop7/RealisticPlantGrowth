package de.nightevolution.realisticplantgrowth.plant.data;

public class GrowthSettings {
    private final int bonemealLimit;
    private final int minLightLevel;
    private final boolean allowPistonHarvesting;
    private final boolean offlineGrow;

    public GrowthSettings(int bonemealLimit, int minLightLevel, boolean allowPistonHarvesting, boolean offlineGrow) {
        this.bonemealLimit = bonemealLimit;
        this.minLightLevel = minLightLevel;
        this.allowPistonHarvesting = allowPistonHarvesting;
        this.offlineGrow = offlineGrow;
    }

    // Getters
    public int getBonemealLimit() {
        return bonemealLimit;
    }

    public int getMinLightLevel() {
        return minLightLevel;
    }

    public boolean isAllowPistonHarvesting() {
        return allowPistonHarvesting;
    }

    public boolean isOfflineGrow() {
        return offlineGrow;
    }
}
