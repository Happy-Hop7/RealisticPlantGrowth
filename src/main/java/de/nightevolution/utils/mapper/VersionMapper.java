package de.nightevolution.utils.mapper;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class VersionMapper {

    private final RealisticPlantGrowth instance;
    private final Logger logger;

    /**
     * A set of plant materials used for the 'require_hoe_to_harvest' setting in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material}s represent agricultural plants that require a hoe to be harvested.
     */
    protected static final Set<Material> agriculturalPlants = new HashSet<>(Arrays.asList(
            Material.ATTACHED_MELON_STEM,
            Material.ATTACHED_PUMPKIN_STEM,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.WHEAT
    ));

    /**
     * A set of all supported land plants in the {@link RealisticPlantGrowth} plugin.
     * This set includes various plant {@link Material}s found on land.
     * Saplings are added later to this set.
     */
    protected static final Set<Material> plants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.BROWN_MUSHROOM,
            Material.BEETROOTS,
            Material.CACTUS,
            Material.CARROTS,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.COCOA,
            Material.CRIMSON_FUNGUS,
            Material.GLOW_LICHEN,
            Material.SHORT_GRASS,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.RED_MUSHROOM,
            Material.SUGAR_CANE,
            Material.SWEET_BERRY_BUSH,
            Material.TALL_GRASS,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT,
            Material.VINE,
            Material.WARPED_FUNGUS,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT,
            Material.WHEAT
    ));

    /**
     * A set of all supported aquatic plants in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material} represent plant {@link Block}s typically found in aquatic environments.
     */
    protected static final Set<Material> aquaticPlants = new HashSet<>(Arrays.asList(
            Material.KELP,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    protected static final Set<Material> upwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.KELP,
            Material.KELP_PLANT,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT
    ));

    protected static final Set<Material> downwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT
    ));

    protected static final Set<Material> growEventReturnsAirBlockPlants = new HashSet<>(Arrays.asList(
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.SUGAR_CANE,
            Material.CACTUS
    ));

    protected VersionMapper() {
        this.instance = RealisticPlantGrowth.getInstance();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    /**
     * Checks if the given String is a valid plant {@link Material} in the {@link Bukkit} {@link Material} system.
     *
     * @param materialString The string representation of the {@link Material}  to be checked.
     * @return {@code true} if the material is a valid plant or aquatic plant material, {@code false} otherwise.
     */
    public boolean checkPlantMaterial(@NotNull String materialString) {
        Material m = Material.getMaterial(materialString);

        if (m == null)
            logger.warn("Material '" + materialString + "' is not a Bukkit Material!");

        else if (instance.isAPlant(m) || instance.isAnAquaticPlant(m))
            return true;

        else
            logger.warn("Material '" + materialString + "' is not a Plant Material!");


        return false;
    }

    public Material getMappedPlantName(@NotNull Material m) {
        String materialString = m.toString().toLowerCase();

        if (materialString.contains("bamboo"))
            return Material.BAMBOO;

        if (materialString.contains("melon"))
            return Material.MELON_STEM;

        if (materialString.contains("pumpkin"))
            return Material.PUMPKIN_STEM;

        if (materialString.contains("carrot"))
            return Material.CARROTS;

        if (materialString.contains("potato")) {
            return Material.POTATOES;
        }

        if (materialString.contains("cocoa")) {
            return Material.COCOA;
        }

        if (materialString.contains("beetroot")) {
            return Material.BEETROOTS;
        }

        if (materialString.contains("cave_vine")) {
            return Material.CAVE_VINES;
        }

        if (materialString.contains("twisting_vine")) {
            return Material.TWISTING_VINES;
        }

        if (materialString.contains("weeping_vine")) {
            return Material.WEEPING_VINES;
        }

        if (materialString.contains("kelp")) {
            return Material.KELP;
        }

        return m;
    }


}
