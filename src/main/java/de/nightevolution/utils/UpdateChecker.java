package de.nightevolution.utils;

import com.google.gson.Gson;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.rest.ModrinthVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


/**
 * The UpdateChecker class is responsible for checking updates for the {@link RealisticPlantGrowth} plugin
 * using the Modrinth API.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final Logger logger;


    /**
     * Constructs a new {@link UpdateChecker} instance.
     */
    public UpdateChecker() {
        this.plugin = RealisticPlantGrowth.getInstance();
        this.logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
    }


    /**
     * Asynchronously fetches the latest version information from the Modrinth API and
     * executes the provided consumer with the retrieved {@link ModrinthVersion}.
     *
     * @param consumer The consumer to accept the {@link ModrinthVersion}.
     */
    public void getVersion(final Consumer<ModrinthVersion> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://api.modrinth.com/v2/project/realistic-plant-growth/version"))
                        .build();

                HttpResponse<String> httpsGetResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
                int responseCode = httpsGetResponse.statusCode();

                logger.verbose("Modrinth API response code: " + responseCode);
                logger.verbose("Modrinth API response body: " + httpsGetResponse.body());

                if (responseCode == 200) {
                    consumer.accept(getVersion(httpsGetResponse));
                } else
                    throw new Exception("Error response code: " + responseCode);


            } catch (Exception e) {
                logger.error("Failed to check Modrinth API for RealisticPlantGrowth updates!");
                logger.error("Error details: " + e.getMessage());
            }
        });
    }

    /**
     * Parses the Modrinth API response to extract the latest version.
     *
     * @param getResponse The HTTP response containing version information.
     * @return The latest {@link ModrinthVersion}.
     */
    private ModrinthVersion getVersion(HttpResponse<String> getResponse) {
        Gson gson = new Gson();
        ModrinthVersion[] versionArray = gson.fromJson(getResponse.body(), ModrinthVersion[].class);

        List<ModrinthVersion> versionList = Arrays.stream(versionArray).toList();
        return Collections.max(versionList);
    }

}


