package de.nightevolution.realisticplantgrowth.utils.config;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.exception.ConfigurationException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Validator is responsible for validating the configuration files of the RealisticPlantGrowth plugin.
 * It ensures that the configuration files are correctly structured and all required fields are present.
 */
public abstract class Validator {

    protected static org.apache.logging.log4j.Logger logger;
    protected final YamlDocument fileToValidate;
    protected final String fileName;
    protected final Plugin plugin;


    /**
     * Constructs a new Validator with the necessary configuration files and data.
     *
     * @param fileToValidate The configuration file to validate
     */
    public Validator(Plugin plugin, YamlDocument fileToValidate) {
        this.plugin = plugin;
        this.fileToValidate = fileToValidate;

        fileName = (fileToValidate.getName() == null) ? "" : fileToValidate.getName().toString();

        logger = LogUtils.getLogger(this.getClass());
        LogUtils.debug(logger, "Created new {}.", this.getClass().getSimpleName());
    }


    /**
     * <p>Handles configuration errors by logging the issue and disabling the plugin.</p>
     *
     * <p>This method is invoked whenever a required modifier is missing or the configuration is incomplete.
     * It logs the missing details, provides a link to the documentation for troubleshooting,
     * and disables the plugin to prevent runtime errors.</p>
     *
     * @param missingModifierRoute <br>The {@link Route} indicating the location of the missing modifier in the configuration file.
     */
    protected void handleConfigurationError(Route missingModifierRoute) {
        logger.error("Configuration error detected in {}!", fileName);
        logger.error("Missing modifier at: {}", missingModifierRoute);
        logger.error("Please refer to the Wiki for correct configuration or join our Discord for support.");
        logger.error("Wiki: https://realistic-plant-growth.nightevolution.de/guides/configuration/{}", fileName.toLowerCase());

        // Disable the plugin due to an incomplete configuration
        throw new ConfigurationException("Configuration error detected in " + fileName + "!");
    }

    private void logInvalidKey(String rawKey, String reason) {
        LogUtils.error(logger,"{}: '{}'.", reason, rawKey);
        logger.warn("{} will be ignored!", rawKey);
        logger.warn("Check your biome entries in {}!", fileName);
    }



    /**
     * Validates a list of material names and returns a set of valid Materials.
     *
     * @param stringMaterialList List of material names to validate
     * @return A HashSet of valid Bukkit Materials
     */
    protected Set<Material> validateMaterials(List<String> stringMaterialList) {
        if (stringMaterialList == null || stringMaterialList.isEmpty()) {
            return Collections.emptySet();
        }
        HashSet<Material> materialSet = new HashSet<>(stringMaterialList.size());
        for (String materialName : stringMaterialList) {
            Material material = Material.getMaterial(materialName);
            if (material != null) {
                materialSet.add(material);
            } else {
                //TODO: Add custom Plants integration here
                logger.warn("Material: '{}' is not a recognized Bukkit Material!", materialName);
                logger.warn("Please check your material names in {}!", fileName);
            }
        }
        return materialSet;
    }

    /**
     * Validates a list of biome names (as strings) and returns a set of valid {@link NamespacedKey}s
     * that exist in the biome registry.
     *
     * @param stringBiomeList List of biome names (e.g., "minecraft:plains")
     * @return A set of valid {@link NamespacedKey}s registered in the biome registry.
     */
    protected Set<NamespacedKey> validateBiomes(List<String> stringBiomeList) {
        if (stringBiomeList == null || stringBiomeList.isEmpty()) {
            return Collections.emptySet();
        }

        Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Set<NamespacedKey> validKeys = new HashSet<>(stringBiomeList.size());

        for (String entry : stringBiomeList) {
            NamespacedKey key = NamespacedKey.fromString(entry);

            if (key == null) {
                logInvalidKey(entry, "Invalid namespaced key format");
                continue;
            }

            if (biomeRegistry.get(key) != null) {
                validKeys.add(key);
            } else {
                logInvalidKey(entry, "Biome not registered on this server");
            }
        }

        return validKeys;
    }


}