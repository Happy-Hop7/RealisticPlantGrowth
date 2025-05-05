package de.nightevolution.realisticplantgrowth.utils;

import com.google.gson.Gson;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.rest.ModrinthVersion;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;


/**
 * The UpdateChecker class is responsible for checking updates for the {@link RealisticPlantGrowth} plugin
 * using the Modrinth API.
 */
public class UpdateChecker {

    private final RealisticPlantGrowth plugin;
    private final Logger logger;
    private final int updateInterval;

    private int scheduledTaskId = -1;


    /**
     * Constructs a new {@link UpdateChecker} instance.
     */
    public UpdateChecker(int updateInterval) {
        this.plugin = RealisticPlantGrowth.getInstance();
        this.logger = LogUtils.getLogger(this.getClass());
        this.updateInterval = updateInterval;
        boolean isAutoUpdate = updateInterval >= 1;

        // Immediate check
        checkForUpdates();

        // Scheduled checks
        if (isAutoUpdate) {
            logger.info("Automatic update checks are enabled.");
            scheduleAutomaticUpdateChecks();
        }

    }

    private void checkForUpdates() {
        getVersion(version -> {
            if (version == null) return;

            ModrinthVersion thisPluginVersion = new ModrinthVersion();
            thisPluginVersion.setVersion_number(plugin.getDescription().getVersion());

            if (thisPluginVersion.compareTo(version) >= 0) {
                logger.info("Your RealisticPlantGrowth plugin is up to date (version {}).", thisPluginVersion.getVersion_number());
            } else {
                logger.warn("A new version of RealisticPlantGrowth is available!");
                logger.warn("Current version: {}", thisPluginVersion.getVersion_number());
                logger.warn("Latest version: {}", version.getVersion_number());
                logger.warn("Download the latest version at:");
                logger.warn("https://modrinth.com/plugin/realistic-plant-growth/version/latest");
            }
        });
    }


    /**
     * Schedules automatic update checks every {@code updateInterval} hours if enabled.
     */
    public void scheduleAutomaticUpdateChecks() {
        // Convert hours to ticks: 1 hour = 60 * 60 * 20 = 72,000 ticks
        long intervalTicks = updateInterval * 60L * 60L * 20L;

        scheduledTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
            logger.info("Running scheduled update check...");
            checkForUpdates();
        }, intervalTicks, intervalTicks).getTaskId();
    }


    /**
     * Asynchronously fetches the latest version information from the Modrinth API and
     * executes the provided consumer with the retrieved {@link ModrinthVersion}.
     *
     * @param consumer The consumer to accept the {@link ModrinthVersion}.
     */
    public void getVersion(final Consumer<ModrinthVersion> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://api.modrinth.com/v2/project/realistic-plant-growth/version"))
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> httpsGetResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
                int responseCode = httpsGetResponse.statusCode();

                LogUtils.debug(logger, "Modrinth API response code: " + responseCode);
                LogUtils.debug(logger, "Modrinth API response body: " + httpsGetResponse.body());

                if (responseCode == 200) {
                    consumer.accept(getVersion(httpsGetResponse));
                } else
                    throw new Exception("Error response code: " + responseCode);


            } catch (Exception e) {
                LogUtils.error(logger,"Failed to check Modrinth API for RealisticPlantGrowth updates!");
                LogUtils.error(logger,"Error details: ", e);
            }
        });

    }

    /**
     * Parses the Modrinth API response to extract the latest version.
     *
     * @param getResponse The HTTP response containing version information.
     * @return The latest {@link ModrinthVersion}, or null if parsing fails.
     */
    @Nullable
    private ModrinthVersion getVersion(HttpResponse<String> getResponse) {
        Gson gson = new Gson();
        ModrinthVersion[] versionArray = gson.fromJson(getResponse.body(), ModrinthVersion[].class);

        if (versionArray == null || versionArray.length == 0) {
            LogUtils.error(logger, "Modrinth API response contained no usable version data.");
            return null;
        }

        return Collections.max(Arrays.asList(versionArray));
    }

    public void cancelScheduledTask() {
        if (scheduledTaskId != -1) {
            Bukkit.getScheduler().cancelTask(scheduledTaskId);
            LogUtils.debug(logger, "Cancelled scheduled update check task.");
            scheduledTaskId = -1;
        }
    }



}


