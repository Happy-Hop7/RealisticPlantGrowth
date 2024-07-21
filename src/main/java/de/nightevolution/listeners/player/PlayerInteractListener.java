package de.nightevolution.listeners.player;

import de.nightevolution.ConfigManager;
import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.enums.MessageType;
import de.nightevolution.utils.enums.PlaceholderInterface;
import de.nightevolution.utils.mapper.MaterialMapper;
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

import java.util.*;

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
    private final MaterialMapper materialMapper;
    private final String logFile = "PlayerInteractEvent";
    private final boolean logEvent;

    private static final HashMap<UUID, Long> playerCooldownMap = new HashMap<>();

    public PlayerInteractListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.msgManager = instance.getMessageManager();
        this.versionMapper = instance.getVersionMapper();
        this.materialMapper = versionMapper.getMaterialMapper();
        this.logEvent = RealisticPlantGrowth.isDebug();

        logger = new Logger(this.getClass().getSimpleName(), RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e) {
        if (logEvent) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Player Interact Event --------------------", logFile);
            logger.logToFile("  Player: " + e.getPlayer().getName(), logFile);
            logger.logToFile("  Player location: " + e.getPlayer().getLocation(), logFile);
        }

        if (!cm.isDisplay_growth_rates()) {
            if (logEvent)
                logger.logToFile("  Display_growth_rates deactivated.", logFile);
            return;
        }

        // Check if the world is enabled for plant growth modification
        World eventWorld = e.getPlayer().getWorld();
        if (instance.isWorldDisabled(eventWorld)) {
            if (logEvent)
                logger.logToFile("  World: " + eventWorld + " not activated.", logFile);
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

        // Use the block above the clicked block to avoid a sky light level of zero
        Block eventBlock = clickedBlock.getRelative(BlockFace.UP);
        Player ePlayer = e.getPlayer();

        if (!ePlayer.hasPermission("rpg.info.interact")) {
            if (logEvent)
                logger.logToFile("Player has no interact permission.", logFile);
            return;
        }

        // Use a BlockState to "change" the block type without actually changing the block type in the world
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material plantMaterial = versionMapper.getMaterialFromSeed(e.getMaterial());

        // getMaterialFromSeed is nullable
        if (plantMaterial == null) {
            if (logEvent)
                logger.logToFile("Could not retrieve a plant material from used seed material.", logFile);
            return;
        }

        // Add a cooldown to stop click spamming, preventing unnecessary heavy area calculations
        Long lastTime = playerCooldownMap.get(e.getPlayer().getUniqueId());
        long currentTime = System.currentTimeMillis();

        // Cooldown in milliseconds
        int cooldown = (cm.getDisplay_cooldown() * 1000);
        if (lastTime != null) {
            if (logEvent) {
                logger.logToFile("  Last interact time: " + lastTime, logFile);
                logger.logToFile("  Current time: " + currentTime, logFile);
                logger.logToFile("  Cooldown: " + cooldown + " ms", logFile);
            }

            if ((currentTime - lastTime) < cooldown) {
                logger.logToFile("  PlayerInteractEvent during cooldown.", logFile);
                return;
            }
        }

        playerCooldownMap.put(e.getPlayer().getUniqueId(), currentTime);

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  All pre-checks passed.", logFile);
            logger.logToFile("  Seed material in player hand: " + e.getMaterial(), logFile);
            logger.logToFile("  Material derived from seed: " + plantMaterial, logFile);
        }

        if (!versionMapper.isGrowthModifiedPlant(plantMaterial)) {
            logger.logToFile("  Vanilla behavior for: " + plantMaterial, logFile);

            // Send a player message
            msgManager.sendLocalizedMsg(ePlayer, MessageType.PLANT_NOT_MODIFIED_MSG,
                    PLANT_PLACEHOLDER, plantMaterial.toString().toLowerCase(), true);

            return;
        }

        eventBlockState.setType(plantMaterial);

        if (logEvent)
            logger.logToFile("  Calculating Growth Modifier Data...", logFile);

        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlock, eventBlockState);

        double growthRate = surrounding.getGrowthRate();
        double deathChance = surrounding.getDeathChance();

        if (logEvent) {
            logger.logToFile("    Growth rate: " + growthRate, logFile);
            logger.logToFile("    Death chance: " + deathChance, logFile);
            logger.logToFile("    Biome: " + surrounding.getBiome(), logFile);
        }

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
                plantMaterial.toString().toLowerCase(),
                growthRate,
                deathChance,
                surrounding.getBiome().toString().toLowerCase(),
                surrounding.isInValidBiome(),
                surrounding.usedFertilizer(),
                surrounding.hasUVLightAccess(),
                versionMapper.getMaterialMapper().canGrowInDark(plantMaterial),
                surrounding.isInDarkness()
        );

        msgManager.sendLocalizedMsg(ePlayer, MessageType.GROWTH_RATE_MSG, placeholders, replacements, true);
    }

    public static void clearPlayerCooldownData(UUID uuid) {
        playerCooldownMap.remove(uuid);
    }
}
