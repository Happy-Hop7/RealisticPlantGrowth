package de.nightevolution.realisticplantgrowth.utils.enums;

import java.util.ArrayList;
import java.util.List;

public enum ModifierType {
    NATURAL("natural"),
    UV_LIGHT("uv_light"),
    GROWTH_RATE("growth_rate"),
    DEATH_CHANCE("death_chance"),
    FERTILIZER_BOOST("fertilizer_boost");
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