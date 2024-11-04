package de.nightevolution.realisticplantgrowth.utils.mapper;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Mapper for handling various material-related operations in the RealisticPlantGrowth plugin.
 * Manages growth modifications, plant variations, and interactions with the plugin's configuration.
 */
public class MaterialMapper {

    private final VersionMapper versionMapper;
    private final ConfigManager cm;
    private final Logger logger;
    private final String logFile = "verbose";

    /**
     * Set of materials representing plants with growth modifications.
     */
    private static HashSet<Material> growthModifiedPlants;
    private static HashSet<Material> growInDarkPlants;
    private final Map<String, List<Material>> plantVariationsMap = new HashMap<>();

    /**
     * Constructor for {@link MaterialMapper}.
     *
     * @param versionMapper The associated {@link VersionMapper} instance.
     */
    public MaterialMapper(VersionMapper versionMapper) {
        this.versionMapper = versionMapper;
        this.cm = RealisticPlantGrowth.getInstance().getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        initializePlantVariationsMap();
    }

    /**
     * Initializes a {@link Map} of plant variations for different plant types. <p>
     * This method populates a map where each entry represents a plant type (e.g., "bamboo") and its associated
     * list of {@link Material} variations. The variations include different materials representing the same plant type,
     * such as mature plants and saplings.
     */
    private void initializePlantVariationsMap() {
        plantVariationsMap.put("bamboo", Arrays.asList(Material.BAMBOO, Material.BAMBOO_SAPLING));
        plantVariationsMap.put("melon", Collections.singletonList(Material.MELON_STEM));
        plantVariationsMap.put("pumpkin", Collections.singletonList(Material.PUMPKIN_STEM));
        plantVariationsMap.put("carrot", Collections.singletonList(Material.CARROTS));
        plantVariationsMap.put("potato", Collections.singletonList(Material.POTATOES));
        plantVariationsMap.put("cocoa", Collections.singletonList(Material.COCOA));
        plantVariationsMap.put("beetroot", Collections.singletonList(Material.BEETROOTS));
        plantVariationsMap.put("cave_vine", Arrays.asList(Material.CAVE_VINES, Material.CAVE_VINES_PLANT));
        plantVariationsMap.put("twisting_vine", Arrays.asList(Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT));
        plantVariationsMap.put("weeping_vine", Arrays.asList(Material.WEEPING_VINES, Material.WEEPING_VINES_PLANT));
        plantVariationsMap.put("kelp", Arrays.asList(Material.KELP, Material.KELP_PLANT));
        plantVariationsMap.put("chorus", Arrays.asList(Material.CHORUS_FLOWER, Material.CHORUS_PLANT));

        if (versionMapper.getPitcherPlacedMaterial() != null) {
            plantVariationsMap.put("pitcher", Collections.singletonList(versionMapper.getPitcherPlacedMaterial()));
        }

        if (versionMapper.getTorchflowerMaterial() != null) {
            plantVariationsMap.put("torchflower",
                    Arrays.asList(versionMapper.getTorchflowerMaterial(), versionMapper.getTorchflowerPlacedMaterial()));
        }

    }

    /**
     * Retrieves a Bukkit {@link Material} based on the given material string.<p>
     * This method attempts to get a Bukkit {@link Material} using the provided material string.
     * If the material is not found or is not considered a plant material, a warning is logged,
     * and null is returned.
     *
     * @param materialString The string representation of the {@link Material} to retrieve.
     * @return The Bukkit {@link Material} if found and considered a plant material, otherwise null.
     */
    @Nullable
    public Material getCheckedMaterial(@NotNull String materialString) {

        Material material = Material.getMaterial(materialString);

        if (material == null) {
            logger.warn("Material '" + materialString + "' is not a Bukkit Material!");
        } else if (versionMapper.isPlantMaterial(material)) {
            return material;
        } else {
            logger.warn("Material '" + materialString + "' is not a Plant Material!");
        }

        return null;
    }

    /**
     * Updates the set of {@link Material}s representing plants with modified growth behavior based on configuration data.
     * <p>
     * This method reads growth modification data from the configuration, processes each entry, and updates
     * the set of growth-modified plants accordingly. It logs information about each processed material,
     * including those not mapped to known plant variations. Unsupported materials, such as VINE and GLOW_LICHEN,
     * trigger warnings. The final set of growth-modified plants is then logged for debugging purposes.
     */
    protected void updateGrowthModifiedPlants() {
        Map<String, Object> growthModData = cm.getGrowthModifiers();

        growthModifiedPlants = new HashSet<>();

        for (String key : growthModData.keySet()) {
            growthModifiedPlants.addAll(getPlantVariationsOf(key));

            Material notMapped = Material.getMaterial(key);

            if (notMapped != null) {
                logger.verbose(notMapped + " is not a mapped material.");

                if (notMapped == Material.VINE || notMapped == Material.GLOW_LICHEN) {
                    logger.warn("'" + Material.VINE + "' and '" + Material.GLOW_LICHEN + "' are not supported yet.");
                }

            } else {
                logger.warn("Plant growth modifiers for '" + key + "' are ignored.");
            }

        }

        if (RealisticPlantGrowth.isVerbose()) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Updated GrowthModifiedPlants --------------------", logFile);
            for (Material m : growthModifiedPlants) {
                logger.logToFile("  - " + m.toString(), logFile);
            }
        }
    }

    /**
     * Updates the set of {@link Material}s that can grow in the dark based on configuration data.
     * <p>
     * This method reads materials configured to grow in the dark from the configuration and updates
     * the set of materials that can grow in the dark accordingly. It processes each material entry
     * using the {@code getPlantVariationsOf} method, which includes variations of each specified plant type.
     */
    protected void updateGrowInDark() {
        growInDarkPlants = new HashSet<>();

        for (Material m : cm.getGrow_In_Dark()) {
            growInDarkPlants.addAll(getPlantVariationsOf(m.toString()));
        }
    }

    /**
     * Retrieves the configuration key associated with the specified Bukkit {@link Material}.
     * <p>
     * This method iterates through the plantVariationsMap to find the entry containing the specified Material.
     * It then retrieves the corresponding configuration key using {@code findConfigKeyInGrowthModifiers} and returns it as a Route.
     *
     * @param material The Bukkit {@link Material} for which to retrieve the configuration key.
     * @return The {@link Route} representing the configuration key associated with the specified Material.
     */
    @NotNull
    public Route getConfigKeyByMaterial(@NotNull Material material) {
        for (Map.Entry<String, List<Material>> entry : plantVariationsMap.entrySet()) {
            if (entry.getValue().contains(material)) {
                String configKey = entry.getKey();
                return Route.from(findConfigKeyInGrowthModifiers(configKey));
            }
        }

        return Route.from(findConfigKeyInGrowthModifiers(material.toString()));
    }

    /**
     * Finds the configuration key associated with the specified key in the {@code GrowthModifiers.yml} file.
     * <p>
     * This method searches through the keys in the {@code GrowthModifiers.yml} file to find a key containing the specified configKey.
     * It returns the found key if there is a match; otherwise, it throws an {@link IllegalArgumentException}.
     *
     * @param configKey The key for which to find the associated configuration key.
     * @return The configuration key associated with the specified key in the {@code GrowthModifiers.yml} file.
     * @throws IllegalArgumentException If the specified key is not found in the {@code GrowthModifiers.yml} file.
     */
    @NotNull
    private String findConfigKeyInGrowthModifiers(@NotNull String configKey) {
        for (String key : cm.getGrowthModifiers().keySet()) {
            if (key.contains(configKey.toUpperCase())) {
                return key;
            }
        }

        throw new IllegalArgumentException(
                getClass().getSimpleName() + ": Material '" + configKey + "' not found in your GrowthModifiers.yml!");
    }

    // Getters

    /**
     * Checks if the specified Bukkit {@link Material} is included in the set of growth-modified plants.
     *
     * @param material The Bukkit {@link Material} to check.
     * @return {@code true} if the {@link Material} is a growth-modified plant, {@code false} otherwise.
     */
    public boolean isGrowthModifiedPlant(@NotNull Material material) {
        return growthModifiedPlants.contains(material);
    }

    /**
     * Retrieves the {@link Set} of {@link Material} representing plants with modified growth behavior.
     *
     * @return The {@link Set} of growth-modified plants.
     */
    public HashSet<Material> getGrowthModifiedPlants() {
        return growthModifiedPlants;
    }

    /**
     * Retrieves a {@link List} of {@link Material} variations associated with the specified material string.
     * <p>
     * This method searches through the plantVariationsMap to find entries containing the specified material string.
     * It returns the associated list of material variations. If no variations are found, it checks if the material
     * is a valid plant material using 'getCheckedMaterial' and returns a singleton list with the validated material,
     * or an empty list if the material is ignored.
     *
     * @param materialString The string representation of the {@link Material} for which to retrieve variations.
     * @return A {@link List} of {@link Material} variations associated with the specified material string.
     */
    @NotNull
    public List<Material> getPlantVariationsOf(@NotNull String materialString) {
        String loweredMaterialString = materialString.toLowerCase();
        for (String searchString : plantVariationsMap.keySet()) {
            if (loweredMaterialString.contains(searchString)) {
                return plantVariationsMap.get(searchString);
            }
        }

        Material checkedMaterial = getCheckedMaterial(materialString);
        if (checkedMaterial != null) {
            return Collections.singletonList(checkedMaterial);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if the specified material can grow in the dark based on configuration.
     *
     * @param m The {@link Material} to check.
     * @return {@code true} if the {@link Material} m can grow in the dark, {@code false} otherwise.
     */
    public boolean canGrowInDark(@NotNull Material m) {
        return growInDarkPlants.contains(m);
    }

}