package de.nightevolution.listeners.plant;

import de.nightevolution.ConfigManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.mapper.VersionMapper;
import de.nightevolution.utils.plant.PlantKiller;
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
import org.jetbrains.annotations.NotNull;

/**
 * The BlockBreakListener class handles events related to block breaking,
 * particularly focusing on RealisticPlantGrowth mechanics.
 */
public class BlockBreakListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final VersionMapper vm;
    private final Logger logger;
    private final BukkitScheduler scheduler;

    /**
     * Constructs a new BlockBreakListener.
     *
     * @param instance The main plugin instance.
     */
    public BlockBreakListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.vm = instance.getVersionMapper();
        this.cm = instance.getConfigManager();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        this.scheduler = Bukkit.getScheduler();

        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }


    /**
     * Handles the BlockBreakEvent triggered when a block is broken.
     *
     * @param e BlockBreakEvent containing information about the event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent(@NotNull BlockBreakEvent e) {
        Block b = e.getBlock();
        World world = b.getWorld();

        if (instance.isWorldDisabled(world)) {
            return;
        }

        // basically every crop planted on farmland + nether warts
        if (vm.isAgriculturalPlant(b) && !(e.getPlayer().getGameMode() == GameMode.CREATIVE)) {
            Player p = e.getPlayer();
            ItemStack usedHOE = p.getInventory().getItemInMainHand();

            logger.verbose("Player using a " + usedHOE.getType().name() + " to harvest.");
            logger.verbose("require_hoe: " + cm.isRequire_Hoe());
            logger.verbose("destroy_farmland: " + cm.isDestroy_Farmland());
            logger.verbose("isSolid: " + b.getType().isSolid());
            logger.verbose("isAPlant: " + vm.isAPlant(b.getType()));

            if (cm.isRequire_Hoe()) {
                requireHoeToHarvest(e, p, usedHOE);
            }

            // Destroy Farmland
            if (cm.isDestroy_Farmland() && !b.getType().isSolid()) {
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
    private void requireHoeToHarvest(@NotNull BlockBreakEvent e, @NotNull Player p, @NotNull ItemStack usedHoe) {
        // If not using a hoe: cancel DropItems

        if (!usedHoe.getType().name().endsWith("_HOE")) {
            logger.verbose("Block drops cancelled: true");
            e.setDropItems(false);
        } else {
            scheduler.runTaskLater(instance, () -> {
                damageHoe(p, usedHoe);
            }, 1); // 1 Tick delay
        }
    }


    /**
     * Calculates and applies durability changes to the provided hoe used for harvesting.
     * This method simulates durability changes based on enchantments like Unbreaking.
     * If the hoe has the Unbreaking enchantment, there is a chance that the durability
     * will not decrease with each use. Additionally, the method handles the removal of
     * the hoe if it reaches its maximum durability.
     *
     * @param p       The player who caused the BlockBreakEvent.
     * @param usedHoe The hoe used to harvest the plant.
     */
    private void damageHoe(@NotNull Player p, @NotNull ItemStack usedHoe) {

        Damageable hoe = (Damageable) usedHoe.getItemMeta();
        if (hoe == null)
            return;

        if (usedHoe.getEnchantments().containsKey(Enchantment.DURABILITY)) {
            if (Math.random() <= ((double) 1 / (usedHoe.getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
                hoe.setDamage(hoe.getDamage() + 1);
                usedHoe.setItemMeta(hoe);
            }
        } else {
            hoe.setDamage(hoe.getDamage() + 1);
            usedHoe.setItemMeta(hoe);
        }

        // Remove the hoe if it reaches maximum durability
        if (hoe.getDamage() >= usedHoe.getType().getMaxDurability()) {
            p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
            p.getInventory().remove(usedHoe);
        }
        p.updateInventory();

    }


}
