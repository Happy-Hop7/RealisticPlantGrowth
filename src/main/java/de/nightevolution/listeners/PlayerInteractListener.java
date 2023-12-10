package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Listens to player block interactions in order to give information
 * about crops growing rates in the current biome.
 */
public class PlayerInteractListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;
    private static final HashMap<UUID, Long> playerCooldownMap = new HashMap<>();


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

        if (!cm.isDisplay_growth_rates())
            return;

        // Check if the world is enabled for plant growth modification
        World eventWorld = e.getPlayer().getWorld();
        if (instance.isWorldDisabled(eventWorld))
            return;

        if (!instance.isClickableSeed(e.getMaterial())){
            return;
        }

        if ((e.getAction() != Action.LEFT_CLICK_BLOCK))
            return;


        Block clickedBlock = e.getClickedBlock();
        if (!e.hasItem() || clickedBlock == null || instance.isAPlant(clickedBlock)){
            return;
        }

        logger.verbose("All checks passed.");
        logger.verbose("Getting Block Data...");

        // Using block above the clicked Block to avoid a SkyLight-Level of zero.
        Block eventBlock = clickedBlock.getRelative(BlockFace.UP);

        // Using a BlockState to "change" the Block type without actually changing the Block type in the world.
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material blockMaterial = instance.getMaterialFromSeed(e.getMaterial());

        // getMaterialFromSeed is nullable.
        if(blockMaterial == null)
            return;

        // Adding a cooldown to stop click spamming which prevents unnecessary heavy area calculations.
        Long lastTime = playerCooldownMap.get(e.getPlayer().getUniqueId());
        long currentTime = System.currentTimeMillis();

        // cooldown in milliseconds
        int cooldown = (cm.getDisplay_cooldown() * 1000);
        if (lastTime != null) {
            logger.verbose("lastTime: " + lastTime);
            logger.verbose("currentTime: " + currentTime);
            logger.verbose("cooldown: " + cooldown);
            if((currentTime - lastTime) < cooldown) {
                logger.verbose("PlayerInteractEvent-Cooldown.");
                return;
            }

        }

        playerCooldownMap.put(e.getPlayer().getUniqueId(), currentTime);

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            e.setCancelled(true);

        if (!instance.isGrowthModifiedPlant(blockMaterial)){
            logger.verbose("Vanilla behavior for: " + blockMaterial);
            e.getPlayer().sendMessage("Vanilla behavior for: " + blockMaterial);
            return;
        }



        eventBlockState.setType(blockMaterial);

        logger.verbose("Calculating Data.");
        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlock, eventBlockState);


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

    public static void clearPlayerCooldownData(UUID uuid){
        playerCooldownMap.remove(uuid);
    }

}
