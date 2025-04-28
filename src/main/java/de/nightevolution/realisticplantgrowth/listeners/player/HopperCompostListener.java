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
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.Material;

import java.util.Objects;

public class HopperCompostListener implements Listener {
    private final ConfigManager cm;
    private final Logger logger;
    private final boolean logEvent;
    private final String logFile = "InventoryMoveEvent";

    public HopperCompostListener(RealisticPlantGrowth instance) {
        this.cm = instance.getConfigManager();

        // Enable logging if debug mode is active and player logging is enabled
        this.logEvent = (RealisticPlantGrowth.isDebug() && cm.isPlayer_log());

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHopperCompostItem(InventoryMoveItemEvent e) {

        logEvent(e);

        // Ensure the event is from a hopper moving an item
        if (!(e.getSource().getType().name().equals("HOPPER"))) {
            return;
        }

        ItemStack item = e.getItem();
        if (item.getAmount() <= 0) {
            return;
        }

        Block composterBlock = Objects.requireNonNull(e.getDestination().getLocation()).getBlock();
        if (composterBlock.getType() != Material.COMPOSTER) {
            return;
        }

        // Check if the item is (Bonemeal or any other compostable item)
        if (!(item.getType() == Material.BONE_MEAL) || !cm.getAllowBonemealInComposters()) {
            return;
        }
        
        // Get the current composter state
        Levelled composterLevel = (Levelled) composterBlock.getBlockData();
        int currentLevel = composterLevel.getLevel();
        int maxLevel = composterLevel.getMaximumLevel() - 1;


        // If the composter was successfully filled, update the level
        if (currentLevel < maxLevel) {
            int newComposterLevel = currentLevel + 1;
            composterLevel.setLevel(newComposterLevel);

            // Update the composter block data
            composterBlock.setBlockData(composterLevel);


            // Play composter fill sound
            composterBlock.getWorld().playSound(
                    composterBlock.getLocation().add(0.5, 0.5, 0.5),
                    Sound.BLOCK_COMPOSTER_FILL_SUCCESS,
                    1.0f,
                    1.0f
            );

            // Spawn green happy particles
            composterBlock.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    composterBlock.getLocation().add(0.5, 0.1 + 0.15 * newComposterLevel, 0.5),
                    3,
                    0.15, 0.25, 0.15
            );

            // Remove the items from the hopper
            e.getItem().setAmount(item.getAmount() - 1);

            // Log or handle further actions if necessary
            if (logEvent) logger.logToFile("  Composter quick-filled by hopper: " + composterBlock.getLocation() + " to level " + newComposterLevel, logFile);
        }
    }

    private void logEvent(InventoryMoveItemEvent e) {
        if (logEvent) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Inventory Move Item Event --------------------", logFile);
            logger.logToFile("  Source: " + e.getSource().getType().name(), logFile);
            logger.logToFile("  Destination: " + e.getDestination().getType().name(), logFile);
            logger.logToFile("  Location: " + e.getDestination().getLocation(), logFile);
        }
    }
}
