package de.nightevolution.realisticplantgrowth.utils.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;

public class ConfigValidator extends Validator{


    /**
     * Constructs a new Validator with the necessary configuration files and data.
     *
     * @param plugin
     * @param fileToValidate The configuration file to validate
     */
    public ConfigValidator(Plugin plugin, YamlDocument fileToValidate) {
        super(plugin, fileToValidate);
    }

    /**
     * Checks the validity of the sound and effect specified in the plant_death_sound_effect section.
     * If not valid, reverts to default values.
     *
     * @param plant_death_sound_effect The plant death sound effect section from the config
     * @return The validated section, reverting to defaults if invalid
     */
    public Section checkEffectsSection(Section plant_death_sound_effect) {
        boolean soundEffectEnabled = plant_death_sound_effect.getBoolean("enabled");
        boolean soundValid = false;
        boolean effectValid = false;

        if (!soundEffectEnabled)
            return plant_death_sound_effect;

        // Checking, if String is a Bukkit sound/effect
        String sound = plant_death_sound_effect.getString("sound");
        String effect = plant_death_sound_effect.getString("effect");

        try {
            Sound.valueOf(sound);
            soundValid = true;
        } catch (IllegalArgumentException e) {
            logger.warn(sound + " is not a valid Bukkit sound!");
        }

        try {
            Effect.valueOf(effect);
            effectValid = true;
        } catch (IllegalArgumentException e) {
            logger.warn(effect + " is not a valid Bukkit effect!");
        }

        if (!(soundValid && effectValid)) {
            logger.warn("Using default values instead.");
            return plant_death_sound_effect.getDefaults();
        }

        return plant_death_sound_effect;
    }


}
