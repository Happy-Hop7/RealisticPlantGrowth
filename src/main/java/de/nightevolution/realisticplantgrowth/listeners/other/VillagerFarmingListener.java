package de.nightevolution.realisticplantgrowth.listeners.other;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.utils.plant.PlantKiller;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class VillagerFarmingListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final VersionMapper vm;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final String logFile = "debug";

    // Event Data
    private Block eventBlock;
    private Material eventBlockType;
    private Location eventLocation;
    private World eventWorld;

    /**
     * Constructs a new VillagerFarmingListener.
     *
     * @param instance The main plugin instance.
     */
    public VillagerFarmingListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.vm = instance.getVersionMapper();
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        this.scheduler = Bukkit.getScheduler();

        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerFarmingCropsEvent(@NotNull EntityChangeBlockEvent e) {
        if (!e.getEntityType().equals(EntityType.VILLAGER)) {
            return;
        }

        if (cm.isDebug_log()) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Villager Farming Event --------------------", logFile);
            logger.logToFile("  Villager: " + e.getEntity().getEntityId(), logFile);
            logger.logToFile("  Entity Location: " + e.getEntity().getLocation(), logFile);
        }

        eventWorld = e.getEntity().getWorld();

        if (instance.isWorldDisabled(eventWorld)) {
            if (cm.isDebug_log()) {
                logger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
            return;
        }

        eventBlock = e.getBlock();
        eventBlockType = e.getBlock().getType();
        eventLocation = e.getBlock().getLocation();

        if (cm.isDebug_log()) {
            logEventData();
        }

        if (!vm.isGrowthModifiedPlant(eventBlockType)) {
            if (cm.getVillager_disable_composter_interaction() && eventBlockType == Material.COMPOSTER) {
                e.setCancelled(true);
                eventLocation.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, e.getEntity().getLocation().toHighestLocation(), 1, 0.5, 0.5, 0.5);
                if (cm.isDebug_log()) {
                    logger.logToFile("  -> " + eventBlockType + " event cancelled.", logFile);
                }
            } else if (cm.isDebug_log()) {
                logger.logToFile("  -> " + eventBlockType + " is not a growth modified plant.", logFile);
            }
            return;
        }


        if (cm.getVillager_require_hoe()) {
            eventBlock.setType(Material.AIR);
            eventLocation.getWorld().playSound(eventLocation, Sound.BLOCK_CROP_BREAK, 1, 1);
        }

        // Destroy Farmland
        if (cm.getVillager_destroy_farmland()) {
            new PlantKiller().destroyFarmland(eventBlock);
        }



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
        logger.logToFile("    villager.destroy_farmland: " + cm.getVillager_destroy_farmland(), logFile);
        logger.logToFile("    villager.require_hoe: " + cm.getVillager_require_hoe(), logFile);
    }

}
