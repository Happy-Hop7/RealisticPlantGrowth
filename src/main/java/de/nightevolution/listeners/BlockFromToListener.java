package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.plant.PlantKiller;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;

    public BlockFromToListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    @EventHandler
    public void onWaterDestroyCrops(BlockFromToEvent e) {
        Block source = e.getBlock();
        Block target = e.getToBlock();
        logger.verbose("FromToEvent");
        logger.verbose("Source: " + source.getType());
        logger.verbose("Target: " + target.getType());

        if (instance.isWorldDisabled(target.getWorld())) {
            return;
        }


        if (source.getType() == Material.WATER && instance.getVersionMapper().isAgriculturalPlant(target)) {
            if (cm.isRequire_Hoe()) {
                target.setType(Material.AIR);
                logger.verbose("No drops.");
                e.setCancelled(true);
            }

            if (cm.isDestroy_Farmland()) {
                PlantKiller pk = new PlantKiller();
                pk.destroyFarmland(target);
            }

        }
    }
}
