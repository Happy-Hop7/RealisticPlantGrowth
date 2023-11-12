package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;


public abstract class PlantGrowthListener  implements Listener{
    protected RealisticPlantGrowth instance;
    protected Logger logger;
    protected ConfigManager configManager;
    protected SpecialBlockSearch specialBlockSearch;
    protected Surrounding surrounding;

    // Block Data
    protected String coords;
    protected StringBuilder toLog;
    protected boolean isDark;
    protected int lightLevel;

    public PlantGrowthListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        logger.verbose("Abstract PlantGrowthListener Constructor.");

        instance.getServer().getPluginManager().registerEvents(this, instance);
        specialBlockSearch = SpecialBlockSearch.get();
        configManager = instance.getConfigManager();
        toLog = new StringBuilder(255);

    }

    protected void readBlockData(BlockEvent e){
        // Get coords of the event for logging
        coords = e.getBlock().getLocation().toString();

        if(configManager.isLog_coords())
            toLog.append(coords).append(": ");


    }
    protected void checkForSpecialBlocks(Block b){
        specialBlockSearch.surroundingOf(b);
    }


}