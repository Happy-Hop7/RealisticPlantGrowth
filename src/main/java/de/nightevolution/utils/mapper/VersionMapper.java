package de.nightevolution.utils.mapper;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public abstract class VersionMapper {

    private final RealisticPlantGrowth instance;
    private final Logger logger;

    protected VersionMapper() {
        instance = RealisticPlantGrowth.getInstance();
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
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

    public String getOriginalPlantName(Material m) {
        return mappedPlantNames.get(m);
    }

}
