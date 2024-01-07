package de.nightevolution.utils.mapper.versions;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Version_1_20_4 extends VersionMapper {
    /**
     * A set of plant materials used for the 'require_hoe_to_harvest' setting in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material}s represent agricultural plants that require a hoe to be harvested.
     */
    private static final Set<Material> agriculturalPlants = new HashSet<>(Arrays.asList(
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
    private static final Set<Material> plants = new HashSet<>(Arrays.asList(
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
    private static final Set<Material> aquaticPlants = new HashSet<>(Arrays.asList(
            Material.KELP,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    private static final Set<Material> upwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.KELP,
            Material.KELP_PLANT,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT
    ));

    private static final Set<Material> downwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT
    ));

    private static final Set<Material> growEventReturnsAirBlockPlants = new HashSet<>(Arrays.asList(
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.SUGAR_CANE,
            Material.CACTUS
    ));

    public Version_1_20_4() {
        super();
    }

}
