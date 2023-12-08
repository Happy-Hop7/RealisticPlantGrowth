package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listens to player block interactions in order to give information
 * about crops growing rates in the current biome.
 */
public class PlayerInteractListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;


    public PlayerInteractListener(RealisticPlantGrowth instance){
        this.instance = instance;
        this.cm = instance.getConfigManager();

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e){
        logger.verbose("Interact-Event");
        World eventWorld = e.getPlayer().getWorld();

        // Check if the world is enabled for plant growth modification
        if (!instance.isWorldEnabled(eventWorld))
            return;

        logger.verbose("World is valid");
        if (!cm.isDisplay_growth_rates())
            return;

        logger.verbose("Display growth rates is activated.");
        /*if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK) || !(e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;
        */

        logger.verbose("CLICK_BLOCK Action.");
        Block eventBlock = e.getClickedBlock();
        if (!e.hasItem() || eventBlock == null){
            return;
        }

        logger.verbose("Item and block involved.");
        if (!instance.isClickableSeed(e.getMaterial())){
            return;
        }

        logger.verbose("Getting Block Data.");
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material blockMaterial = instance.getMaterialFromSeed(e.getMaterial());
        if(blockMaterial == null)
            return;

        eventBlockState.setType(blockMaterial);

        logger.verbose("Calculating Data.");
        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlockState, eventBlock.getWorld());


        double growthRate = surrounding.getGrowthRate();
        double deathChance = surrounding.getDeathChance();

        Player p = e.getPlayer();

        logger.verbose("growthRate: " + growthRate);
        logger.verbose("deathChance: " + deathChance);
        logger.verbose("Biome: " + surrounding.getBiome());

        p.sendMessage("growthRate: " + growthRate);
        p.sendMessage("deathChance: " + deathChance);
        p.sendMessage("Biome: " + surrounding.getBiome());

    }

}
