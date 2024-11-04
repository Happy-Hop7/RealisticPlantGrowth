package de.nightevolution.realisticplantgrowth.utils.enums;

/**
 * Interface containing placeholder constants for use in localized messages.
 * These placeholders can be replaced with dynamic values at runtime.
 */
public interface PlaceholderInterface {

    /**
     * Placeholder for the plant material name.
     */
    String PLANT_PLACEHOLDER = "{PLANT}";

    /**
     * Placeholder for the growth rate of a plant.
     */
    String GROWTH_RATE_PLACEHOLDER = "{GROWTH_RATE}";

    /**
     * Placeholder for the death chance of a plant.
     */
    String DEATH_CHANCE_PLACEHOLDER = "{DEATH_CHANCE}";

    /**
     * Placeholder for the biome name.
     */
    String BIOME_PLACEHOLDER = "{BIOME}";

    /**
     * Placeholder indicating whether a biome is valid for plant growth.
     */
    String IS_VALID_BIOME_PLACEHOLDER = "{IS_VALID_BIOME}";

    /**
     * Placeholder indicating whether fertilizer was used.
     */
    String FERTILIZER_USED_PLACEHOLDER = "{FERTILIZER_USED}";

    /**
     * Placeholder indicating whether UV light was used.
     */
    String UV_LIGHT_USED_PLACEHOLDER = "{UV_LIGHT_USED}";

    /**
     * Placeholder indicating whether a plant can grow in the dark.
     */
    String CAN_GROW_IN_DARK_PLACEHOLDER = "{CAN_GROW_IN_DARK}";

    /**
     * Placeholder indicating whether it is dark.
     */
    String IS_DARK_PLACEHOLDER = "{IS_DARK}";

    /**
     * Placeholder for a list of biome groups.
     */
    String BIOME_GROUP_LIST_PLACEHOLDER = "{BIOME_GROUP_LIST}";

    /**
     * Placeholder for a list of biomes.
     */
    String BIOME_LIST_PLACEHOLDER = "{BIOME_LIST}";

}
