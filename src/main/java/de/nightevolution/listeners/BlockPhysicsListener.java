package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;

    public BlockPhysicsListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    @EventHandler
    public void onWaterDestroyCrops(BlockPhysicsEvent e) {
        Block source = e.getSourceBlock();
        Block target = e.getBlock();
        logger.verbose("Physics bitch.");

        if (instance.isWorldDisabled(target.getWorld())) {
            return;
        }

        if (cm.isRequire_Hoe()) {
            if (source.getType() == Material.WATER && instance.getVersionMapper().isAgriculturalPlant(target)) {
                target.setType(Material.AIR);
                logger.verbose("No drops.");
            }
        }

    }

}
