package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
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
     * Handles the BlockBreakEvent triggered when a block is broken.
     *
     * @param e BlockBreakEvent containing information about the event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent (BlockBreakEvent e) {
        Block b = e.getBlock();
        logger.verbose("BlockBreakEvent!");

        World world = e.getBlock().getWorld();
        if(configManager.getEnabled_worlds().contains(world.getName()))
            return;


        if(RealisticPlantGrowth.isAPlant(b) && !(e.getPlayer().getGameMode() == GameMode.CREATIVE)){
            Player p = e.getPlayer();
            ItemStack usedHOE = p.getInventory().getItemInMainHand();
            logger.verbose("Player using a " + usedHOE.getType().name() + " to harvest.");

            if(configManager.isRequire_hoe())
                requireHoeToHarvest(e, p, usedHOE);



            logger.verbose("destroy_farmland: " + configManager.isDestroy_farmland());
            logger.verbose("isSolid: " + !b.getType().isSolid());
            logger.verbose("isAPlant: " + RealisticPlantGrowth.isAPlant(b));

            // Destroy Farmland
            if (configManager.isDestroy_farmland() && !b.getType().isSolid()) {
                destroyFarmland(e);
            }

        }

    }

    /**
     * Handles the logic for requiring a hoe to harvest a plant.
     * If a hoe is required and not used, this method cancels item drops and replaces the plant with air.
     * If a hoe is used, it delegates to the 'damageHoe' method to simulate durability changes.
     *
     * @param e       The BlockBreakEvent containing information about the event.
     * @param p       The player who caused the BlockBreakEvent.
     * @param usedHoe The hoe used to harvest the plant.
     */
    private void requireHoeToHarvest(BlockBreakEvent e, Player p, ItemStack usedHoe){
        // If not using a hoe: cancel DropItems and replace plant with air
        if(!usedHoe.getType().name().endsWith("_HOE")) {
            logger.verbose("Block drops cancelled: true");
            e.setDropItems(false);
            e.getBlock().setType(Material.AIR);
        }else{
            damageHoe(p, usedHoe);
        }
    }


    /**
     * Destroys farmland below the broken block, replacing it with coarse dirt after a 1-tick delay.
     *
     * @param e BlockBreakEvent containing information about the event.
     */
    private void destroyFarmland(BlockBreakEvent e){
        Block u = e.getBlock().getRelative(BlockFace.DOWN);
        if (u.getType().equals(Material.FARMLAND)) {

            // Schedule the replacement of farmland with coarse dirt with a 1-tick delay
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTaskLater(instance, () ->{
                logger.verbose("Replacing Farmland.");
                u.setType(Material.COARSE_DIRT);
            },1 ); // 1 Tick delay
        }
    }

    /**
     * Calculates and applies durability changes to the provided hoe used for harvesting.
     * This method simulates durability changes based on enchantments like Unbreaking.
     * If the hoe has the Unbreaking enchantment, there is a chance that the durability
     * will not decrease with each use. Additionally, the method handles the removal of
     * the hoe if it reaches its maximum durability.
     *
     * @param p          The player who caused the BlockBreakEvent.
     * @param usedHoe   The hoe used to harvest the plant.
     */
    private void damageHoe(Player p, ItemStack usedHoe){

        Damageable hoe = (Damageable) usedHoe.getItemMeta();
        if(hoe == null)
            return;

        if (usedHoe.getEnchantments().containsKey(Enchantment.DURABILITY)) {
            if (Math.random() <= ((double) 1 / (usedHoe.getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
                hoe.setDamage(hoe.getDamage() + 1);
                usedHoe.setItemMeta(hoe);
            }
        }else{
            hoe.setDamage(hoe.getDamage() + 1);
            usedHoe.setItemMeta(hoe);
        }

        // Remove the hoe if it reaches maximum durability
        if(hoe.getDamage() >= usedHoe.getType().getMaxDurability()) {
            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
            p.getInventory().remove(usedHoe);
        }
        p.updateInventory();

    }


}
