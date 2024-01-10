package de.nightevolution.utils;

import com.google.gson.Gson;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.rest.ModrinthAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

/**
 * Utility class for checking updates of a Bukkit/Spigot plugin on SpigotMC.
 * This class uses the SpigotMC API to check for updates based on the provided resource ID.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final Logger logger;


    /**
     * Constructs an {@link UpdateChecker} instance for a given plugin and resource ID.
     */
    public UpdateChecker() {
        this.plugin = RealisticPlantGrowth.getInstance();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }

    /**
     * Asynchronously fetches the latest version information from SpigotMC.
     * Executes the provided consumer with the latest version string if available.
     *
     * @param consumer The consumer to accept the latest version string.
     */
    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            try (HttpClient client = HttpClient.newHttpClient()) {

                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://api.modrinth.com/v2/project/realistic-plant-growth/version"))
                        .build();

                HttpResponse<String> httpsGetResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

                logger.verbose(httpsGetResponse.body());

                String version = getVersion(httpsGetResponse);
                consumer.accept(version);

            } catch (Exception e) {
                logger.error("Couldn't check Modrinth-API for RealisticPlantGrowth updates!");
                e.printStackTrace();
            }
        });
    }

    private String getVersion(HttpResponse<String> getResponse) {
        Gson gson = new Gson();
        ModrinthAPI[] versionArray = gson.fromJson(getResponse.body(), ModrinthAPI[].class);
        return versionArray[0].getVersion_number();
    }

}


