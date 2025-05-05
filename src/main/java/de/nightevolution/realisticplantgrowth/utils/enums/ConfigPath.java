package de.nightevolution.realisticplantgrowth.utils.enums;

public enum ConfigPath {
    // General Settings
    GENERAL_SECTION("general"),
    GENERAL_LANGUAGE_CODE("general.language_code"),
    GENERAL_ENABLED_WORLDS("general.enabled_worlds"),
    GENERAL_ENABLED_WORLDS_AS_BLACKLIST("general.enabled_worlds_as_blacklist"),
    GENERAL_SHOW_GROWTH_RATES_ON_CLICK("general.show_growth_rates_on_click"),
    GENERAL_GROWTH_RATE_DISPLAY_COOLDOWN("general.growth_rate_display_cooldown"),

    // Farmland Settings
    FARMLAND_SETTINGS_SECTION("farmland_settings"),
    FARMLAND_DESTROY_ON_HARVEST("farmland_settings.destroy_on_harvest"),
    FARMLAND_REPLACEMENT_BLOCKS("farmland_settings.replacement_blocks"),

    // Composter Settings
    COMPOSTER_SECTION("composter"),
    COMPOSTER_DISABLE_BONEMEAL_OUTPUT("composter.disable_bonemeal_output"),
    COMPOSTER_QUICK_FILL_WITH_SHIFT("composter.quick_fill_with_shift"),
    COMPOSTER_ALLOW_BONEMEAL_AS_INPUT("composter.allow_bonemeal_as_input"),

    // Fertilizer Settings
    FERTILIZER_SECTION("fertilizer"),
    FERTILIZER_ENABLED("fertilizer.enabled"),
    FERTILIZER_BEHAVIOR_RADIUS("fertilizer.behavior.radius"),
    FERTILIZER_BEHAVIOR_PASSIVE("fertilizer.behavior.passive"),
    FERTILIZER_BEHAVIOR_ALLOW_ABOVE_100("fertilizer.behavior.allow_growth_rate_above_100"),
    FERTILIZER_INVALID_BIOME_ENABLED("fertilizer.invalid_biome.enabled"),
    FERTILIZER_INVALID_BIOME_GROWTH_RATE("fertilizer.invalid_biome.growth_rate"),
    FERTILIZER_INVALID_BIOME_DEATH_CHANCE("fertilizer.invalid_biome.death_chance"),

    // UV Light Settings
    UV_LIGHT_SECTION("uv_light"),
    UV_LIGHT_ENABLED("uv_light.enabled"),
    UV_LIGHT_RADIUS("uv_light.radius"),
    UV_LIGHT_BLOCKS("uv_light.blocks"),
    UV_LIGHT_REQUIRE_ALL_BLOCKS("uv_light.require_all_blocks"),
    UV_LIGHT_GROW_IN_DARK("uv_light.grow_in_dark"),

    // Effects
    EFFECTS_SECTION("effects"),
    EFFECTS_PLANT_DEATH_SOUND_EFFECT_ENABLED("effects.plant_death_sound_effect.enabled"),
    EFFECTS_PLANT_DEATH_SOUND("effects.plant_death_sound_effect.sound"),
    EFFECTS_PLANT_DEATH_VOLUME("effects.plant_death_sound_effect.volume"),
    EFFECTS_PLANT_DEATH_PITCH("effects.plant_death_sound_effect.pitch"),
    EFFECTS_PLANT_DEATH_EFFECT("effects.plant_death_sound_effect.effect"),
    EFFECTS_PLANT_DEATH_DATA("effects.plant_death_sound_effect.data"),

    // Logging
    LOGGING_SECTION("logging"),
    LOGGING_DEBUG_LOG("logging.debug_log"),
    LOGGING_PLANT_LOG("logging.plant_log"),
    LOGGING_STRUCTURE_LOG("logging.structure_log"),
    LOGGING_PLAYER_LOG("logging.player_log"),
    LOGGING_BONEMEAL_LOG("logging.bonemeal_log"),

    // Plugin Updates
    PLUGIN_UPDATES_SECTION("plugin_updates"),
    PLUGIN_UPDATES_CHECK_FOR_UPDATES("plugin_updates.check_for_updates"),
    PLUGIN_UPDATES_INTERVAL_HOURS("plugin_updates.check_interval_hours"),

    // Metrics
    USE_METRICS("use_metrics"),

    // Internal
    CONFIG_VERSION("config-version"),
    VERBOSE("verbose");

    private final String path;

    ConfigPath(String path) {
        this.path = path;
    }

    /**
     * Gets the key within a section.
     * @return the key within the section, or the full path if this is not a section path
     */
    public String getKey() {
        if (this.path.contains(".")) {
            return this.path.substring(this.path.indexOf('.') + 1);
        }
        return this.path;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return this.path;
    }
}
