package de.nightevolution.realisticplantgrowth.utils.biome;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.MaterialMapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class for checking if a given plant {@link Material} and {@link Biome} combination is valid
 * based on configured BiomeGroups and Default Biomes.
 */
public class BiomeChecker {
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;

    private final Material plantMaterial;
    private Biome currentBiome;
    private String currentBiomeNameSpace;

    private static final Route biomeGroupsListRoute = Route.from("BiomeGroup", "Groups");
    private static final Route defaultBiomeListRoute = Route.from("Default", "Biome");
    private final Route currentPlantRoute;

    private String matchingBiomeGroup;
    private Section plantSection;

    private final static Map<Material, Biome> validBiomesCache = new HashMap<>();



    /**
     * Constructs a {@link BiomeChecker} for a specific plant {@link Material} and {@link org.bukkit.NamespacedKey}.
     *
     * @param plantMaterial The {@link Material} of the plant.
     * @param currentBiomeNameSpace  The current {@link org.bukkit.NamespacedKey} of a {@link Biome} where the plant is located.
     */
    public BiomeChecker(@NotNull Material plantMaterial, @NotNull NamespacedKey currentBiomeNameSpace) {
        instance = RealisticPlantGrowth.getInstance();
        this.cm = instance.getConfigManager();
        MaterialMapper materialMapper = instance.getVersionMapper().getMaterialMapper();

        this.plantMaterial = plantMaterial;
        this.currentBiomeNameSpace = currentBiomeNameSpace.asString();

        this.currentPlantRoute = materialMapper.getConfigKeyByMaterial(plantMaterial);

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new Biome Checker.");
        logger.verbose("NameSpacedKey: " + currentBiomeNameSpace.asString()); // e.g. terralith:moonlight_valley
        initPlantSection();
    }


    /**
     * Constructs a {@link BiomeChecker} for a specific plant {@link Material} and {@link Biome}.
     *
     * @param plantMaterial The {@link Material} of the plant.
     * @param currentBiome  The current {@link Biome} where the plant is located.
     */
    @Deprecated
    public BiomeChecker(@NotNull Material plantMaterial, @NotNull Biome currentBiome) {
        instance = RealisticPlantGrowth.getInstance();
        this.cm = instance.getConfigManager();
        MaterialMapper materialMapper = instance.getVersionMapper().getMaterialMapper();

        this.plantMaterial = plantMaterial;
        this.currentBiome = currentBiome;

        this.currentPlantRoute = materialMapper.getConfigKeyByMaterial(plantMaterial);

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new Biome Checker.");
        if (RealisticPlantGrowth.isVerbose())
            logger.logToFile("BiomeKey: " + currentBiome.getKey(), "verbose");
        initPlantSection();
    }


    /**
     * Initializes the plant {@link Section} from the {@link ConfigManager}.
     * Throws an exception if the section cannot be obtained.
     */
    private void initPlantSection() {
        logger.verbose(currentPlantRoute.toString());
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
        List<String> biomeGroupStringList = getBiomeGroupStringList();
        if (!biomeGroupStringList.isEmpty()) {
            for (String biomeGroup : biomeGroupStringList) {
                if (instance.isPaperFork()) {
                    // TODO: ConfigManager getBiomeSetOfBiomeGroup need to return Strings
                    for (String biomeString : cm.getBiomeSetOfBiomeGroup(biomeGroup)) {
                        logger.verbose("returned String: " + biomeString);
                        logger.verbose("String to check: " + currentBiomeNameSpace);
                        if (biomeString.equalsIgnoreCase(currentBiomeNameSpace)){
                            matchingBiomeGroup = biomeGroup;
                            return true;
                        }
                    }
                } else {
                    for (String biomeString : cm.getBiomeSetOfBiomeGroup(biomeGroup)) {
                        logger.verbose("returned String: " + biomeString);
                        logger.verbose("String to check: " + currentBiome);
                        if (biomeString.equalsIgnoreCase(currentBiome.toString())){
                            matchingBiomeGroup = biomeGroup;
                            return true;
                        }
                    }
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
        Optional<List<String>> optionalStringList = plantSection.getOptionalStringList(defaultBiomeListRoute);
        List<String> biomeStringList = optionalStringList.orElseGet(ArrayList::new);
        if (!biomeStringList.isEmpty()) {
            if (biomeStringList.size() == 1 && biomeStringList.getFirst().equalsIgnoreCase("ALL"))
                return true;

            if (instance.isPaperFork()) {
                // TODO: ConfigManager getBiomeSetOfBiomeGroup need to return Strings
                logger.logToFile("currentBiomeNameSpaceString: " + currentBiomeNameSpace, "biome");
                return (biomeStringList.contains(currentBiomeNameSpace));
            } else {
                logger.logToFile("currentBiomeNameSpaceString: " + currentBiome.getKey().asString(), "biome");
                return (cm.getCheckedBiomeSet(biomeStringList).contains(currentBiome.getKey().asString()));
            }

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

    @NotNull
    public List<String> getBiomeGroupStringList() {
        Optional<List<String>> biomeGroupStringList = plantSection.getOptionalStringList(biomeGroupsListRoute);
        return biomeGroupStringList.orElseGet(ArrayList::new);
    }

    @NotNull
    public List<String> getDefaultBiomes() {
        Optional<List<String>> biomeStringList = plantSection.getOptionalStringList(defaultBiomeListRoute);
        List<String> stringList = biomeStringList.orElseGet(ArrayList::new);

        if (stringList.size() == 1 && stringList.getFirst().equalsIgnoreCase("ALL")) {
            return List.of("ALL");
        }

        return stringList;
    }

    @Nullable
    public String getMatchingBiomeGroup() {
        return matchingBiomeGroup;
    }


}
