package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.listeners.*;
import de.nightevolution.utils.BiomeChecker;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.Surrounding;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RealisticPlantGrowth extends JavaPlugin implements Listener {

    // For convenience, a reference to the instance of this plugin
    private static RealisticPlantGrowth instance;

    private static final String classPrefix = "RealisticPlantGrowth: ";
    private static final String logFile = "debug";

    private static boolean verbose = false;
    private static boolean debug = false;

    private static ConfigManager cm;
    private MessageManager mm;
    private BukkitAudiences bukkitAudiences;

    private Logger logger;


    /**
     * Used for 'require_hoe_to_harvest' setting
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
     * All supported land plants
     * #saplings are added later to this List.
     */
    private static final Set<Material> plants = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.CAVE_VINES,
            Material.CHORUS_FLOWER,
            Material.COCOA,
            Material.CRIMSON_FUNGUS,
            Material.GLOW_LICHEN,
            Material.GRASS,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.SWEET_BERRY_BUSH,
            Material.TALL_GRASS,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.TWISTING_VINES,
            Material.VINE,
            Material.WARPED_FUNGUS,
            Material.WEEPING_VINES,
            Material.WHEAT
    ));

    /**
     * All supported aquatic plants
     */
    private static final Set<Material> aquaticPlants = new HashSet<>(Arrays.asList(
            Material.KELP,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    private static HashSet<Material> growthModifiedPlants;

    // (Key) Clickable Seed -> (Value) Plant
    private static HashMap<Material, Material> clickableSeedsMap;
    private static HashSet<Material> clickableSeeds;

    @Override
    //TODO: Add Startup Messages
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        // Initialize an audiences instance for the plugin
        this.bukkitAudiences = BukkitAudiences.create(this);

        updateVariables();
        drawLogo();

    }

    private void registerCommands(){
        Objects.requireNonNull(instance.getCommand("rpg")).setExecutor(CommandManager.get());
    }
    private void registerTabCompleter(){
        Objects.requireNonNull(instance.getCommand("rpg")).setTabCompleter(new TabCompleterImpl());
    }

    private void registerListeners(){
        new BlockGrowListener(instance);
        new StructureGrowListener(instance);
        new BlockSpreadListener(instance);
        new BlockFertilizeListener(instance);
        new PlayerInteractListener(instance);
        new BlockBreakListener(instance);
    }

    public void reload(){
        cm.reloadAllYAMLFiles();
        HandlerList.unregisterAll((Plugin) RealisticPlantGrowth.getInstance());
        updateVariables();
    }

    public void updateVariables(){
        cm = ConfigManager.get();
        mm = MessageManager.get();

        verbose = cm.isVerbose();
        debug = cm.isDebug_log();

        logger = new Logger(this.getClass().getSimpleName(), this, verbose, debug);

        getSaplingsTag();
        updateGrowthModifiedPlants();
        updateClickableSeeds();
        registerCommands();
        registerTabCompleter();
        Surrounding.clearCache();
        BiomeChecker.clearCache();
        registerListeners();
    }

    /**
     * Method used by error used, if critical error appears.
     * Disables this plugin via the Bukkit plugin manager.
     */
    void disablePlugin(){
        logger.log("");
        logger.error("&cDisabling " + this.getClass().getSimpleName() + "...");
        logger.log("");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }
    }



    public @NonNull BukkitAudiences getBukkitAudiences() {
        if (this.bukkitAudiences == null) {
            throw new IllegalStateException(classPrefix + "Tried to access Adventure API when the plugin was disabled!");
        }
        return this.bukkitAudiences;
    }


    // Getters
    public static RealisticPlantGrowth getInstance(){
        return instance;
    }
    public ConfigManager getConfigManager(){
        return cm;
    }
    public MessageManager getMessageManager(){
        return this.mm;
    }



    /**
     * Adds all saplings to the seeds and plants list.
     * Saplings are chosen by vanilla {@code saplings} tag.
     * AZALEA and FLOWERING_AZALEA are also included.
     */
    private void getSaplingsTag(){
        logger.logToFile("Getting saplings tag...", logFile);
        Set<Material> saplingSet = (Tag.SAPLINGS.getValues());

        logger.verbose("Adding saplings to plant list...");
        plants.addAll(saplingSet);

        if(verbose){
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- All Plants ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material material : plants){
                    logger.logToFile("  - " + material, logFile);
                }
            }, 20);
        }
    }
    public boolean isAPlant(@NotNull Block b){
        return plants.contains(b.getType());
    }
    public boolean isAgriculturalPlant(@NotNull Block b){
        return agriculturalPlants.contains(b.getType());
    }
    public boolean isAnAquaticPlant(@NotNull Block b){
        return aquaticPlants.contains(b.getType());
    }
    public boolean canGrowInDark(@NotNull Material m){
        return cm.getGrow_In_Dark().contains(m);
    }


    /**
     * Identifies and collects plants with modified growth behavior from the root entries of GrowthModifiers.
     * Each root entry corresponds to a plant with modified growth characteristics.
     * The method extracts these plants and adds them to the collection of modified growth rate plants.
     */
    private void updateGrowthModifiedPlants(){
        Map<String, Object> growthModData = cm.getGrowthModifiers();
        Set<String> keys = growthModData.keySet();
        growthModifiedPlants = new HashSet<>();

        for (String key: keys){
            Route r = Route.fromString(key);

            if(r.length() == 1){
                Material m = Material.getMaterial(key);

                if(m == null){
                    logger.warn("Material '" + key + "' is not a Bukkit Material!" );
                    logger.warn("Plant growth modifiers for '" + key + "' are ignored.");
                }else {
                    growthModifiedPlants.add(m);
                }
            }
        }
        
        if(debug){
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () ->{
                logger.logToFile("", logFile);
                logger.logToFile("---------------------- Growth modified plants ----------------------", logFile);
                logger.logToFile("", logFile);

                for (Material m :growthModifiedPlants){
                    logger.logToFile("  - " + m, logFile);
                }
            }, 2 * 20);
        }
        
    }

    /**
     * Retrieves the items, which the player can click to show information
     * about the growth rates for that seed.
     * Should be executed AFTER updateGrowthModifiedPlants() and getSaplingsTag()
     * Iterates through "growthModifiedPlants" Set
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

                for(Material plant : plants) {
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
            },3 * 20);
        }
    }


    private void drawLogo(){
        String logo = System.lineSeparator() +
                System.lineSeparator() +
                "&2     .{{}}}}}}." + System.lineSeparator() +
                "&2    {{{{{{(`)}}}." + System.lineSeparator() +
                "&2   {{{(`)}}}}}}}}}" + System.lineSeparator() +
                "&2  }}}}}}}}}{{(`){{{" + "&b     Realistic &aPlant &bGrowth" + System.lineSeparator() +
                "&2  }}}}{{{{(`)}}{{{{" + "&b       by &6TheRealPredator" + System.lineSeparator() +
                "&2 {{{(`)}}}}}}}{}}}}}" + System.lineSeparator() +
                "&2{{{{{{{{(`)}}}}}}}}}}" + System.lineSeparator() +
                "&2{{{{{{{}{{{{(`)}}}}}}" + "&a    ... successfully enabled." +System.lineSeparator() +
                "&2 {{{{{(`&r)   {&2{{{(`)}'" + System.lineSeparator() +
                "&2  `\"\"'\" &r|   | &2\"'\"'`" + System.lineSeparator() +
                "       &r/     \\" + System.lineSeparator() +
                "&a~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + System.lineSeparator();
        logger.log(logo);

    }
    public static boolean isDebug() {
        return debug;
    }
    public static boolean isVerbose() {
        return verbose;
    }
@Nullable
    public Material getMaterialFromSeed(Material seed){
        if(clickableSeeds.contains(seed))
            return clickableSeedsMap.get(seed);
        return null;
    }

    public boolean isClickableSeed(Material material){
        return clickableSeeds.contains(material);
    }

    /**
     * Checks if plant growth modification is enabled for the specified world.
     *
     * @param world The World to check for plant growth modification.
     * @return True if growth modification is enabled for the world, false otherwise.
     */
    public boolean isWorldEnabled(World world){
        return(cm.getEnabled_worlds().contains(world.getName()));
    }

    public HashSet<Material> getGrowthModifiedPlants(){
        return growthModifiedPlants;
    }


}
