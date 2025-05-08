package de.nightevolution.realisticplantgrowth.utils;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.enums.MainConfigPath;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

/**
 * Handles integration with the bStats metrics system.
 * <p>
 * If metrics are enabled in the plugin configuration, this class initializes
 * the bStats Metrics system and registers custom charts to track plugin usage
 * statistics such as feature enablement.
 * </p>
 *
 * <p>
 * For more details, see the plugin's bStats page: <br>
 * <a href="https://bstats.org/plugin/bukkit/Realistic%20Plant%20Growth/20634">RealisticPlantGrowth's bStats Page</a>
 * </p>
 */
public class MetricsHandler {

    /** The plugin's unique bStats service ID. */
    private static final int SERVICE_ID = 20634;

    /** The Metrics instance from bStats. */
    private final Metrics metrics;

    /**
     * Constructs the MetricsHandler and initializes bStats if enabled in the config.
     *
     * @param plugin The main plugin instance.
     * @param configManager The configuration manager for accessing config values.
     */
    public MetricsHandler(RealisticPlantGrowth plugin, ConfigManager configManager) {
        Logger logger = LogUtils.getLogger(this.getClass());
        YamlDocument config = configManager.getConfig();

        // Check if metrics are enabled in the config
        if (config.getBoolean(MainConfigPath.USE_METRICS.getPath())) {
            logger.info("bStats enabled.");
            this.metrics = new Metrics(plugin, SERVICE_ID);

            registerCustomCharts(config);
        } else {
            logger.info("bStats disabled.");
            this.metrics = null;
        }
    }

    /**
     * Registers custom charts to provide insights about enabled features.
     *
     * @param config The plugin's configuration document.
     */
    private void registerCustomCharts(YamlDocument config) {
        // Update Checker status
        metrics.addCustomChart(new SimplePie("update_checker", () ->
                config.getBoolean(MainConfigPath.PLUGIN_UPDATES_CHECK_FOR_UPDATES.getPath()) ? "Enabled" : "Disabled"));

        // UV-Light feature status
        metrics.addCustomChart(new SimplePie("uv_light", () ->
                config.getBoolean(MainConfigPath.UV_LIGHT_ENABLED.getPath()) ? "Enabled" : "Disabled"));

        // Fertilizer feature status
        metrics.addCustomChart(new SimplePie("fertilizer", () ->
                config.getBoolean(MainConfigPath.FERTILIZER_ENABLED.getPath()) ? "Enabled" : "Disabled"));
    }
}
