package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.listeners.*;
import de.nightevolution.utils.BiomeChecker;
import de.nightevolution.utils.Logger;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The main class for the {@link RealisticPlantGrowth} plugin.
 * This class serves as the entry point for the plugin and handles initialization, configuration, and event listening.
 */
public final class RealisticPlantGrowth extends JavaPlugin {

    /**
     * The main class for the {@link RealisticPlantGrowth} plugin, serving as a singleton instance.
     */
    private static RealisticPlantGrowth instance;

    /**
     * The name of the debug log file used by the {@link RealisticPlantGrowth} plugin.
     */
    private static final String logFile = "debug";

    /**
     * A flag indicating whether verbose logging is enabled in the {@link RealisticPlantGrowth} plugin.
     */
    private static boolean verbose = false;

    /**
     * A flag indicating whether debug mode is enabled in the {@link RealisticPlantGrowth} plugin.
     */
    private static boolean debug = false;

    /**
     * The {@link ConfigManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private static ConfigManager cm;

    /**
     * The {@link CommandManager} used by the {@link RealisticPlantGrowth} plugin.
     */
    private CommandManager cmdManager;

    /**
     * The {@link BukkitAudiences} instance for managing Adventure API interactions in the {@link RealisticPlantGrowth} plugin.
     */
    private BukkitAudiences bukkitAudiences;

    /**
     * The {@link Logger} instance for recording log messages in the {@link RealisticPlantGrowth} plugin.
     */
    private Logger logger;

    /**
     * The {@link Metrics} instance for collecting anonymous usage data for the {@link RealisticPlantGrowth} plugin.
     */
    private Metrics metrics;


    /**
     * A set of plant materials used for the 'require_hoe_to_harvest' setting in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material}s represent agricultural plants that require a hoe to be harvested.
     */
    private static final Set<Material> agriculturalPlants = new HashSet<>(Arrays.asList(
            Material.ATTACHED_MELON_STEM,
            Material.ATTACHED_PUMPKIN_STEM,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.WHEAT
    ));

    /**
     * A set of all supported land plants in the {@link RealisticPlantGrowth} plugin.
     * This set includes various plant {@link Material}s found on land.
     * Saplings are added later to this set.
     */
    private static final Set<Material> plants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.BROWN_MUSHROOM,
            Material.BEETROOTS,
            Material.CACTUS,
            Material.CARROTS,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.COCOA,
            Material.CRIMSON_FUNGUS,
            Material.GLOW_LICHEN,
            Material.SHORT_GRASS,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.RED_MUSHROOM,
            Material.SUGAR_CANE,
            Material.SWEET_BERRY_BUSH,
            Material.TALL_GRASS,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT,
            Material.VINE,
            Material.WARPED_FUNGUS,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT,
            Material.WHEAT
    ));

    /**
     * A set of all supported aquatic plants in the {@link RealisticPlantGrowth} plugin.
     * These {@link Material} represent plant {@link Block}s typically found in aquatic environments.
     */
    private static final Set<Material> aquaticPlants = new HashSet<>(Arrays.asList(
            Material.KELP,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    private static final Set<Material> upwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.KELP,
            Material.KELP_PLANT,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT
    ));

    private static final Set<Material> downwardsGrowingPlants = new HashSet<>(Arrays.asList(
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT
    ));

    private static final Set<Material> growEventReturnsAirBlockPlants = new HashSet<>(Arrays.asList(
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.SUGAR_CANE,
            Material.CACTUS
    ));
    private static Set<Material> saplings;

    /**
     * Set of materials representing plants with growth modifications.
     */
    private static HashSet<Material> growthModifiedPlants;

    /**
     * Mapping of clickable seeds to their corresponding plant materials.
     * Key: Clickable Seed ({@link Material}) , Value: Plant {@link Material}
     */
    private static HashMap<Material, Material> clickableSeedsMap;

    /**
     * Set of materials representing clickable seeds.
     */
    private static HashSet<Material> clickableSeeds;

    @Override
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        updateVariables();
        registerMetrics();
        drawLogo();
    }

    /**
     * Registers the primary command executor for the {@link RealisticPlantGrowth} plugin.
     * Associates the "{@code /rpg}" command with the corresponding {@link CommandManager}.
     */
    private void registerCommands() {
        Objects.requireNonNull(instance.getCommand("rpg")).setExecutor(cmdManager);
    }

    /**
     * Registers the tab completer for the {@link RealisticPlantGrowth} plugin's primary command.
     * Associates the "{@code /rpg}" command with the provided {@link TabCompleterImpl} implementation.
     */
    private void registerTabCompleter() {
        Objects.requireNonNull(instance.getCommand("rpg")).setTabCompleter(new TabCompleterImpl());
    }

    /**
     * Registers various event-listeners to handle plant growth, structure growth, block spread,
     * fertilization, block-breaking, player interactions, and player quit events.
     * Each listener is associated with the provided instance of the {@link RealisticPlantGrowth} plugin.
     */
    private void registerListeners() {
        new BlockGrowListener(instance);
        new StructureGrowListener(instance);
        new BlockSpreadListener(instance);
        new BlockFertilizeListener(instance);
        new BlockBreakListener(instance);
        new PlayerInteractListener(instance);
        new PlayerQuitListener(instance);
    }

    /**
     * Registers metrics using bStats if the corresponding configuration setting is enabled.
     * <p> bStats is a service that provides statistics and insights about plugin usage. </p>
     * <p> For more information about bStats, visit the bStats page:
     * <a href="https://bstats.org/plugin/bukkit/Realistic%20Plant%20Growth/20634">bStats Page</a>
     * </p>
     */
    private void registerMetrics() {
        // Check if the use_metrics configuration setting is enabled
        if (cm.use_metrics()) {
            // Log that bStats is enabled
            logger.log("bStats enabled.");

            // Create a new Metrics instance with the plugin and the unique plugin ID (20634)
            metrics = new Metrics(this, 20634);
        } else
            logger.log("bStats disabled.");
    }

    /**
     * Reloads the plugin by refreshing YAML files, unregistering event handlers, and updating variables.
     * This method is intended for use when reloading plugin configurations or making runtime adjustments.
     */
    public void reload() {
        cm.reloadAllYAMLFiles();
        HandlerList.unregisterAll(instance);
        updateVariables();
    }

    /**
     * Updates the plugin variables and configurations based on the latest settings.
     * This method refreshes the {@link ConfigManager}, {@link MessageManager}, logging settings,
     * and various cached data used by the plugin.
     */
    public void updateVariables() {
        cm = ConfigManager.get();

        verbose = cm.isVerbose();
        debug = cm.isDebug_log();

        logger = new Logger(this.getClass().getSimpleName(), verbose, debug);

        cmdManager = new CommandManager();

        if (this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }

        // Initialize an audiences instance for the plugin
        this.bukkitAudiences = BukkitAudiences.create(this);

        getSaplingsTag();
        updateGrowthModifiedPlants();
        updateClickableSeeds();
        registerCommands();
        registerTabCompleter();
        BiomeChecker.clearCache();
        registerListeners();

    }

    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the {@link Bukkit} {@link PluginManager}.
     */
    void disablePlugin() {
        logger.log("");
        logger.error("&cDisabling " + this.getClass().getSimpleName() + "...");
        logger.log("");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }
    }


    /**
     * Adds all saplings to the seeds and plants list.
     * Saplings are chosen by vanilla {@code saplings} tag.
     * AZALEA and FLOWERING_AZALEA are also included.
     */
    private void getSaplingsTag() {
        logger.verbose("Getting saplings tag...");
        Set<Material> saplingSet = (Tag.SAPLINGS.getValues());

        plants.addAll(saplingSet);
        saplings = saplingSet;

        if (verbose) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- Saplings ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material material : saplings) {
                    logger.logToFile("  - " + material, logFile);
                }


                logger.logToFile("", logFile);
                logger.logToFile("---------------------- All Plants ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material material : plants) {
                    logger.logToFile("  - " + material, logFile);
                }
            }, 20);
        }
    }

    /**
     * Identifies and collects plants with modified growth behavior from the root entries of GrowthModifiers.
     * Each root entry corresponds to a plant with modified growth characteristics.
     * The method extracts these plants and adds them to the collection of modified growth rate plants.
     */
    private void updateGrowthModifiedPlants() {
        Map<String, Object> growthModData = cm.getGrowthModifiers();
        Set<String> keys = growthModData.keySet();
        growthModifiedPlants = new HashSet<>();

        for (String key : keys) {
            Route r = Route.fromString(key);

            if (r.length() == 1) {
                Material m = Material.getMaterial(key);

                if (m == null) {
                    logger.warn("Material '" + key + "' is not a Bukkit Material!");
                    logger.warn("Plant growth modifiers for '" + key + "' are ignored.");
                } else {
                    // TODO: Name mapping of vines and bamboo (new function getMappedName)
                    growthModifiedPlants.add(m);
                }
            }
        }

        if (debug) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- Growth modified plants ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material m : growthModifiedPlants) {
                    logger.logToFile("  - " + m, logFile);
                }
            }, 2 * 20);
        }

    }

    /**
     * Updates the set of clickable seeds based on the specified plant and aquatic plant {@link Material}s.
     * Additionally, performs debug logging if the debug mode is enabled.
     */
    private void updateClickableSeeds() {
        clickableSeedsMap = new HashMap<>();
        clickableSeeds = new HashSet<>();

        for (Material plant : plants) {
            clickableSeedsMap.put(plant.createBlockData().getPlacementMaterial(), plant);
        }

        for (Material plant : aquaticPlants) {
            clickableSeedsMap.put(plant.createBlockData().getPlacementMaterial(), plant);
        }

        clickableSeeds.addAll(clickableSeedsMap.keySet());


        // getPlacementMaterial() returns AIR for e.g. BAMBOO_SAPLING
        clickableSeeds.remove(Material.AIR);

        // also remove torchFlower, since it is already a fully grown decoration plant
        clickableSeeds.remove(Material.TORCHFLOWER);


        if (debug) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- Material --> Clickable Seed ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material plant : plants) {
                    logger.logToFile(plant.toString(), logFile);
                    logger.logToFile("  -> " + plant.createBlockData().getPlacementMaterial(), logFile);
                }
                for (Material plant : aquaticPlants) {
                    logger.logToFile(plant.toString(), logFile);
                    logger.logToFile("  -> " + plant.createBlockData().getPlacementMaterial(), logFile);
                }
                // Log clickable Seeds Set:
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- Clickable Seeds ----------------------", logFile);
                logger.logToFile("", logFile);
                for (Material seed : clickableSeeds) {
                    logger.logToFile("  - " + seed, logFile);
                }
            }, 3 * 20);
        }
    }


    private void drawLogo() {
        String logo = System.lineSeparator() +
                System.lineSeparator() +
                "&2     .{{}}}}}}." + System.lineSeparator() +
                "&2    {{{{{{(`)}}}." + System.lineSeparator() +
                "&2   {{{(`)}}}}}}}}}" + System.lineSeparator() +
                "&2  }}}}}}}}}{{(`){{{" + "&b     Realistic &aPlant &bGrowth" + System.lineSeparator() +
                "&2  }}}}{{{{(`)}}{{{{" + "&b       by &6TheRealPredator" + System.lineSeparator() +
                "&2 {{{(`)}}}}}}}{}}}}}" + System.lineSeparator() +
                "&2{{{{{{{{(`)}}}}}}}}}}" + System.lineSeparator() +
                "&2{{{{{{{}{{{{(`)}}}}}}" + "&a    ... successfully enabled." + System.lineSeparator() +
                "&2 {{{{{(`&r)   {&2{{{(`)}'" + System.lineSeparator() +
                "&2  `\"\"'\" &r|   | &2\"'\"'`" + System.lineSeparator() +
                "       &r/     \\" + System.lineSeparator() +
                "&a~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + System.lineSeparator();
        logger.log(logo);

    }

    // Getters

    /**
     * Provides access to the singleton instance of the {@link RealisticPlantGrowth} plugin.
     *
     * @return The singleton instance of the {@link RealisticPlantGrowth} plugin.
     */
    @NotNull
    public static RealisticPlantGrowth getInstance() {
        return instance;
    }

    /**
     * Retrieves the configuration manager associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link ConfigManager} instance managing plugin configurations.
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return cm;
    }

    /**
     * Retrieves the message manager associated with the {@link RealisticPlantGrowth} plugin.
     *
     * @return The {@link MessageManager} instance handling plugin messages.
     */
    @NotNull
    public MessageManager getMessageManager() {
        return MessageManager.get();
    }


    /**
     * Retrieves the corresponding {@link Material} that a seed converts to if the seed is placed.
     *
     * @param seed {@link Material} representing the seed to inquire about.
     * @return The {@link Material} that the provided seed can grow into, or {@code null} if not applicable.
     */
    @Nullable
    public Material getMaterialFromSeed(@NotNull Material seed) {
        if (clickableSeeds.contains(seed))
            return clickableSeedsMap.get(seed);
        return null;
    }

    /**
     * Checks if the given {@link Block} represents a plant.
     *
     * @param b The {@link Block} to check.
     * @return {@code true} if the {@link Block} b is a plant, {@code false} otherwise.
     */
    public boolean isAPlant(@NotNull Block b) {
        return plants.contains(b.getType());
    }

    /**
     * Checks if the given {@link Block} represents an agricultural plant.
     *
     * @param b The {@link Block} to check.
     * @return {@code true} if the {@link Block} b is an agricultural plant, {@code false} otherwise.
     */
    public boolean isAgriculturalPlant(@NotNull Block b) {
        return agriculturalPlants.contains(b.getType());
    }

    /**
     * Checks if the given {@link Block} represents an aquatic plant.
     *
     * @param b The {@link Block} to check.
     * @return {@code true} if the {@link Block} b is an aquatic plant, {@code false} otherwise.
     */
    public boolean isAnAquaticPlant(@NotNull Block b) {
        return aquaticPlants.contains(b.getType());
    }

    /**
     * Checks if the given {@link Block} represents a sapling.
     *
     * @param b The {@link Block} to check.
     * @return {@code true} if the {@link Block} b is a sapling, {@code false} otherwise.
     */
    public boolean isSapling(@NotNull Block b) {
        return saplings.contains(b.getType());
    }

    /**
     * Checks if the given {@link Material} is a growth-modified plant.
     *
     * @param m The {@link Material} to check.
     * @return {@code true} if the {@link Material} m is a growth-modified plant, {@code false} otherwise.
     */
    public boolean isGrowthModifiedPlant(@NotNull Material m) {
        return growthModifiedPlants.contains(m);
    }

    /**
     * Checks if the specified material can grow in the dark based on configuration.
     *
     * @param m The {@link Material} to check.
     * @return {@code true} if the {@link Material} m can grow in the dark, {@code false} otherwise.
     */
    public boolean canGrowInDark(@NotNull Material m) {
        return cm.getGrow_In_Dark().contains(m);
    }

    /**
     * Checks if a given {@link Material} represents a clickable seed.
     *
     * @param material The {@link Material} to check for clickability.
     * @return {@code true} if the {@link Material} is a clickable seed, {@code false} otherwise.
     */
    public boolean isClickableSeed(@NotNull Material material) {
        return clickableSeeds.contains(material);
    }

    public boolean isUpwardsGrowingPlant(@NotNull Material material) {
        return upwardsGrowingPlants.contains(material);
    }

    public boolean isDownwardsGrowingPlant(@NotNull Material material) {
        return downwardsGrowingPlants.contains(material);
    }

    public boolean isGrowEventReturnsAirBlockPlant(@NotNull Material material) {
        return growEventReturnsAirBlockPlants.contains(material);
    }

    /**
     * Checks if plant growth modification is disabled for the specified world.
     *
     * @param world The {@link World} to check for plant growth modification.
     * @return {@code true} if growth modification is enabled for the world, {@code false} otherwise.
     */
    public boolean isWorldDisabled(@NotNull World world) {
        return (!cm.getEnabled_worlds().contains(world.getName()));
    }

    /**
     * Retrieves a HashSet containing {@link Material}s of plants that have growth modifications applied.
     *
     * @return A HashSet of Material objects representing plants with growth modifications.
     */
    public HashSet<Material> getGrowthModifiedPlants() {
        return growthModifiedPlants;
    }

    /**
     * Checks whether the debug mode is enabled.
     *
     * @return {@code true} if debug mode is enabled, {@code false} otherwise.
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Checks whether the verbose mode is enabled.
     *
     * @return {@code true} if verbose mode is enabled, {@code false} otherwise.
     */
    public static boolean isVerbose() {
        return verbose;
    }

}
