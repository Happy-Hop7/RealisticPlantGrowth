package de.nightevolution.realisticplantgrowth.listeners;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.plant.PlantKiller;
import de.nightevolution.realisticplantgrowth.plant.SpecialBlockSearch;
import de.nightevolution.realisticplantgrowth.plant.Surrounding;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Listener;

/**
 * An abstract base class for listeners that handle plant growth events.
 * <p>
 * This class provides methods for processing and modifying plant growth events, including:
 * <ul>
 *     <li>Determining if an event should be canceled based on growth rate and death chance.</li>
 *     <li>Finding the root block of a plant.</li>
 *     <li>Handling the effects of fertilizer usage.</li>
 * </ul>
 * </p>
 */
public abstract class PlantGrowthListener implements Listener {
    protected RealisticPlantGrowth instance;
    protected Logger superLogger;
    protected String logFile = "PlantGrowthEvent";
    protected boolean logEvent;

    protected ConfigManager configManager;
    protected SpecialBlockSearch specialBlockSearch;
    protected Surrounding surrounding;
    protected VersionMapper versionMapper;

    // Event Data
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
     * registers the listener with the server's plugin manager, and obtains necessary components such as
     * SpecialBlockSearch and the configuration manager.
     * </p>
     *
     * @param instance The RealisticPlantGrowth instance to associate with the listener.
     */
    public PlantGrowthListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        // Initialize the logger with verbosity and debugging options.
        superLogger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        superLogger.verbose("Registered new " + this.getClass().getSimpleName() + ".");

        // Register this listener with the server's plugin manager.
        instance.getServer().getPluginManager().registerEvents(this, instance);

        // Initialize components.
        specialBlockSearch = SpecialBlockSearch.get();
        configManager = instance.getConfigManager();
        versionMapper = instance.getVersionMapper();

        // Can get overwritten by child classes.
        logEvent = RealisticPlantGrowth.isDebug() && configManager.isPlant_log();
    }

    /**
     * Processes the plant growth event to determine if it should proceed.
     * <p>
     * Checks if the event block is a growth-modified plant and retrieves environmental data.
     * Logs relevant information if logging is enabled.
     * </p>
     *
     * @return {@code true} if the event should proceed, {@code false} if it should be canceled.
     */
    protected boolean processEvent() {
        if (logEvent) {
            superLogger.logToFile("  Event Block: " + eventBlockType, logFile);
            superLogger.logToFile("  Is Block a growth-modified plant: " +
                    versionMapper.isGrowthModifiedPlant(eventBlockType), logFile);
        }

        // Check if the event block is a growth-modified plant.
        if (!versionMapper.isGrowthModifiedPlant(eventBlockType)) {
            if (logEvent) {
                superLogger.logToFile("  -> Event Block not a growth-modified plant.", logFile);
            }
            return false;
        }

        // Retrieve surrounding environment data.
        surrounding = specialBlockSearch.surroundingOf(eventBlock);

        // Get death chance and growth rate from the surrounding environment.
        deathChance = surrounding.getDeathChance();
        growthRate = surrounding.getGrowthRate();

        if (logEvent) {
            superLogger.logToFile("  Growth Rate: " + growthRate, logFile);
            superLogger.logToFile("  Death Chance: " + deathChance, logFile);
        }
        return true;
    }

    /**
     * Determines whether the plant growth event should be canceled based on configured parameters.
     * <p>
     * Checks conditions such as death chance, growth rate, and specific cancellation rules
     * to decide if the event should be canceled. Logs detailed information if logging is enabled.
     * </p>
     *
     * @return {@code true} if the event should be canceled, {@code false} otherwise.
     */
    protected boolean shouldEventBeCancelled() {
        if (deathChance >= 100.0 || growthRate <= 0.0) {
            if (logEvent) {
                superLogger.logToFile("  -> Event: Kill plant.", logFile);
            }
            killPlant();
            return true;
        }

        if (cancelDueToGrowthRate()) {
            if (logEvent) {
                superLogger.logToFile("  -> Event canceled due to growth rate.", logFile);
            }
            return true;
        }

        if (cancelDueToDeathChance()) {
            if (logEvent) {
                superLogger.logToFile("  -> Event canceled due to death chance.", logFile);
            }
            killPlant();
            return true;
        }

        return false;
    }

    /**
     * Retrieves the root {@link Block} of a specified plant block, considering the growth direction of the plant.
     * <p>
     * This method is designed for plants that grow either upwards (e.g., bamboo, kelp) or downwards
     * (e.g., twisted vines).
     * </p>
     *
     * @param plantBlock The {@link Block} representing the plant.
     * @return The root {@link Block} of the specified plant.
     */
    public Block getRootBlockOf(Block plantBlock) {
        Material plantBlockType = plantBlock.getType();
        Block returnBlock = plantBlock;

        if (logEvent) {
            superLogger.logToFile("  getRootBlockOf(): Starting with plantBlock: " + plantBlock, logFile);
        }

        if (versionMapper.isUpwardsGrowingPlant(plantBlockType)) {
            if (logEvent) {
                superLogger.logToFile("    Searching downwards.", logFile);
            }
            returnBlock = iterateThroughPlantBlocks(plantBlock, BlockFace.DOWN);
        } else if (versionMapper.isDownwardsGrowingPlant(plantBlockType)) {
            if (logEvent) {
                superLogger.logToFile("    Searching upwards.", logFile);
            }
            returnBlock = iterateThroughPlantBlocks(plantBlock, BlockFace.UP);
        }

        if (plantBlockType == Material.VINE || plantBlockType == Material.GLOW_LICHEN) {
            // TODO: Handling special cases like Vines or Glow Lichen here.
        }

        if (logEvent) {
            superLogger.logToFile("  getRootBlockOf(): Found root block: " + returnBlock, logFile);
        }

        return returnBlock;
    }

    /**
     * Iterates through plant {@link Block}s in the specified direction to find the root block.
     * <p>
     * This method traverses the plant blocks in the specified search direction until it finds
     * a block that does not match the type of the initial block.
     * </p>
     *
     * @param plantBlock      The current {@link Block} representing the plant.
     * @param searchDirection The direction ({@link BlockFace}) to search for the root block.
     * @return The root {@link Block} of the specified plant.
     */
    private Block iterateThroughPlantBlocks(Block plantBlock, BlockFace searchDirection) {
        Block currentBlock = plantBlock;
        Block tempBlock;
        String plantBlockTypeName = plantBlock.getType().name();

        while (currentBlock.getType().name().startsWith(plantBlockTypeName)) {
            tempBlock = currentBlock.getRelative(searchDirection);

            if (tempBlock.getType().name().startsWith(plantBlockTypeName)) {
                currentBlock = tempBlock;
            } else {
                break;
            }
        }

        return currentBlock;
    }

    /**
     * Checks if the growth event should be canceled based on the configured growth rate.
     * <p>
     * If a random value exceeds the growth rate, the event will be canceled.
     * </p>
     *
     * @return {@code true} if the event should be canceled due to the growth rate, {@code false} otherwise.
     */
    protected boolean cancelDueToGrowthRate() {
        return (Math.random() * 100) > growthRate;
    }

    /**
     * Checks if the growth event should be canceled based on the configured death chance.
     * <p>
     * If the event block implements the Ageable interface, the death chance is adjusted based on the plant's age.
     * Logs detailed information if logging is enabled.
     * </p>
     *
     * @return {@code true} if the event should be canceled due to the death chance, {@code false} otherwise.
     */
    protected boolean cancelDueToDeathChance() {
        if (eventBlock.getBlockData() instanceof Ageable crop) {
            if ((crop.getAge() != crop.getMaximumAge()) && versionMapper.isAgriculturalPlant(eventBlock)) {
                deathChance /= crop.getMaximumAge();

                if (logEvent)
                    superLogger.verbose("Adjusted DeathChance using Ageable interface.");

            } else if (eventBlockType == Material.BAMBOO) {
                deathChance /= 14;

                if (logEvent)
                    superLogger.verbose("Adjusted DeathChance for Bamboo using Ageable interface.");

            } else {
                if (logEvent)
                    superLogger.verbose("Full DeathChance used with Ageable interface.");
            }

            if (logEvent)
                superLogger.logToFile("  Crop age: " + crop.getAge() + " / " + crop.getMaximumAge(), logFile);
        }
        return (Math.random() * 100) < deathChance;
    }

    /**
     * Initiates the process to kill the plant associated with the current event block.
     * <p>
     * Creates a new PlantKiller instance and uses it to kill the specified plant block.
     * </p>
     */
    protected void killPlant() {
        PlantKiller pk = new PlantKiller();
        pk.killPlant(eventBlock);
    }

    /**
     * Checks if fertilizer was used in the surrounding environment and adjusts the fill level of the composter.
     * <p>
     * If fertilizer was used and passive fertilizer mode is not enabled, this method reduces the fill level
     * of the closest composter to the event block. Logs detailed information if logging is enabled.
     * </p>
     */
    protected void checkFertilizerUsage() {
        if (surrounding.usedFertilizer() && !configManager.isFertilizer_passive()) {
            if (logEvent) {
                superLogger.logToFile("  Fertilizer was used.", logFile);
                superLogger.logToFile("  Reducing fill level of the closest composter.", logFile);
            }
            PlantKiller pk = new PlantKiller();
            pk.reduceComposterFillLevelOf(surrounding.getClosestComposter());
        }
    }

    /**
     * Logs detailed data about the current event.
     * <p>
     * Includes information such as block type, location, world, and biome.
     * </p>
     */
    protected void logEventData() {
        superLogger.logToFile("  Event data:", logFile);
        superLogger.logToFile("    Block Type: " + eventBlockType, logFile);
        superLogger.logToFile("    Location: " + eventLocation, logFile);
        superLogger.logToFile("    World: " + eventWorld.getName(), logFile);
        superLogger.logToFile("    Biome: " + eventBiome, logFile);
    }
}
