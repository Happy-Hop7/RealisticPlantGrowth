package de.nightevolution.realisticplantgrowth.listeners.other;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.plant.PlantKiller;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

/**
 * A listener class for handling {@link BlockFromToEvent} to control the destruction of crops by water flow.
 */
public class BlockFromToListener implements Listener {
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;

    /**
     * Constructs a new {@link BlockFromToListener}.
     *
     * @param instance The main plugin instance of RealisticPlantGrowth.
     */
    public BlockFromToListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    /**
     * Handles the {@link BlockFromToEvent} to prevent the destruction of crops by water flow based on configuration settings.
     *
     * @param e The {@link BlockFromToEvent} triggered by the flow of water.
     */
    @EventHandler(ignoreCancelled = true)
    public void onWaterDestroyCrops(BlockFromToEvent e) {
        Block source = e.getBlock();
        Block target = e.getToBlock();
        logger.verbose("BlockFromToEvent");
        logger.verbose("Source Block: " + source.getType() + " at " + source.getLocation());
        logger.verbose("Target Block: " + target.getType() + " at " + target.getLocation());

        // Check if the target world is disabled in the configuration.
        if (instance.isWorldDisabled(target.getWorld())) {
            return;
        }

        // Check if the source block is water and the target block is an agricultural plant.
        if (source.getType() == Material.WATER && instance.getVersionMapper().isAgriculturalPlant(target)) {

            // Check if requiring a hoe is enabled in the configuration.
            if (cm.isRequire_Hoe()) {
                target.setType(Material.AIR);
                e.setCancelled(true);
                logger.verbose("Plant at " + target.getLocation() + " destroyed to prevent drops.");
            }

            // Check if destroying farmland is enabled in the configuration.
            if (cm.isDestroy_Farmland()) {
                PlantKiller pk = new PlantKiller();
                pk.destroyFarmland(target);
                logger.verbose("Farmland of plant at " + target.getLocation() + " destroyed.");
            }

        }
    }
}
