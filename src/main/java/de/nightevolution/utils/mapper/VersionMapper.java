package de.nightevolution.utils.mapper;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class VersionMapper {

    private final Logger logger;
    private final MaterialMapper materialMapper;

    /**
     * A set of plant materials used for the 'require_hoe_to_harvest' setting in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material}s represent agricultural plants that require a hoe to be harvested.
     */
    static final Set<Material> agriculturalPlants = new HashSet<>(Arrays.asList(
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
    static final Set<Material> plants = new HashSet<>(Set.of(
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
            Material.DEAD_BUSH,
            Material.GLOW_LICHEN,
            Material.MELON,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN,
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
    static Set<Material> saplings;

    /**
     * A set of all supported aquatic plants in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material} represent plant {@link Block}s typically found in aquatic environments.
     */
    static final Set<Material> aquaticPlants = new HashSet<>(Set.of(
            Material.KELP,
            Material.KELP_PLANT,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    static final Set<Material> upwardsGrowingPlants = new HashSet<>(Set.of(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.KELP,
            Material.KELP_PLANT,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT
    ));

    static final Set<Material> downwardsGrowingPlants = new HashSet<>(Set.of(
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT
    ));

    static final Set<Material> growEventReturnsAirBlockPlants = new HashSet<>(Set.of(
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.SUGAR_CANE,
            Material.CACTUS
    ));


    /**
     * Mapping of clickable seeds to their corresponding plant materials.
     * Key: Clickable Seed ({@link Material}) , Value: Plant {@link Material}
     */
    private static HashMap<Material, Material> clickableSeedsMap;

    /**
     * Set of materials representing clickable seeds.
     */
    private static HashSet<Material> clickableSeeds;


    private static final String logFile = "debug";
    private static final String treeLogFile = "treeLog";

    protected Material grassMaterial;

    protected VersionMapper() {
        this.materialMapper = new MaterialMapper(this);
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    public void reload() {
        plants.add(getGrassMaterial());
        getSaplingsTag();
        materialMapper.updateGrowthModifiedPlants();
        materialMapper.updateGrowInDark();
        updateClickableSeeds();
    }

    /**
     * Adds all saplings to the seeds and plants list.
     * Saplings are chosen by vanilla {@code saplings} tag.
     * AZALEA and FLOWERING_AZALEA are also included.
     */
    private void getSaplingsTag() {
        logger.verbose("Getting saplings tag...");
        saplings = (Tag.SAPLINGS.getValues());
        plants.addAll(saplings);
    }



    /**
     * Updates the set of clickable seeds based on the specified plant and aquatic plant {@link Material}s.
     * Additionally, performs debug logging if the debug mode is enabled.
     */
    protected void updateClickableSeeds() {
        clickableSeedsMap = new HashMap<>();
        clickableSeeds = new HashSet<>();


        for (Material plant : plants) {
            logger.verbose(plant.toString());
            clickableSeedsMap.put(plant.createBlockData().getPlacementMaterial(), plant);
        }

        for (Material plant : aquaticPlants) {
            logger.verbose(plant.toString());
            clickableSeedsMap.put(plant.createBlockData().getPlacementMaterial(), plant);
        }

        clickableSeeds.addAll(clickableSeedsMap.keySet());


        // getPlacementMaterial() returns AIR for e.g. BAMBOO_SAPLING
        clickableSeeds.remove(Material.AIR);

        // also remove torchFlower, since it is already a fully grown decoration plant
        clickableSeeds.remove(Material.TORCHFLOWER);

    }

    // Getters
    public MaterialMapper getMaterialMapper() {
        return materialMapper;
    }

    /**
     * Retrieves the corresponding {@link Material} that a seed converts to if the seed is placed.
     *
     * @param seed {@link Material} representing the seed to inquire about.
     * @return The {@link Material} that the provided seed can grow into, or {@code null} if not applicable.
     */
    @Nullable
    public Material getMaterialFromSeed(@NotNull Material seed) {
        if (clickableSeeds.contains(seed))
            return clickableSeedsMap.get(seed);
        return null;
    }

    public boolean isPlantMaterial(Material material) {
        return plants.contains(material) || aquaticPlants.contains(material);
    }

    /**
     * Checks if the given {@link Material} is a plant.
     *
     * @param m The {@link Material} to check.
     * @return {@code true} if the {@link Material} m is a plant, {@code false} otherwise.
     */
    public boolean isAPlant(@NotNull Material m) {
        return plants.contains(m);
    }

    /**
     * Checks if the given {@link Block} represents an agricultural plant.
     *
     * @param b The {@link Block} to check.
     * @return {@code true} if the {@link Block} b is an agricultural plant, {@code false} otherwise.
     */
    public boolean isAgriculturalPlant(@NotNull Block b) {
        return agriculturalPlants.contains(b.getType());
    }

    /**
     * Checks if the given {@link Material} is an aquatic plant.
     *
     * @param m The {@link Material} to check.
     * @return {@code true} if the {@link Material} m is an aquatic plant, {@code false} otherwise.
     */
    public boolean isAnAquaticPlant(@NotNull Material m) {
        return aquaticPlants.contains(m);
    }

    /**
     * Checks if the given {@link Block} represents a sapling.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the {@link Block} is a sapling, {@code false} otherwise.
     */
    public boolean isSapling(@NotNull Block block) {
        return saplings.contains(block.getType());
    }

    public boolean isSapling(@NotNull Material material) {
        return saplings.contains(material);
    }

    /**
     * Checks if a given {@link Material} represents a clickable seed.
     *
     * @param material The {@link Material} to check for clickability.
     * @return {@code true} if the {@link Material} is a clickable seed, {@code false} otherwise.
     */
    public boolean isClickableSeed(@NotNull Material material) {
        return clickableSeeds.contains(material);
    }

    public boolean isUpwardsGrowingPlant(@NotNull Material material) {
        return upwardsGrowingPlants.contains(material);
    }

    public boolean isDownwardsGrowingPlant(@NotNull Material material) {
        return downwardsGrowingPlants.contains(material);
    }

    public boolean isGrowEventReturnsAirBlockPlant(@NotNull Material material) {
        return growEventReturnsAirBlockPlants.contains(material);
    }

    public HashSet<Material> getGrowthModifiedPlants() {
        return materialMapper.getGrowthModifiedPlants();
    }

    public boolean isGrowthModifiedPlant(@NotNull Material material) {
        return materialMapper.isGrowthModifiedPlant(material);
    }

    public boolean isGrowthModifiedPlant(@NotNull Block block) {
        return materialMapper.isGrowthModifiedPlant(block);
    }

    public Material getGrassMaterial() {
        return grassMaterial;
    }

}
