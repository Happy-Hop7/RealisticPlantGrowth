package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.PlantKiller;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class PlantGrowthListener  implements Listener{
    protected RealisticPlantGrowth instance;
    protected Logger logger;
    protected String logString = "";
    protected ConfigManager configManager;
    protected SpecialBlockSearch specialBlockSearch;
    protected Surrounding surrounding;

    // Event Data
    protected String coords;
    protected Block eventBlock;
    protected Material eventBlockType;
    protected World eventWorld;
    protected Biome eventBiome;

    protected double growthRate;
    protected double deathChance;

    protected BlockFace[] blockFaceArray = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.EAST
    };


    /**
     * Constructs a new PlantGrowthListener instance.
     * <p>
     * Initializes the listener with the provided RealisticPlantGrowth instance, sets up the logger,
     * registers the listener with the server's plugin manager, and obtains necessary components such as SpecialBlockSearch
     * and the configuration manager.
     * </p>
     *
     * @param instance The RealisticPlantGrowth instance to associate with the listener.
     */
    public PlantGrowthListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

        instance.getServer().getPluginManager().registerEvents(this, instance);
        specialBlockSearch = SpecialBlockSearch.get();
        configManager = instance.getConfigManager();

    }

    /**
     * Initializes event-related data for plant growth modification.
     * <p>
     * This method extracts and stores information about the event block, world, biome, and surrounding environment.
     * It checks if the provided event block is a plant eligible for growth modification, and if so,
     * retrieves additional data such as death chance and growth rate from the surrounding environment.
     * </p>
     *
     * @param e The BlockEvent representing the growth-related event.
     * @return True if the event data is successfully initialized and the plant is eligible for growth modification, false otherwise.
     */
    protected boolean initEventData(@NotNull BlockEvent e) {
        // Get coordinates and information of the event block for logging
        eventBlock = e.getBlock();
        eventWorld = eventBlock.getWorld();
        eventBiome = eventBlock.getBiome();
        eventBlockType = eventBlock.getType();

        // Check if the world is enabled for plant growth modification
        return !instance.isWorldDisabled(eventWorld);
    }

    /**
     * @return
     * -1 = return and cancel event
     * 0 = normal event
     * 1 = return without canceling the event
     */
    protected boolean processEvent(){
        // this check needs to be here because the eventBlock can be changed from child classes.
        if(!instance.getGrowthModifiedPlants().contains(eventBlockType))
            return false;

        calculateSurroundingOf(eventBlock);
        logEvent();

        return true;
    }

    @Nullable
    protected Block getAttachedStem() {
        for (BlockFace blockFace : blockFaceArray){
            Block relativeEventBlock = eventBlock.getRelative(blockFace);
            if(relativeEventBlock.getType() == Material.MELON_STEM){
                return relativeEventBlock;
            }
            if(relativeEventBlock.getType() == Material.PUMPKIN_STEM){
                return relativeEventBlock;
            }
        }
        return null;
    }

    protected void calculateSurroundingOf(@NotNull Block eventBlock){
        // Retrieve surrounding environment data
        surrounding = specialBlockSearch.surroundingOf(eventBlock);

        // Get death chance and growth rate from the surrounding environment
        deathChance = surrounding.getDeathChance();
        growthRate = surrounding.getGrowthRate();
    }

    /**
     * Check if the plant should be killed due to high death chance or zero growth rates.
     * @return true if, plant should be killed. false otherwise.
     */
    protected boolean isDeathChanceTooHigh(){
        if(deathChance >= 100.0 || growthRate <= 0.0){
            logger.verbose("Super-Event: Kill plant.");
            killPlant();
            return true;
        }
        return false;
    }

    /**
     * Checks if the growth event should be canceled based on the configured growth rate.
     *
     * @return True if the event should be canceled due to the growth rate, false otherwise.
     */
    protected boolean cancelDueToGrowthRate (){
        return ((Math.random() * 100) > growthRate);
    }

    /**
     * Checks if the growth event should be canceled based on the configured death chance.
     * <p>
     * If the event block implements the Ageable interface, the death chance is adjusted based on the plant's age.
     * </p>
     *
     * @return True if the event should be canceled due to the death chance, false otherwise.
     */
    protected boolean cancelDueToDeathChance(){

        if(eventBlock.getBlockData() instanceof Ageable crop){
            if(crop.getAge() != crop.getMaximumAge()) {
                deathChance = (deathChance / crop.getMaximumAge());
                logger.verbose("Using Ageable Interface for DeathChance.");
                logger.verbose("Partial DeathChance: " + deathChance);
                logger.verbose("Max Age of crop: " + (crop.getMaximumAge()));
            }

            else{
                logger.verbose("Using full DeathChance for fully grown plants.");
                logger.verbose("DeathChance: " + deathChance);
                logger.verbose("Max Age of crop: " + (crop.getMaximumAge()));
            }

        }

        return ((Math.random() * 100) < deathChance);
    }

    /**
     * Initiates the process to kill the plant associated with the current event block.
     * <p>
     * Creates a new PlantKiller instance and uses it to initiate the plant killing process for the specified event block.
     * </p>
     */
    protected void killPlant(){
        PlantKiller pk = new PlantKiller();
        pk.killPlant(eventBlock);
    }

    /**
     * Checks if fertilizer was used in the surrounding environment
     * and reduces the fill level of the corresponding composter.
     * <p>
     * If a fertilizer was used and passive fertilizer mode is not enabled,
     * this method initiates the process to reduce the fill level of the composter closest to the event block.
     * </p>
     */
    protected void checkFertilizerUsage(){
        if(surrounding.usedFertilizer() && !configManager.isFertilizer_passiv()){
            logger.verbose("Fertilizer was used. Reducing fill level of particular Composter.");
            PlantKiller pk = new PlantKiller();
            pk.reduceComposterFillLevelOf(surrounding.getClosestComposter());
        }
    }

    protected void logEvent(){
        // Log coordinates if logging is enabled
        if (configManager.isLog_Coords()) {
            coords = "[ " +
                    eventBlock.getLocation().getBlockX() + ", " +
                    eventBlock.getLocation().getBlockY() + ", " +
                    eventBlock.getLocation().getBlockZ() + "] ";
            logString += coords;
        }
        logger.verbose(logString);
        logger.verbose("EventBiome: " + eventBiome);
        logger.verbose("DeathChance: " + deathChance);
        logger.verbose("GrowthRate: " + growthRate);
    }

}
