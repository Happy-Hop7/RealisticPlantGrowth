package de.nightevolution.realisticplantgrowth.listeners.player;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.util.Objects;

/**
 * Handles the interaction between hoppers and composters.
 * Allows bonemeal to be input to composters via hoppers, and optionally prevents bonemeal extraction.
 */
public class HopperCompostListener implements Listener {

    private final ConfigManager cm;
    private final Logger logger;
    private final boolean logEvent;
    private static final String LOG_FILE = "BonemealLog";

    /**
     * Registers the hopper-composter event listener.
     *
     * @param instance Main plugin instance
     */
    public HopperCompostListener(RealisticPlantGrowth instance) {
        this.cm = instance.getConfigManager();
        this.logEvent = RealisticPlantGrowth.isDebug() && cm.isBonemeal_log();

        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    /**
     * Called when items move between inventories (e.g. hopper to composter).
     *
     * @param e Inventory move event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHopperComposterInteraction(InventoryMoveItemEvent e) {
        if (logEvent) logEventDetails(e);

        final ItemStack item = e.getItem();
        if (item.getAmount() <= 0) return;

        final InventoryType sourceType = e.getSource().getType();
        final InventoryType destinationType = e.getDestination().getType();

        if (sourceType == InventoryType.HOPPER && destinationType == InventoryType.COMPOSTER && cm.isComposterBonemealInputAllowed()) {
            handleBonemealInput(e, item);
        } else if (sourceType == InventoryType.COMPOSTER && destinationType == InventoryType.HOPPER && cm.isComposterBonemealOutputDisabled()) {
            handleBonemealOutput(e);
        }
    }

    /**
     * Handles the event where bonemeal is fed into a composter by a hopper.
     *
     * @param e    The inventory move event
     * @param item The item being transferred
     */
    private void handleBonemealInput(InventoryMoveItemEvent e, ItemStack item) {
        final Block composterBlock = Objects.requireNonNull(e.getDestination().getLocation()).getBlock();
        final Levelled composterData = (Levelled) composterBlock.getBlockData();
        final int currentLevel = composterData.getLevel();
        final int maxLevel = composterData.getMaximumLevel() - 1;

        if (currentLevel >= maxLevel) return;

        final int newLevel = currentLevel + 1;
        composterData.setLevel(newLevel);
        composterBlock.setBlockData(composterData, true);

        // Play sound and spawn particles
        composterBlock.getWorld().playSound(
                composterBlock.getLocation().add(0.5, 0.5, 0.5),
                Sound.BLOCK_COMPOSTER_FILL_SUCCESS, 1.0f, 1.0f
        );

        composterBlock.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                composterBlock.getLocation().add(0.5, 0.1 + 0.15 * newLevel, 0.5),
                3, 0.15, 0.25, 0.15
        );

        // Decrease item count
        item.setAmount(item.getAmount() - 1);

        // Logging
        if (logEvent) {
            logger.logToFile(String.format("  Composter accepted bonemeal at %s, level increased to %d",
                    composterBlock.getLocation(), newLevel), LOG_FILE);
        }
    }

    /**
     * Cancels extraction of bonemeal from a composter.
     *
     * @param e Inventory move event
     */
    private void handleBonemealOutput(InventoryMoveItemEvent e) {
        e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  Cancelled bonemeal extraction from composter.", LOG_FILE);
        }
    }

    /**
     * Logs the inventory move event details if logging is enabled.
     *
     * @param e Inventory move event
     */
    private void logEventDetails(InventoryMoveItemEvent e) {
        logger.logToFile("", LOG_FILE);
        logger.logToFile("---------------- Inventory Move Item Event ----------------", LOG_FILE);
        logger.logToFile("  Source: " + e.getSource().getType().name(), LOG_FILE);
        logger.logToFile("  Destination: " + e.getDestination().getType().name(), LOG_FILE);
        logger.logToFile("  Location: " + e.getDestination().getLocation(), LOG_FILE);
    }
}
