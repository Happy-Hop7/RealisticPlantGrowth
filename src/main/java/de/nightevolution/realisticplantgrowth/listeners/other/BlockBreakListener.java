package de.nightevolution.realisticplantgrowth.listeners.other;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.utils.plant.PlantKiller;
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
    private final String logFile = "PlayerInteractEvent";

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

            logger.logToFile("Player using a " + usedHOE.getType().name() + " to harvest.", logFile);
            logger.logToFile("require_hoe: " + cm.isRequire_Hoe(), logFile);
            logger.logToFile("destroy_farmland: " + cm.isDestroy_Farmland(), logFile);
            logger.logToFile("isSolid: " + b.getType().isSolid(), logFile);
            logger.logToFile("isAPlant: " + vm.isAPlant(b.getType()), logFile);

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
            logger.logToFile("Block drops cancelled: true", logFile);
            e.setDropItems(false);
        } else {
            scheduler.runTaskLater(instance, () -> {
                damageHoe(p, usedHoe);
            }, 1); // 1 Tick delay
        }
    }


    /**
     * Calculates and applies durability changes to the provided hoe used for harvesting.
     * This method simulates durability changes based on the Unbreaking enchantment.
     * If the hoe has the Unbreaking enchantment, there is a chance that the durability
     * will not decrease with each use. Additionally, if the hoe reaches its maximum durability,
     * it is removed from the player's inventory.
     *
     * @param player   The player who caused the BlockBreakEvent.
     * @param usedHoe  The hoe used to harvest the plant.
     */
    private void damageHoe(@NotNull Player player, @NotNull ItemStack usedHoe) {

        // Ensure the ItemStack has metadata that can be damaged
        Damageable hoe = (Damageable) usedHoe.getItemMeta();
        if (hoe == null) {
            logger.error("Damage attempt on hoe failed: Item has no damageable metadata.");
            return;
        }

        // TODO: Use VersionMapper for this distinction
        // Fetch the appropriate enchantment (UNBREAKING or DURABILITY for backward compatibility)
        // API Change from version 1.20.3 to 1.20.4.
        Enchantment unbreaking = Enchantment.getByName("UNBREAKING");
        if (unbreaking == null) {
            unbreaking = Enchantment.getByName("DURABILITY");
            logger.logToFile("Using legacy enchantment 'DURABILITY' due to API version < 1.20.4", logFile);
        }

        // Exit if enchantment retrieval fails
        if (unbreaking == null) {
            logger.error("Enchantment retrieval failed: Neither UNBREAKING nor DURABILITY enchantments found.");
            return;
        }

        // Logging the enchantment key, presence, and level on the hoe for debugging
        boolean hasUnbreaking = usedHoe.getEnchantments().containsKey(unbreaking);
        int unbreakingLevel = usedHoe.getEnchantmentLevel(unbreaking);

        logger.logToFile("Unbreaking Enchantment: " + unbreaking, logFile);
        logger.logToFile("Hoe has Unbreaking: " + hasUnbreaking, logFile);
        logger.logToFile("  - Unbreaking Level: " + unbreakingLevel, logFile);

        // Apply damage with Unbreaking logic
        if (hasUnbreaking) {
            if (Math.random() <= (1.0 / (unbreakingLevel + 1))) {
                hoe.setDamage(hoe.getDamage() + 1);
                usedHoe.setItemMeta(hoe);
                logger.logToFile("Hoe durability reduced (Unbreaking applied).", logFile);
                logger.logToFile("  - New Damage: " + hoe.getDamage(), logFile);
            } else {
                logger.logToFile("Hoe durability unchanged (Unbreaking prevented damage).", logFile);
            }
        } else {
            hoe.setDamage(hoe.getDamage() + 1);
            usedHoe.setItemMeta(hoe);
            logger.logToFile("Hoe durability reduced without Unbreaking enchantment.", logFile);
            logger.logToFile("  - New Damage: " + hoe.getDamage(), logFile);
        }

        // Check and handle maximum durability
        if (hoe.getDamage() >= usedHoe.getType().getMaxDurability()) {
            player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
            player.getInventory().remove(usedHoe);
            logger.logToFile("Hoe reached max durability and was removed from inventory.", logFile);
        } else {
            logger.logToFile("Hoe durability is within limits.", logFile);
            logger.logToFile("Current Damage: " + hoe.getDamage() +
                    " / Max Durability: " + usedHoe.getType().getMaxDurability(), logFile);
        }

        // Update player inventory after durability adjustments
        player.updateInventory();

    }


}
