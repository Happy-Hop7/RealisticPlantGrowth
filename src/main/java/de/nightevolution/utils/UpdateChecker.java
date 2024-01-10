package de.nightevolution.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Utility class for checking updates of a Bukkit/Spigot plugin on SpigotMC.
 * This class uses the SpigotMC API to check for updates based on the provided resource ID.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    /**
     * Constructs an {@link UpdateChecker} instance for a given plugin and resource ID.
     *
     * @param plugin     The JavaPlugin instance representing the Bukkit/Spigot plugin.
     * @param resourceId The SpigotMC resource ID of the plugin to check for updates.
     */
    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    /**
     * Asynchronously fetches the latest version information from SpigotMC.
     * Executes the provided consumer with the latest version string if available.
     *
     * @param consumer The consumer to accept the latest version string.
     */
    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" +
                    this.resourceId).toURL().openStream(); Scanner scann = new Scanner(inputStream)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException | URISyntaxException e) {
                // Log an error message if unable to check for updates
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}

