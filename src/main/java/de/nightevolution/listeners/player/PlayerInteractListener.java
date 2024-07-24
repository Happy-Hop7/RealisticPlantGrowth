package de.nightevolution.listeners.player;

import de.nightevolution.ConfigManager;
import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.enums.MessageType;
import de.nightevolution.utils.enums.PlaceholderInterface;
import de.nightevolution.utils.mapper.VersionMapper;
import de.nightevolution.utils.plant.SpecialBlockSearch;
import de.nightevolution.utils.plant.Surrounding;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Listens to player block interactions in order to provide information
 * about crop growth rates in the current biome.
 */
public class PlayerInteractListener implements Listener, PlaceholderInterface {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;
    private final MessageManager msgManager;
    private final VersionMapper versionMapper;
    private final boolean logEvent;
    private final String logFile = "PlayerInteractEvent";

    private static final HashMap<UUID, Long> playerCooldownMap = new HashMap<>();

    /**
     * Constructs a new {@link PlayerInteractListener}.
     *
     * @param instance The main plugin instance of RealisticPlantGrowth.
     */
    public PlayerInteractListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.msgManager = instance.getMessageManager();
        this.versionMapper = instance.getVersionMapper();

        // Enable logging if debug mode is active and player logging is enabled
        this.logEvent = (RealisticPlantGrowth.isDebug() && cm.isPlayer_log());

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    /**
     * Handles player interactions with blocks, specifically when holding clickable seeds.
     *
     * @param e The PlayerInteractEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e) {

        if (logEvent) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Player Interact Event --------------------", logFile);
            logger.logToFile("  Player: " + e.getPlayer().getName(), logFile);
            logger.logToFile("  Player location: " + e.getPlayer().getLocation(), logFile);
        }

        // Check if growth rate display is enabled
        if (!cm.isDisplay_growth_rates()) {
            if (logEvent)
                logger.logToFile("  Display_growth_rates deactivated.", logFile);
            return;
        }

        // Check if the world is enabled for plant growth modification
        World eventWorld = e.getPlayer().getWorld();
        if (instance.isWorldDisabled(eventWorld)) {
            if (logEvent)
                logger.logToFile("  World: " + eventWorld.getName() + " is not activated for plant growth modification.", logFile);
            return;
        }

        // Check if the player is holding a clickable seed in their hand
        if (!versionMapper.isClickableSeed(e.getMaterial())) {
            return;
        }

        // Check if the player interaction was a left click
        if (e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        // If the clicked block is a plant, prioritize harvest over growth information
        Block clickedBlock = e.getClickedBlock();
        if (!e.hasItem() || clickedBlock == null || versionMapper.isAPlant(clickedBlock.getType())) {
            return;
        }

        // Use the block above the clicked block to avoid a skylight level of zero
        Block eventBlock = clickedBlock.getRelative(BlockFace.UP);
        Player ePlayer = e.getPlayer();

        // Check if the player has the required permission
        if (!ePlayer.hasPermission("rpg.info.interact")) {
            if (logEvent)
                logger.logToFile("Player " + ePlayer.getName() + " lacks 'rpg.info.interact' permission.", logFile);
            return;
        }

        // Use a BlockState to "change" the block type without actually changing the block type in the world
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material plantMaterial = versionMapper.getMaterialFromSeed(e.getMaterial());

        // getMaterialFromSeed is nullable
        if (plantMaterial == null) {
            if (logEvent)
                logger.logToFile("Could not retrieve plant material from seed material: " + e.getMaterial(), logFile);
            return;
        }

        // Add a cooldown to stop click spamming, preventing unnecessary heavy area calculations
        Long lastTime = playerCooldownMap.get(ePlayer.getUniqueId());
        long currentTime = System.currentTimeMillis();

        // Cooldown in milliseconds
        int cooldown = cm.getDisplay_cooldown() * 1000;
        if (lastTime != null) {
            if (logEvent) {
                logger.logToFile("  Last interact time: " + lastTime, logFile);
                logger.logToFile("  Current time: " + currentTime, logFile);
                logger.logToFile("  Cooldown: " + cooldown + " ms", logFile);
            }

            if ((currentTime - lastTime) < cooldown) {
                logger.logToFile("  PlayerInteractEvent triggered during cooldown period.", logFile);
                return;
            }
        }

        // Update the player's last interaction time
        playerCooldownMap.put(ePlayer.getUniqueId(), currentTime);

        // Cancel the interact event if the player is in creative mode.
        // Prevents destroying clicked blocks
        if (ePlayer.getGameMode() == GameMode.CREATIVE)
            e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  All pre-checks passed.", logFile);
            logger.logToFile("  Seed material in player's hand: " + e.getMaterial(), logFile);
            logger.logToFile("  Material derived from seed: " + plantMaterial, logFile);
        }

        // Check if the plant's growth is modified by the plugin
        if (!versionMapper.isGrowthModifiedPlant(plantMaterial)) {
            logger.logToFile("  Vanilla behavior for plant material: " + plantMaterial, logFile);

            // Send a message to the player
            msgManager.sendLocalizedMsg(ePlayer, MessageType.PLANT_NOT_MODIFIED_MSG,
                    PLANT_PLACEHOLDER, plantMaterial.toString().toLowerCase(), true);

            return;
        }

        // Temporarily set the event block state to the plant material
        eventBlockState.setType(plantMaterial);

        if (logEvent)
            logger.logToFile("  Calculating Growth Modifier Data...", logFile);

        // Calculate the surrounding environment's effect on the plant's growth
        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlock, eventBlockState);

        double growthRate = surrounding.getGrowthRate();
        double deathChance = surrounding.getDeathChance();

        if (logEvent) {
            logger.logToFile("    Growth rate: " + growthRate, logFile);
            logger.logToFile("    Death chance: " + deathChance, logFile);
            logger.logToFile("    Biome: " + surrounding.getBiome(), logFile);
        }

        // Prepare placeholders and their replacements for the localized message
        List<String> placeholders = Arrays.asList(
                PLANT_PLACEHOLDER,
                GROWTH_RATE_PLACEHOLDER,
                DEATH_CHANCE_PLACEHOLDER,
                BIOME_PLACEHOLDER,
                IS_VALID_BIOME_PLACEHOLDER,
                FERTILIZER_USED_PLACEHOLDER,
                UV_LIGHT_USED_PLACEHOLDER,
                CAN_GROW_IN_DARK_PLACEHOLDER,
                IS_DARK_PLACEHOLDER
        );

        List<Object> replacements = Arrays.asList(
                e.getMaterial().toString().toLowerCase(),
                growthRate,
                deathChance,
                surrounding.getBiome().toString().toLowerCase(),
                surrounding.isInValidBiome(),
                surrounding.usedFertilizer(),
                surrounding.hasUVLightAccess(),
                versionMapper.getMaterialMapper().canGrowInDark(plantMaterial),
                surrounding.isInDarkness()
        );

        // Send the localized message to the player
        msgManager.sendLocalizedMsg(ePlayer, MessageType.GROWTH_RATE_MSG, placeholders, replacements, true);
    }

    /**
     * Clears the cooldown data for a specific player.
     * Called in {@link PlayerQuitListener}.
     *
     * @param uuid The UUID of the player.
     */
    public static void clearPlayerCooldownData(UUID uuid) {
        playerCooldownMap.remove(uuid);
    }
}