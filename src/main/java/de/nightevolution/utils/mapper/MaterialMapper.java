package de.nightevolution.utils.mapper;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MaterialMapper {

    private final VersionMapper mapper;
    private final ConfigManager cm;
    private final Logger logger;
    private final String logFile = "debug";


    /**
     * Set of materials representing plants with growth modifications.
     */
    private static HashSet<Material> growthModifiedPlants;
    private static HashSet<Material> growInDarkPlants;
    private final Map<String, List<Material>> plantVariationsMap = new HashMap<>();


    public MaterialMapper(VersionMapper mapper) {
        this.mapper = mapper;
        this.cm = RealisticPlantGrowth.getInstance().getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        initializePlantVariationsMap();
    }

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
        plantVariationsMap.put("pitcher", Collections.singletonList(Material.PITCHER_CROP));
        plantVariationsMap.put("chorus", Arrays.asList(Material.CHORUS_FLOWER, Material.CHORUS_PLANT));
    }


    @Nullable
    public Material getCheckedMaterial(@NotNull String materialString) {
        Material material = Material.getMaterial(materialString);

        if (material == null) {
            logger.warn("Material '" + materialString + "' is not a Bukkit Material!");
        } else if (mapper.isPlantMaterial(material)) {
            return material;
        } else {
            logger.warn("Material '" + materialString + "' is not a Plant Material!");
        }

        return null;
    }


    /**
     * Updates the set of growth-modified plants based on the growth modifier data
     * retrieved from the {@link ConfigManager}. The method iterates through the keys
     * of the growth modifier data, validates and checks if each key represents a plant material.
     * If it is a valid plant material, the {@link Material} is added to the set of growth-modified plants.
     * Additionally, if debug mode is enabled, the modified plant materials are logged
     * to a file asynchronously for debugging purposes.
     */
    protected void updateGrowthModifiedPlants() {
        Map<String, Object> growthModData = cm.getGrowthModifiers();

        growthModifiedPlants = new HashSet<>();

        for (String key : growthModData.keySet()) {
            growthModifiedPlants.addAll(getPlantVariationsOf(key));

            Material notMapped = getCheckedMaterial(key);

            if (notMapped != null) {
                logger.verbose(notMapped + " is not a mapped material.");
                growthModifiedPlants.add(notMapped);

                if (notMapped == Material.VINE || notMapped == Material.GLOW_LICHEN) {
                    logger.warn("'" + Material.VINE + "' and '" + Material.GLOW_LICHEN + "' are not supported yet.");
                }

            } else {
                logger.warn("Plant growth modifiers for '" + key + "' are ignored.");
            }

        }

        logger.verbose("--------- GrowthModifiedPlants ------------");
        for (Material m : growthModifiedPlants) {
            logger.verbose("  - " + m.toString());
        }

    }

    protected void updateGrowInDark() {
        growInDarkPlants = new HashSet<>();

        for (Material m : cm.getGrow_In_Dark()) {
            growInDarkPlants.addAll(getPlantVariationsOf(m.toString()));
        }
    }

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

    public boolean isGrowthModifiedPlant(@NotNull Material material) {
        return growthModifiedPlants.contains(material);
    }

    public boolean isGrowthModifiedPlant(@NotNull Block block) {
        return growthModifiedPlants.contains(block.getType());
    }

    public HashSet<Material> getGrowthModifiedPlants() {
        return growthModifiedPlants;
    }

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
            logger.warn("Ignoring Material '" + materialString + "'.");
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

    // TODO: implement
    public boolean isSamePlant(Block blockToCheck, Material plantMaterial) {
        return false;
    }

    // TODO: implement
    public boolean isSamePlant(Material MaterialToCheck, Material plantMaterial) {
        return false;
    }

}
