package de.nightevolution.utils.plant;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.enums.DeathChanceType;
import de.nightevolution.utils.enums.GrowthModifierType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Optional;


/**
 * The Modifier class represents modifiers for plant growth in the RealisticPlantGrowth plugin.
 * It includes properties such as growthModifier, deathChance, and the information if a fertilizer was used.
 */
public class Modifier {

    private final RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final ConfigManager cm = instance.getConfigManager();
    private final Logger logger;
    private final GrowthModifierType growthModifierType;
    private final DeathChanceType deathChanceType;
    private double growthModifier;
    private double deathChance;
    private boolean fertilizerUsed;
    private final String biomeGroup;
    private boolean specialCase = false;

    private final Section modifierSection;


    /**
     * Constructs a Modifier for a specific plant type, biome group, growth modifier type, and death chance type.
     *
     * @param plantType          The material of the plant.
     * @param biomeGroup         The biome group (nullable).
     * @param growthModifierType The type of growth modifier.
     * @param deathChanceType    The type of death chance.
     */
    public Modifier(@NotNull Material plantType, @Nullable String biomeGroup,
                    @NotNull GrowthModifierType growthModifierType,
                    @NotNull DeathChanceType deathChanceType) {

        this.biomeGroup = biomeGroup;
        this.growthModifierType = growthModifierType;
        this.deathChanceType = deathChanceType;

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Created new " + this.getClass().getSimpleName() + ".");


        Optional<Section> optionalSection = cm.getGrowthModifierSection(Route.from(instance.getOriginalPlantName(plantType)));
        if (optionalSection.isEmpty()) {
            logger.error("Section '" + plantType + " couldn't be obtained.");
            throw new YAMLException("Check your GrowthModifiers.yml!");
        }

        this.modifierSection = optionalSection.get();

        initModifier();
    }

    /**
     * Creates a "Death" Modifier indicating a plant with no survival chance.
     */
    public Modifier() {
        this.biomeGroup = null;
        this.growthModifierType = null;
        this.deathChanceType = null;
        this.modifierSection = null;
        this.fertilizerUsed = false;

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Created new " + this.getClass().getSimpleName() + ".");

        growthModifier = 0.0;
        deathChance = 100.0;

    }

    /**
     * Initializes the modifier properties based on the configuration.
     */
    private void initModifier() {

        Route growthModifierRoute;
        Route deathChanceRoute;

        if (growthModifierType == GrowthModifierType.FERTILIZER_INVALID_BIOME ||
                deathChanceType == DeathChanceType.FERTILIZER_INVALID_BIOME) {

            growthModifier = getCheckedGrowthModifier(cm.getFertilizer_invalid_biome_growth_rate());
            deathChance = getCheckedDeathChance(cm.getFertilizer_invalid_biome_death_chance());
            return;

        } else if (biomeGroup == null) {
            growthModifierRoute = Route.from("Default", growthModifierType);
            deathChanceRoute = Route.from("Default", deathChanceType);
        } else {
            growthModifierRoute = Route.from("BiomeGroup", biomeGroup, growthModifierType);
            deathChanceRoute = Route.from("BiomeGroup", biomeGroup, deathChanceType);
        }

        logger.verbose("growthModifierRoute: " + growthModifierRoute);
        logger.verbose("deathChanceRoute: " + deathChanceRoute);

        Optional<Double> optionalGrowthDouble = modifierSection.getOptionalDouble(growthModifierRoute);
        Optional<Double> optionalDeathDouble = modifierSection.getOptionalDouble(deathChanceRoute);

        if (optionalDeathDouble.isEmpty() || optionalGrowthDouble.isEmpty()) {
            throw new IllegalArgumentException("GrowthModifier couldn't be obtained!");
        }

        this.growthModifier = getCheckedGrowthModifier(optionalGrowthDouble.get());
        this.deathChance = getCheckedDeathChance(optionalDeathDouble.get());
    }

    /**
     * Applies fertilizer effects to the provided modifier if fertilizer boost can be applied.
     */
    public void applyFertilizerEffects() {
        double fertilizerBoost;

        if (specialCase) {
            fertilizerBoost = ((getGrowthModifier() / 100) * (cm.getFertilizer_invalid_biome_growth_rate() / 100) * 100);
            deathChance += cm.getFertilizer_invalid_biome_death_chance();

        } else
            fertilizerBoost = getGrowthModifier() + cm.getFertilizer_boost_growth_rate();

        setGrowthModifier(fertilizerBoost);
        setFertilizerUsed(true);
    }

    /**
     * Gets the current growth modifier.
     *
     * @return The growth modifier value.
     */
    public double getGrowthModifier() {
        return growthModifier;
    }

    /**
     * Gets the current death chance.
     *
     * @return The death chance value.
     */
    public double getDeathChance() {
        return deathChance;
    }

    /**
     * Checks if fertilizer was used to calculate the modifiers.
     *
     * @return True if fertilizer was used, false otherwise.
     */
    public boolean isFertilizerUsed() {
        return fertilizerUsed;
    }

    /**
     * Sets the growth modifier with the specified value.
     *
     * @param growthModifier The new growth modifier value.
     */
    public void setGrowthModifier(double growthModifier) {
        this.growthModifier = getCheckedGrowthModifier(growthModifier);
    }

    /**
     * Sets the death chance with the specified value.
     *
     * @param deathChance The new death chance value.
     */
    public void setDeathChance(double deathChance) {
        this.deathChance = getCheckedDeathChance(deathChance);
    }

    /**
     * Sets whether fertilizer was used or not.
     *
     * @param fertilizerUsed True if fertilizer was used, false otherwise.
     */
    public void setFertilizerUsed(boolean fertilizerUsed) {
        this.fertilizerUsed = fertilizerUsed;
    }


    public void setSpecialCase(boolean specialCase) {
        this.specialCase = specialCase;
    }

    public boolean getSpecialCase() {
        return specialCase;
    }

    public GrowthModifierType getGrowthModifierType() {
        return growthModifierType;
    }

    public DeathChanceType getDeathChanceType() {
        return deathChanceType;
    }

    /**
     * Ensures that the provided growth modifier is within valid bounds.
     *
     * @param growthModifier The growth modifier to be checked.
     * @return The corrected growth modifier value.
     */
    private double getCheckedGrowthModifier(double growthModifier) {
        if (growthModifier <= 0.0)
            return 0.0;
        if (growthModifier > 100.0 && !cm.isFertilizer_allow_growth_rate_above_100()) {
            return 100.0;
        }
        return growthModifier;
    }

    /**
     * Ensures that the provided death chance is within valid bounds.
     *
     * @param deathChance The death chance to be checked.
     * @return The corrected death chance value.
     */
    private double getCheckedDeathChance(double deathChance) {
        if (deathChance < 0.0)
            return 0.0;

        return Math.min(deathChance, 100.0);

    }

}
