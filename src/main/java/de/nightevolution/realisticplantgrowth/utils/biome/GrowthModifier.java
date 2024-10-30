package de.nightevolution.realisticplantgrowth.utils.biome;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.enums.DeathChanceType;
import de.nightevolution.realisticplantgrowth.utils.enums.GrowthModifierType;
import org.bukkit.NamespacedKey;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

@Getter
public class GrowthModifier {

    private final RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
    private final ConfigManager cm = instance.getConfigManager();
    private final Logger logger;

    private double growthRate;
    private double deathChance;
    private double growthRateUVLight;
    private double deathChanceUVLight;

    private HashSet<NamespacedKey> validBiomes;


    public GrowthModifier(double growthRate, double deathChance, double growthRateUVLight, double deathChanceUVLight) {
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Created new " + this.getClass().getSimpleName() + ".");

        this.growthRate = growthRate;
        this.deathChance = deathChance;
        this.growthRateUVLight = growthRateUVLight;
        this.deathChanceUVLight = deathChanceUVLight;
    }


    public double getGrowthModifierFor(@NotNull GrowthModifierType growthModifierType) {
        return switch (growthModifierType) {
            case GrowthRate        -> growthRate;
            case UVLightGrowthRate -> growthRateUVLight;
            case FERTILIZER_INVALID_BIOME ->
                // TODO: Make configurable per plant
                cm.getFertilizer_invalid_biome_growth_rate();
        };
    }

    public double getDeathChanceFor(@NotNull DeathChanceType deathChanceType) {
        return switch (deathChanceType) {
            case NaturalDeathChance -> deathChance;
            case UVLightDeathChance -> deathChanceUVLight;
            case FERTILIZER_INVALID_BIOME ->
                // TODO: Make configurable per plant
                    cm.getFertilizer_invalid_biome_death_chance();
        };
    }


}
