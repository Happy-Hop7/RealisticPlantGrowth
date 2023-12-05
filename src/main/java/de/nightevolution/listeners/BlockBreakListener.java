package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.PlantKiller;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    private final BukkitScheduler scheduler;

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
        scheduler = Bukkit.getScheduler();
    }


    /**
     * Handles the BlockBreakEvent triggered when a block is broken.
     *
     * @param e BlockBreakEvent containing information about the event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent (BlockBreakEvent e) {
        Block b = e.getBlock();
        World world = b.getWorld();

        if(!configManager.getEnabled_worlds().contains(world.getName())){
            return;
        }

        // basically every crop planted on farmland + nether warts
        if(instance.isAgriculturalPlant(b) && !(e.getPlayer().getGameMode() == GameMode.CREATIVE)){
            Player p = e.getPlayer();
            ItemStack usedHOE = p.getInventory().getItemInMainHand();

            logger.verbose("Player using a " + usedHOE.getType().name() + " to harvest.");
            logger.verbose("require_hoe: " + configManager.isRequire_Hoe());
            logger.verbose("destroy_farmland: " + configManager.isDestroy_Farmland());
            logger.verbose("isSolid: " + b.getType().isSolid());
            logger.verbose("isAPlant: " + instance.isAPlant(b));

            if(configManager.isRequire_Hoe()) {
                requireHoeToHarvest(e, p, usedHOE);
            }

            // Destroy Farmland
            if (configManager.isDestroy_Farmland() && !b.getType().isSolid()) {
                new PlantKiller().destroyFarmland(e.getBlock());
            }

        }

    }

    /**
     * Handles the logic for requiring a hoe to harvest a plant.
     * If a hoe is required and not used, this method cancels item drops.
     * If a hoe is used, it delegates to the 'damageHoe' method to simulate durability changes.
     *
     * @param e       The BlockBreakEvent containing information about the event.
     * @param p       The player who caused the BlockBreakEvent.
     * @param usedHoe The hoe used to harvest the plant.
     */
    private void requireHoeToHarvest(BlockBreakEvent e, Player p, ItemStack usedHoe){
        // If not using a hoe: cancel DropItems

        if(!usedHoe.getType().name().endsWith("_HOE")) {
            logger.verbose("Block drops cancelled: true");
            e.setDropItems(false);
        }else{
            scheduler.runTaskLater(instance, () -> {
                damageHoe(p, usedHoe);
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
