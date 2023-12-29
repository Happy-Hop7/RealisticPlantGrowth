package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for checking if a given plant {@link Material} and {@link Biome} combination is valid
 * based on configured BiomeGroups and Default Biomes.
 */
public class BiomeChecker {
    private final ConfigManager cm;
    private final Logger logger;

    private final Material plantMaterial;
    private final Biome currentBiome;

    private static final Route biomeListRoute = Route.from("BiomeGroup", "Groups");
    private static final Route defaultBiomeListRoute = Route.from("Default", "Biome");
    private final Route currentPlantRoute;

    private String matchingBiomeGroup;
    private Section plantSection;

    private final static Map<Material, Biome> validBiomesCache = new HashMap<>();


    /**
     * Constructs a {@link BiomeChecker} for a specific plant {@link Material} and {@link Biome}.
     *
     * @param plantMaterial The {@link Material} of the plant.
     * @param currentBiome  The current {@link Biome} where the plant is located.
     */
    public BiomeChecker(@NotNull Material plantMaterial, @NotNull Biome currentBiome) {
        RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
        this.cm = instance.getConfigManager();

        this.plantMaterial = plantMaterial;
        this.currentBiome = currentBiome;

        this.currentPlantRoute = Route.from(plantMaterial);

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new Biome Checker.");

        initPlantSection();
    }

    /**
     * Initializes the plant {@link Section} from the {@link ConfigManager}.
     * Throws an exception if the section cannot be obtained.
     */
    private void initPlantSection() {
        Optional<Section> optionalSection = cm.getGrowthModifierSection(currentPlantRoute);
        if (optionalSection.isEmpty()) {
            logger.error("Couldn't read GrowthModifier section for '" + plantMaterial + "'!");
            throw new IllegalArgumentException("Check your GrowthModifiers.yml!");
        }
        plantSection = optionalSection.get();
    }


    /**
     * Checks if the configured BiomeGroups include the current {@link Biome}.
     *
     * @return true if the current {@link Biome} is in any BiomeGroup, false otherwise.
     */
    private boolean checkBiomeGroups() {
        Optional<List<String>> biomeStringList = plantSection.getOptionalStringList(biomeListRoute);
        if (biomeStringList.isPresent() && !biomeStringList.get().isEmpty()) {
            for (String biomeGroup : biomeStringList.get()) {
                if (cm.getBiomeSetOfBiomeGroup(biomeGroup).contains(currentBiome)) {
                    matchingBiomeGroup = biomeGroup;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the configured Default {@link Biome}s include the current {@link Biome}.
     *
     * @return true if the current {@link Biome} is in the list of Default {@link Biome}s, false otherwise.
     */
    private boolean checkDefaultBiomes() {
        Optional<List<String>> optionalBiomeStringList = plantSection.getOptionalStringList(defaultBiomeListRoute);
        if (optionalBiomeStringList.isPresent() && !optionalBiomeStringList.get().isEmpty()) {
            List<String> biomeStringList = optionalBiomeStringList.get();
            if (biomeStringList.size() == 1 && biomeStringList.getFirst().equalsIgnoreCase("ALL"))
                return true;

            return (cm.getCheckedBiomeSet(biomeStringList).contains(currentBiome));

        }
        return false;
    }


    public static void clearCache() {
        validBiomesCache.clear();
    }

    public boolean isValid() {
        // TODO: implement caching
        return (checkBiomeGroups() || checkDefaultBiomes());
    }

    @Nullable
    public String getMatchingBiomeGroup() {
        return matchingBiomeGroup;
    }


}
