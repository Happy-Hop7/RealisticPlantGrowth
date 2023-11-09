package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final RealisticPlantGrowth instance;
    private ConfigManager configManager;
    private Logger logger;
    public BlockBreakListener(RealisticPlantGrowth instance){
        this.instance = instance;
        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        configManager = instance.getConfigManager();
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    /**
     * converts farmland back to dirt after a crop is harvested
     * @param e
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakEvent (BlockBreakEvent e) {
        Block b = e.getBlock();
        logger.verbose("BlockBreakEvent!");
        logger.verbose("destroy_farmland: " + configManager.isDestroy_farmland());
        logger.verbose("isSolid: " + !b.getType().isSolid());
        logger.verbose("isAPlant: " + RealisticPlantGrowth.isAPlant(b));

        if (configManager.isDestroy_farmland() && !b.getType().isSolid() && RealisticPlantGrowth.isAPlant(b)) {
            Block u = b.getRelative(BlockFace.DOWN);

            if (u.getType().equals(Material.FARMLAND)) {
                logger.verbose("Replacing Farmland.");
                u.setType(Material.COARSE_DIRT);
            }
        }
    }




}
