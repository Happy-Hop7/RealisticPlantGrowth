package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.PlantKiller;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;


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
    protected Location eventLocation;
    protected World eventWorld;
    protected Biome eventBiome;

    protected double growthRate;
    protected double deathChance;

    protected BlockFace[] blockFaceArray = {
            BlockFace.UP,
            BlockFace.DOWN,
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

    protected boolean shouldEventBeCancelled(){

        if(deathChance >= 100.0 || growthRate <= 0.0){
            logger.verbose("Super-Event: Kill plant.");
            killPlant();
            return true;
        }

        if(cancelDueToGrowthRate()){
            logger.verbose("Event cancelled due to growth rate.");
            return true;
        }

        if(cancelDueToDeathChance()){
            logger.verbose("Event cancelled due to death chance.");
            killPlant();
            return true;
        }

        return false;
    }


    protected void calculateSurroundingOf(@NotNull Block eventBlock){
        // Retrieve surrounding environment data
        surrounding = specialBlockSearch.surroundingOf(eventBlock);

        // Get death chance and growth rate from the surrounding environment
        deathChance = surrounding.getDeathChance();
        growthRate = surrounding.getGrowthRate();
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
            if((crop.getAge() != crop.getMaximumAge()) && (instance.isAgriculturalPlant(eventBlock))) {
                deathChance = (deathChance / crop.getMaximumAge());
                logger.verbose("Using Ageable Interface for partial DeathChance.");
            }

            else logger.verbose("Using Ageable Interface with full DeathChance.");

            logger.verbose("Age of crop: " + crop.getAge() + " / " + crop.getMaximumAge());
        }

        logger.verbose("DeathChance: " + deathChance);
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
