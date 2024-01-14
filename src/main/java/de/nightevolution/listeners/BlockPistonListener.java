package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import java.util.List;

/**
 * A listener class for handling {@link BlockPistonExtendEvent} to control plant destruction by pistons.
 */
public class BlockPistonListener implements Listener {
    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;

    /**
     * Constructs a new {@link BlockPistonListener}.
     *
     * @param instance The main plugin instance of {@link RealisticPlantGrowth}.
     */
    public BlockPistonListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    /**
     * Handles the {@link BlockPistonExtendEvent} to prevent plant destruction by pistons based on configuration settings.
     *
     * @param e The {@link BlockPistonExtendEvent} triggered by piston extension.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPistonDestroyPlant(BlockPistonExtendEvent e) {
        logger.verbose("BlockPistonExtendEvent");

        // Check if the target world is disabled in the configuration.
        if (instance.isWorldDisabled(e.getBlock().getWorld())) {
            return;
        }

        List<Block> pushedBlocks = e.getBlocks();

        // Check if requiring a hoe is enabled in the configuration.
        if (cm.isRequire_Hoe()) {
            for (Block pushedBlock : pushedBlocks) {
                // Check if the pushed block is an agricultural plant.
                if (instance.getVersionMapper().isAgriculturalPlant(pushedBlock)) {
                    pushedBlock.setType(Material.AIR);
                    logger.verbose("Cancelled item drops of plant at " + pushedBlock.getLocation() + ".");
                }
            }
        }
    }
}
