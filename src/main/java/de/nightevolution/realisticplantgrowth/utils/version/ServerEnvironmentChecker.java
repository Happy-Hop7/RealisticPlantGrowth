package de.nightevolution.realisticplantgrowth.utils.version;

import de.nightevolution.realisticplantgrowth.utils.LogUtils;
import de.nightevolution.realisticplantgrowth.utils.version.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.utils.version.versions.*;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class ServerEnvironmentChecker {

    private final String pluginVersion;
    private final Logger logger;

    private VersionMapper versionMapper;

    public ServerEnvironmentChecker(String pluginVersion) {
        this.pluginVersion = pluginVersion;
        this.logger = LogUtils.getLogger(this.getClass());
    }

    /**
     * Checks the server implementation to determine if it is running on a Paper or Spigot server.
     * <p>
     * This method attempts to load a Paper-specific class. If the class is found, it indicates that
     * the server is a Paper fork, and the corresponding flag is set. If the class cannot be found,
     * it assumes the server is running on Spigot or another non-Paper implementation.
     * </p>
     */
    public boolean checkFork() {
        try {
            logger.info("Checking server implementation...");
            // Attempt to load a Paper-specific class to verify if running on a Paper fork
            Class.forName("io.papermc.paper.util.Tick");
            logger.info("... using Paper implementation.");
            return true;

        } catch (ClassNotFoundException ignored) {
            logger.error("Unsupported server: Paper or compatible fork required during BETA.");
            return false;
        }
    }

    /**
     * Checks the server version and returns the appropriate {@link VersionMapper}.
     * <p>
     * This method determines the server version by extracting it from the Bukkit server class package name.
     * It then sets the corresponding version mapper based on the extracted version.
     *
     * @return Subclass of {@link  VersionMapper} if the version check and initialization are successful, {@code null} otherwise.
     */
    @Nullable
    public VersionMapper checkVersion() {

        int minorReleaseVersion;
        int microReleaseVersion;

        logger.info("Checking server version...");

        try {

            String[] versionString = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            minorReleaseVersion = Integer.parseInt(versionString[1]);

            if (versionString.length >= 3) {
                microReleaseVersion = Integer.parseInt(versionString[2]);
            } else {
                microReleaseVersion = 0;
            }

            logger.info("Your server is running version 1.{}.{}", minorReleaseVersion, microReleaseVersion);

        } catch (ArrayIndexOutOfBoundsException | NumberFormatException whatVersionAreYouUsingException) {
            LogUtils.error(logger, "Error extracting server version: Unable to parse Bukkit version format.");
            return null;
        }

        // Warn if the server version is a snapshot version
        if (pluginVersion.contains("SNAPSHOT")) {
            logger.warn("You are using a snapshot version of RealisticPlantGrowth!");
        }

        // Version below Minecraft 1.20.1 are not supported (due to createBlockState API change).
        if (minorReleaseVersion < 20 || (minorReleaseVersion == 20 && microReleaseVersion == 0)) {
            logger.error("Unsupported server version: This plugin requires Minecraft 1.20.1 or higher.");
            return null;
        }

        // Assign the correct VersionMapper based on the server version
        if (minorReleaseVersion == 20 && microReleaseVersion <= 3) {
            logger.info("Implementation initialized for Minecraft 1.20.1 - 1.20.3.");
            return new Version_1_20();
        }

        // Version 1.20.4 - 1.21.3
        if (minorReleaseVersion <= 21 && microReleaseVersion <= 3) {
            logger.info("Implementation initialized for Minecraft 1.20.4 - 1.21.3.");
            return new Version_1_20_4();
        }

        // Version >= 1.21.4
        else {
            logger.info("Implementation initialized for Minecraft 1.21.4 and above.");
            return new Version_1_21_4();
        }
    }
}
