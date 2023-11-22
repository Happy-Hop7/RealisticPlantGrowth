package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;


public abstract class PlantGrowthListener  implements Listener{
    protected RealisticPlantGrowth instance;
    protected Logger logger;
    protected String logString = "";
    protected ConfigManager configManager;
    protected SpecialBlockSearch specialBlockSearch;
    protected Surrounding surrounding;

    // Block Data
    protected String coords;
    protected Block eventBlock;
    protected World eventWorld;


    public PlantGrowthListener(RealisticPlantGrowth instance) {
        this.instance = instance;

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());

        instance.getServer().getPluginManager().registerEvents(this, instance);
        specialBlockSearch = SpecialBlockSearch.get();
        configManager = instance.getConfigManager();

    }

    protected boolean initEventData(BlockEvent e){
        // Get coords of the event for logging
        eventBlock = e.getBlock();
        eventWorld = eventBlock.getWorld();

        if(!isWorldEnabled(eventWorld))
            return false;

        if(instance.isAPlant(eventBlock) || instance.isAnAquaticPlant(eventBlock)) {

            if (configManager.isLog_Coords()) {
                coords = "[ " +
                        eventBlock.getLocation().getBlockX() + ", " +
                        eventBlock.getLocation().getBlockY() + ", " +
                        eventBlock.getLocation().getBlockZ() + "] ";
                logString += coords;
            }

            surrounding = specialBlockSearch.surroundingOf(eventBlock);

            return true;
        }
        return false;
    }

    protected boolean isWorldEnabled(World world){
        return(configManager.getEnabled_worlds().contains(world.getName()));
    }

    protected void checkForSpecialBlocks(Block b){
        specialBlockSearch.surroundingOf(b);
    }

}
