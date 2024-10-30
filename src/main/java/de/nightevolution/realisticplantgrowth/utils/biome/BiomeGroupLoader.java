package de.nightevolution.realisticplantgrowth.utils.biome;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.MaterialMapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BiomeGroupLoader {

    private final ConfigManager cm;
    private final Logger logger;
    private static final Route biomeGroupsListRoute = Route.from("BiomeGroup", "Groups");
    private static final Route defaultBiomeListRoute = Route.from("Default", "Biome");
    // private final Route currentPlantRoute;

    private HashSet<Material, Set<BiomeGroup>> data;

    public BiomeGroupLoader () {

        RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
        this.cm = instance.getConfigManager();
        MaterialMapper materialMapper = instance.getVersionMapper().getMaterialMapper();

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new BiomeLoader...");
    }

    public BiomeGroup loadBiomeGroupFromConfig(Route route) {
        //this.currentPlantRoute = materialMapper.getConfigKeyByMaterial(plantMaterial);

    }

    public Set<BiomeGroup> loadBiomeGroupsFromPlantKey(Route route) {
        //this.currentPlantRoute = materialMapper.getConfigKeyByMaterial(plantMaterial);
        logger.verbose(route.toString());
        Optional<Section> optionalSection = cm.getGrowthModifierSection(currentPlantRoute);
        if (optionalSection.isEmpty()) {
            logger.error("Couldn't read GrowthModifier section for '" + plantMaterial + "'!");
            throw new IllegalArgumentException("Check your GrowthModifiers.yml!");
        }
        plantSection = optionalSection.get();
    }



}
