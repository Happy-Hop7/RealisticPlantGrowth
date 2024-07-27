package de.nightevolution.listeners.player;

import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

/**
 * Listener for handling {@link PlayerQuitEvent} in the {@link RealisticPlantGrowth} plugin.
 * Clears cooldown data associated with a {@link Player} when they quit the server.
 */
public class PlayerQuitListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final boolean logEvent;

    /**
     * Constructs a new {@link PlayerQuitListener} instance.
     *
     * @param instance The main plugin instance of {@link RealisticPlantGrowth}.
     */
    public PlayerQuitListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);
        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
        scheduler = Bukkit.getScheduler();

        // Enable logging if debug mode is active and player logging is enabled
        this.logEvent = (RealisticPlantGrowth.isDebug() && instance.getConfigManager().isPlayer_log());
    }

    /**
     * Handles the {@link PlayerQuitEvent} by clearing cooldown data associated with the quitting {@link Player}.
     *
     * @param e A {@link PlayerQuitEvent} instance.
     */
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        UUID quittingPlayer = e.getPlayer().getUniqueId();

        if (logEvent) {
            logger.logToFile("", "PlayerInteractEvent");
            logger.logToFile("-------------------- Player Quit Event --------------------\"", "PlayerInteractEvent");
            logger.logToFile("  Clearing '" + e.getPlayer().getName() + "'s cooldown.", "PlayerInteractEvent");
        }

        scheduler.runTaskAsynchronously(instance, () -> {
            PlayerInteractListener.clearPlayerCooldownData(quittingPlayer);
        });
    }
}
