package de.nightevolution.utils;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BiomeChecker {
    
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;
    private Block blockToCheck;
    private final boolean verbose;

    // TODO: implement biomeGroup caching
    private static Map<Biome, String> biomeGroupsCache = new HashMap<>();



    public BiomeChecker(@NotNull RealisticPlantGrowth instance){
        this.instance = instance;
        this.cm = instance.getConfigManager();
        verbose = cm.isVerbose();

        logger = new Logger(this.getClass().getSimpleName(), instance,
                RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Creating new Biome Checker.");
    }

    public static void clearCache(){
        biomeGroupsCache.clear();
    }

    /**
     * Checks to which BiomeGroup from biomeList a biome belongs.
     * @param biomeList
     * @return
     */
    // TODO: implement biomeGroup caching
    public List<String> getBiomeGroupsOf(@NotNull List<String> biomeList, @NotNull Biome biome, boolean returnAfterFirstHit ){
        List<String> biomeGroupsOfBiome = new ArrayList<>();

        outerLoop:
        for (String biomeGroup : biomeList){

            Route r = Route.from(biomeGroup);
            Optional<List<String>> biomesOfBiomeGroup = cm.getBiomeGroupsFile().getOptionalStringList(r);

            if(biomesOfBiomeGroup.isEmpty() || biomesOfBiomeGroup.get().isEmpty()){
                logger.warn("BiomeGroup '" + biomeGroup + "' is empty! Please check your BiomeGroups.yml");
                continue;
            }

            for (String biomeInGroup : biomesOfBiomeGroup.get()) {
                if(biomeInGroup.equalsIgnoreCase(biome.name())){
                    biomeGroupsOfBiome.add(biomeGroup);
                    if(returnAfterFirstHit)
                        break outerLoop;
                    break;
                }
            }

        }
        if(verbose){
            logger.verbose("BiomeGroups for '" + biome + "': ");
            biomeGroupsOfBiome.forEach((bG) -> logger.verbose("  - " + bG));
        }
        return biomeGroupsOfBiome;
    }

    public String getOneBiomeGroupOf(@NotNull List<String> biomeList, @NotNull Biome biome){
        List<String> incompleteBiomeList = getBiomeGroupsOf(biomeList, biome, true);
        if (incompleteBiomeList.isEmpty())
            return null;
       return incompleteBiomeList.getFirst();
    }

    public Optional<List<String>> getAllBiomeGroupsOf(@NotNull Biome biome){
        Optional<List<String>> biomeGroupsOfBiome;
        Map<String, Object> roots = cm.getBiomeGroups();

        // TODO: NullCheck belongs to ConfigManager
        if(roots == null || roots.isEmpty()){
            logger.warn("No BiomeGroups defined in BiomeGroups.yml!");
            return Optional.empty();
        }

        List<String> allBiomeGroups = new ArrayList<>(roots.keySet());
        biomeGroupsOfBiome = Optional.of(getBiomeGroupsOf(allBiomeGroups, biome, false));

        return biomeGroupsOfBiome;
    }
    public Set<Biome> getBiomeSetOf(String biomeGroup){
        Map<String, Object> roots = cm.getBiomeGroups();
        Set<Biome> biomeSet = new HashSet<>();

        //for (String biomeGroupName : roots.keySet()){
            Route biomeGroupNameRoute = Route.from(biomeGroup);
            logger.verbose(biomeGroupNameRoute.toString());
            Optional<List<String>> biomeStringList = cm.getBiomeGroupStringList(biomeGroupNameRoute);
            logger.verbose("BiomeStringList '" + biomeGroup + "' is present?: " + biomeStringList.isPresent());
            if(biomeStringList.isEmpty() || biomeStringList.get().isEmpty()) {
                return biomeSet; // empty set
            }
            biomeSet.addAll(getBiomesFromStringList(biomeStringList.get()));

       // }

        return biomeSet;
    }


    // TODO: implement biomeGroup caching

    /**
     * Method used to get all biomes for a plant
     * Only used by admin commands
     * TODO: Use Optional and Sections before Release!
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

            validBiomes.addAll(getBiomesFromStringList(resolvedBiomeGroupList));
        }

        if(verbose) {
            logger.verbose("Valid Biomes Set:");
            for (Biome b : validBiomes) {
                logger.verbose("  - " + b);
            }
        }

        // TODO: Add default Biomes here

        return validBiomes;
    }

    public Set<Biome> getBiomesFromStringList(@NotNull List<String> biomeStringList){
        Set<Biome> biomeList = new HashSet<>();
        if(biomeStringList.isEmpty()){
            return biomeList;
        }

        for (String biomeName : biomeStringList){
            try{
                Biome b = Biome.valueOf(biomeName);
                biomeList.add(b);
            }catch (IllegalArgumentException e){
                logger.warn("Biome '" + biomeName + "' is not a valid Biome!");
                logger.warn("Please check your BiomeGroups.yml!");
            }

        }
        return biomeList;
    }


    
}
