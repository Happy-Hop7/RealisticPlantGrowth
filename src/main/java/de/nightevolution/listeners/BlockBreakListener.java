package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * The BlockBreakListener class handles events related to block breaking,
 * particularly focusing on RealisticPlantGrowth mechanics.
 */
public class BlockBreakListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager configManager;
    private final Logger logger;

    /**
     * Constructs a new BlockBreakListener.
     *
     * @param instance The main plugin instance.
     */
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent (BlockBreakEvent e) {
        Block b = e.getBlock();
        logger.verbose("BlockBreakEvent!");

        if(RealisticPlantGrowth.isAPlant(b) && !(e.getPlayer().getGameMode() == GameMode.CREATIVE)){
            Player p = e.getPlayer();
            ItemStack usedHOE = p.getInventory().getItemInMainHand();
            logger.verbose("Player using a " + usedHOE.getType().name() + " to harvest.");

            // Require a hoe to harvest
            if(configManager.isRequire_hoe()) {
                // If not using a hoe: cancel DropItems and replace plant with air
                if(!usedHOE.getType().name().endsWith("_HOE")) {
                    logger.verbose("Block drops cancelled: true");
                    e.setDropItems(false);
                    b.setType(Material.AIR);
                }else{
                    //TODO: consider unbreaking enchantment in durability calculation
                    // Damage the usedHOE
                    Damageable hoe = (Damageable) usedHOE.getItemMeta();
                    assert hoe != null;
                    hoe.setDamage(hoe.getDamage()+1);
                    usedHOE.setItemMeta(hoe);
                    if(hoe.getDamage() >= usedHOE.getType().getMaxDurability()) {
                        p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                        p.getInventory().remove(usedHOE);
                    }
                    p.updateInventory();
                }
            }

            logger.verbose("destroy_farmland: " + configManager.isDestroy_farmland());
            logger.verbose("isSolid: " + !b.getType().isSolid());
            logger.verbose("isAPlant: " + RealisticPlantGrowth.isAPlant(b));

            // Destroy Farmland
            if (configManager.isDestroy_farmland() && !b.getType().isSolid()) {
                Block u = b.getRelative(BlockFace.DOWN);

                if (u.getType().equals(Material.FARMLAND)) {

                    // Schedule the replacement of farmland with coarse dirt with a 1-tick delay
                    BukkitScheduler scheduler = Bukkit.getScheduler();
                    scheduler.runTaskLater(instance, () ->{
                        logger.verbose("Replacing Farmland.");
                        u.setType(Material.COARSE_DIRT);
                    },1 ); // 1 Tick delay

                }
            }

        }

    }


}
