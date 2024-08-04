package de.nightevolution.realisticplantgrowth.listeners.other;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to block changes resulting from fertilizing a given block with bonemeal either by a player or by dispensers.
 */
public class BonemealListener implements Listener {

    // Log data
    private final String logFile;
    private final boolean logEvent;
    private RealisticPlantGrowth instance;
    private VersionMapper versionMapper;
    private Logger logger;
    // Event Data
    private Block eventBlock;
    private Material eventBlockType;
    private Location eventLocation;
    private World eventWorld;

    public BonemealListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        // Initialize the logger with verbosity and debugging options.
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");

        // Register this listener with the server's plugin manager.
        instance.getServer().getPluginManager().registerEvents(this, instance);

        // Set up logging specific for block fertilization events
        logEvent = RealisticPlantGrowth.isDebug() && instance.getConfigManager().isBonemeal_log();
        logFile = "Bonemeal";
    }

    /**
     * Handles block changes when a player uses bonemeal or other fertilizing items.
     * <p>
     * This method listens to player interactions and block growth events to determine
     * if they are caused by fertilization. It processes the event accordingly and checks
     * if it should be cancelled based on configured parameters.
     * </p>
     *
     * @param e The event to handle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBonemealEvent(@NotNull PlayerInteractEvent e) {

        if (logEvent) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Player Bonemeal Event --------------------", logFile);
            logger.logToFile("  Player: " + e.getPlayer().getName(), logFile);
            logger.logToFile("  Player location: " + e.getPlayer().getLocation(), logFile);
        }

        eventWorld = e.getPlayer().getWorld();

        if (instance.isWorldDisabled(eventWorld)) {
            if (logEvent) {
                logger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
            return;
        }

        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) {
            return;
        }

        if (logEvent) {
            logEventData();
            logger.logToFile("  Initialized BlockBonemealEvent.", logFile);
        }

        //TODO: implementation here
    }

    /**
     * Logs detailed data about the current event.
     * <p>
     * Includes information such as block type, location, world, and biome.
     * </p>
     */
    protected void logEventData() {
        logger.logToFile("  Event data:", logFile);
        logger.logToFile("    Block Type: " + eventBlockType, logFile);
        logger.logToFile("    Location: " + eventLocation, logFile);
        logger.logToFile("    World: " + eventWorld.getName(), logFile);
    }
}