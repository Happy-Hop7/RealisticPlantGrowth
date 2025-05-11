package de.nightevolution.realisticplantgrowth.utils.config;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BiomeGroupsValidator extends Validator{


    /**
     * Constructs a new Validator with the necessary configuration files and data.
     *
     * @param plugin
     * @param fileToValidate The configuration file to validate
     */
    public BiomeGroupsValidator(Plugin plugin, YamlDocument fileToValidate) {
        super(plugin, fileToValidate);
    }

    /**
     * Gets a Set of Biomes from the BiomeGroups file based on the provided route.
     *
     * @param biomeGroup The name of the desired biome group section.
     * @return a HashSet containing Biomes from the specified route. (empty if route not valid)
     */
    @NotNull
    public List<String> getBiomeSetOfBiomeGroup(@NotNull String biomeGroup) {
        Optional<List<String>> biomeList = biomeGroupsFile.getOptionalStringList(biomeGroup);

        if (plugin.isPaperFork()) {
            if (biomeList.isPresent() && !biomeList.get().isEmpty()) {
                List<String> returnList = new ArrayList<>();
                for (String biomeString : biomeList.get()) {
                    if (biomeString.contains(":")) {
                        returnList.add(biomeString);
                    } else {
                        try {
                            Biome biome = Biome.valueOf(biomeString);
                            returnList.add((biome.getKey().asString()));
                            LogUtils.verbose(logger, "Checked BiomeString: " + biome);
                            LogUtils.verbose(logger, "Added to return list: " + biome.getKey().asString());
                        } catch (IllegalArgumentException e) {
                            logger.warn("Biome '" + biomeString + "' is not a valid Bukkit Biome name!");
                            logger.warn("Please check your BiomeGroups.yml!");
                            logger.warn("Include the Namespace of custom Biomes!");
                        }
                    }
                }

                return returnList;
            } else
                return new ArrayList<>();
        } else {
            if (biomeList.isPresent() && !biomeList.get().isEmpty()) {
                return getCheckedBiomeSet(biomeList.get());
            } else
                return new ArrayList<>();
        }
    }

    /**
     * Checks if the provided biome group name is valid by verifying if it exists in the {@code BiomeGroups.yml} File.
     *
     * <p>This method looks up the given biome group name in the internal collection of registered biome groups
     * and returns {@code true} if the name exists, indicating that it's a valid biome group name.</p>
     *
     * @param biomeGroupNameToCheck the name of the biome group to be checked for validity
     * @return {@code true} if the biome group name exists in the internal data, otherwise {@code false}
     */
    private boolean isValidBiomeGroupName(String biomeGroupNameToCheck) {
        Set<Object> biomeGroupNames = Collections.emptyMap().keySet();
        return biomeGroupNames.contains(biomeGroupNameToCheck);
    }


}
