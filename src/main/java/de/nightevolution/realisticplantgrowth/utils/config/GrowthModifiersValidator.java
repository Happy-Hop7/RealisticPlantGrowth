package de.nightevolution.realisticplantgrowth.utils.config;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.enums.ModifierType;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GrowthModifiersValidator extends Validator{


    /**
     * Constructs a new Validator with the necessary configuration files and data.
     *
     * @param plugin
     * @param fileToValidate The configuration file to validate
     */
    public GrowthModifiersValidator(Plugin plugin, YamlDocument fileToValidate) {
        super(plugin, fileToValidate);
    }

    /**
     * <p>Validates the configuration in {@code GrowthModifiers.yml} for correctness and completeness.</p>
     *
     * <p>This method ensures that all plants have defined growth modifiers for their respective biome groups
     * or appropriate default settings. During the verification process, any detected issues are logged,
     * and the plugin is disabled if the configuration is found to be incomplete.</p>
     *
     * <p><b>Important:</b> Proper configuration is critical to avoid runtime errors and ensure expected plugin functionality.</p>
     */
    public void verifyGrowthModifiersConfiguration() {
        logger.info("Starting verification of GrowthModifiers.yml...");

        // Iterate through all plants defined in the GrowthModifiers.yml file
        for (Object plantSectionString : Collections.emptyMap().keySet()) {
            Section plantSection = growthModifiersFile.getSection(Route.from(plantSectionString));

            // Retrieve all biome groups assigned to the current plant
            List<String> biomeGroupsList = plantSection.getStringList(biomeGroupsListRoute);

            // Validate growth modifiers for each biome group assigned to the plant
            if (!biomeGroupsList.isEmpty()) {
                for (String biomeGroup : biomeGroupsList) {
                    if (isValidBiomeGroupName(biomeGroup))
                        checkBiomeGroupModifiers(plantSection, biomeGroup);
                    else {
                        logger.warn("BiomeGroup '" + biomeGroup + "' is not defined in your BiomeGroups.yml and will be ignored.");
                        logger.warn("Check the configuration at: " + plantSection.getNameAsString() + " -> BiomeGroup -> Groups -> " + biomeGroup);
                        logger.warn("Please verify your BiomeGroups.yml to avoid potential issues");
                    }
                }
            }

            // Check for default modifiers if additional biomes are defined in the Default section
            List<String> biomeStringList = plantSection.getStringList(defaultBiomeListRoute);

            if (!biomeStringList.isEmpty()) {
                checkDefaultModifiers(plantSection);
            }

            LogUtils.verbose(logger, plantSection.getNameAsString() + ": Verification completed.");
        }

        logger.info("GrowthModifiers.yml verification completed successfully.");
    }

    /**
     * <p>Validates the growth modifiers for a specified biome group within a plant's configuration.</p>
     *
     * <p>This method ensures that all required modifiers for the given biome group are declared.
     * If the biome group is missing or incomplete, the method checks the plant's default modifiers
     * for fallback settings. If any required modifier is missing, it logs the error and triggers
     * the necessary error handling routine.</p>
     *
     * @param plantSection <br>The {@link Section} of the plant being validated.
     * @param biomeGroupString <br>The name of the biome group whose growth modifiers are being verified.
     */
    private void checkBiomeGroupModifiers(Section plantSection, String biomeGroupString) {
        Optional<Section> optionalBiomeGroupSection = plantSection.getOptionalSection(Route.from("BiomeGroup", biomeGroupString));

        if (optionalBiomeGroupSection.isPresent()) {
            // Check if all required modifiers are declared in this section
            for (ModifierType modifier : ModifierType.getModifierTypeList()) {
                Optional<Double> optionalDouble = optionalBiomeGroupSection.get().getOptionalDouble(modifier.getValue());

                if (optionalDouble.isEmpty()) {
                    Route modifierRoute = Route.from(plantSection.getRouteAsString(), "BiomeGroup", biomeGroupString, modifier.getValue());
                    handleConfigurationError(modifierRoute);
                }
            }
        } else {
            // If the biome group section is not defined, check default modifiers
            logger.debug("BiomeGroup Section '" + biomeGroupString + "' not found for plant '" + plantSection.getNameAsString() + "'.");
            logger.debug("Checking default modifier section for this plant...");
            checkDefaultModifiers(plantSection);
        }
    }



}
