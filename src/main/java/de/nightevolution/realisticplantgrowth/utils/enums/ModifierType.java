package de.nightevolution.realisticplantgrowth.utils.enums;

import java.util.ArrayList;
import java.util.List;

public enum ModifierType {
    NATURAL_GROWTH_RATE("GrowthRate"),
    NATURAL_DEATH_CHANCE("NaturalDeathChance"),
    UV_LIGHT_GROWTH_RATE("UVLightGrowthRate"),
    UV_LIGHT_DEATH_CHANCE("UVLightDeathChance");
    //FERTILIZER_INVALID_BIOME("FERTILIZER_INVALID_BIOME");

    private final String value;

    ModifierType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<ModifierType> getModifierTypeList() {
        return new ArrayList<>(List.of(ModifierType.values()));
    }
}