package de.nightevolution.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" +
                    this.resourceId + "/~").toURL().openStream(); Scanner scann = new Scanner(inputStream)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException | URISyntaxException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}

