package de.nightevolution;

import de.nightevolution.commands.CommandManager;
import de.nightevolution.commands.TabCompleterImpl;
import de.nightevolution.listeners.*;
import de.nightevolution.utils.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class RealisticPlantGrowth extends JavaPlugin implements Listener {

    // For convenience, a reference to the instance of this plugin
    private static RealisticPlantGrowth instance;

    private static final String classPrefix = "RealisticPlantGrowth: ";

    private static boolean verbose = false;
    private static boolean debug = false;

    private static ConfigManager configManager;
    private MessageManager messageManager;
    private BukkitAudiences bukkitAudiences;

    private Logger logger;


    // #saplings are added later to this List.
    private static final Set<Material> seeds = new HashSet<>(Arrays.asList(
            Material.BAMBOO,
            Material.BEETROOT_SEEDS,
            Material.BROWN_MUSHROOM,
            Material.CACTUS,
            Material.CARROT,
            Material.CHORUS_FLOWER,
            Material.COCOA_BEANS,
            Material.CRIMSON_FUNGUS,
            Material.GLOW_BERRIES,
            Material.KELP,
            Material.MELON_SEEDS,
            Material.NETHER_WART,
            Material.PITCHER_POD,
            Material.POTATO,
            Material.PUMPKIN_SEEDS,
            Material.RED_MUSHROOM,
            Material.SUGAR_CANE,
            Material.SWEET_BERRIES,
            Material.TORCHFLOWER_SEEDS,
            Material.TWISTING_VINES,
            Material.VINE,
            Material.WARPED_FUNGUS,
            Material.WHEAT_SEEDS,
            Material.WEEPING_VINES
    ));

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

    // #saplings are added later to this List.
    private static final Set<Material> plants = new HashSet<>(Arrays.asList(
            Material.BAMBOO_SAPLING,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.CAVE_VINES,
            Material.CHORUS_FLOWER,
            Material.COCOA,
            Material.CRIMSON_FUNGUS,
            Material.GLOW_LICHEN,
            Material.MELON_STEM,
            Material.NETHER_WART,
            Material.PITCHER_CROP,
            Material.POTATOES,
            Material.PUMPKIN_STEM,
            Material.SWEET_BERRY_BUSH,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.TWISTING_VINES,
            Material.WARPED_FUNGUS,
            Material.WEEPING_VINES,
            Material.WHEAT
    ));

    private static final Set<Material> aquaticPlants = new HashSet<>(Arrays.asList(
            Material.KELP,
            Material.SEAGRASS,
            Material.SEA_PICKLE,
            Material.TALL_SEAGRASS
    ));

    @Override
    //TODO: Add Startup Messages
    public void onEnable() {
        // Create an instance of this Plugin
        instance = this;

        // Initialize an audiences instance for the plugin
        this.bukkitAudiences = BukkitAudiences.create(this);

        configManager = ConfigManager.get();
        verbose = configManager.isVerbose();
        debug = configManager.isDebug_log();

        logger = new Logger(this.getClass().getSimpleName(), this, verbose, debug);

        getSaplingsTag();
        registerCommands();
        registerTabCompleter();
        registerListeners();

        logger.log("");
        logger.log("&2" + this.getClass().getSimpleName() + "&2 successfully enabled.");
        logger.log("");


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }
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
        new PlayerListener(instance);
        new BlockBreakListener(instance);
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
        return configManager;
    }
    public MessageManager getMessageManager(){
        return this.messageManager;
    }


    public static void update(){
        // TODO: get new data from configManager
    }

    /**
     * Adds all saplings to the seeds and plants list.
     * Saplings are chosen by vanilla {@code saplings} tag.
     * AZALEA and FLOWERING_AZALEA are also included.
     */
    private void getSaplingsTag(){
        logger.verbose("Getting saplings tag...");
        Set<Material> saplingSet = (Tag.SAPLINGS.getValues());

        logger.verbose("Adding saplings to List");
        seeds.addAll(saplingSet);
        plants.addAll(saplingSet);

        logger.verbose("Printing Lists:");
        if(verbose){
            logger.verbose(" - Seeds:");
            for (Material material : seeds){
                logger.verbose("    - " + material);
            }

            logger.verbose("Plants:");
            for (Material material : plants){
                logger.verbose("    - " + material);
            }
        }
    }
    public boolean isAPlant(Block b){
        return plants.contains(b.getType());
    }

    public boolean isAgriculturalPlant(Block b){
        return agriculturalPlants.contains(b.getType());
    }
    public boolean isAnAquaticPlant(Block b){
        return aquaticPlants.contains(b.getType());
    }
    public boolean canGrowInDark(Block b){
        return configManager.getGrow_in_dark().contains(b.getType());
    }


    public static boolean isDebug() {
        return debug;
    }
    public static boolean isVerbose() {
        return verbose;
    }


}
