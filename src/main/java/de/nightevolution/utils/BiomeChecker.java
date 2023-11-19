package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiomeChecker {
    
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;
    private Block blockToCheck;
    private final boolean verbose;

    public BiomeChecker(RealisticPlantGrowth instance){
        this.instance = instance;
        this.cm = instance.getConfigManager();
        verbose = cm.isVerbose();

        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new Biome Checker.");
    }

    public boolean isInValidBiome(@NotNull Surrounding surrounding){
        Material plantType = surrounding.getType();
        Biome currentBiome = surrounding.getBiome();
        Set<Biome> validBiomes = getValidBiomesFor(plantType);

        if(validBiomes == null || validBiomes.isEmpty()){
            return false;
        }

        return validBiomes.contains(currentBiome);
    }

    public boolean isInValidBiome(@NotNull Block b){
        Material plantType = b.getType();
        Biome currentBiome = b.getBiome();
        Set<Biome> validBiomes = getValidBiomesFor(plantType);

        if(validBiomes == null || validBiomes.isEmpty()){
            return false;
        }

        return validBiomes.contains(currentBiome);
    }

    /**
     * Method used to get all biomes for a plant
     * Only used by admin commands
     */
    public Set<Biome> getValidBiomesFor(@NotNull Material plant){
        YamlDocument biomeGroupFile = cm.getBiomeGroupsFile();
        YamlDocument growthModFile = cm.getGrowthModifiersFile();
        Set<Biome> validBiomes = new HashSet<>();
        Set<Material> growthModifiedPlants = instance.getGrowthModifiedPlants();

        logger.verbose("getValidBiomesFor(" + plant + ")");
        if(!growthModifiedPlants.contains(plant)){
            logger.verbose(plant + " is not listed in GrowthModifiers.yml!");
            return null;
        }

        Route biomeGroupsRoute = Route.from(plant.toString(), "BiomeGroup", "Groups");
        logger.verbose(biomeGroupsRoute.toString());


        List<String> biomeGroupStingList = growthModFile.getStringList(biomeGroupsRoute);
        if(biomeGroupStingList == null) {
            logger.verbose("No BiomeGroups: NULL");
            return null;
        }

        if(biomeGroupStingList.isEmpty()) {
            logger.verbose("No BiomeGroups: isEmpty");
            return null;
        }

        logger.verbose("Biome Groups:");
        for (String biomeGroup : biomeGroupStingList){
            Route biomeRoute = Route.fromString(biomeGroup);
            List<String> resolvedBiomeGroupList = biomeGroupFile.getStringList(biomeRoute);

            if(resolvedBiomeGroupList.isEmpty()) {
                logger.warn("BiomeGroup '" + biomeGroup + "' not defined or empty!");
                continue;
            }

            if(verbose){
                logger.verbose(biomeGroup);
                for (String s : resolvedBiomeGroupList){
                    logger.verbose("  - " + s);
                }
            }

            for (String biome : resolvedBiomeGroupList){
                try{
                    Biome b = Biome.valueOf(biome);
                    validBiomes.add(b);
                }catch (IllegalArgumentException e){
                    logger.warn("Biome '" + biome + "' is not a valid Biome!");
                    logger.warn("Please check your BiomeGroups.yml!");
                }

            }

        }
        if(verbose) {
            logger.verbose("Valid Biomes Set:");
            for (Biome b : validBiomes) {
                logger.verbose("  - " + b);
            }
        }
        return validBiomes;
    }
    
}
