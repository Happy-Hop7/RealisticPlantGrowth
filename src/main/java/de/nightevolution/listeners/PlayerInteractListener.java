package de.nightevolution.listeners;

import de.nightevolution.ConfigManager;
import de.nightevolution.MessageManager;
import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.Logger;
import de.nightevolution.utils.MessageType;
import de.nightevolution.utils.SpecialBlockSearch;
import de.nightevolution.utils.Surrounding;
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
 * Listens to player block interactions in order to give information
 * about crops growing rates in the current biome.
 */
public class PlayerInteractListener implements Listener {

    private final RealisticPlantGrowth instance;
    private final ConfigManager cm;
    private final Logger logger;
    private final MessageManager msgManager;

    private final String PLANT_PLACEHOLDER = "{PLANT}";
    private final String GROWTH_RATE_PLACEHOLDER = "{GROWTH_RATE}";
    private final String DEATH_CHANCE_PLACEHOLDER = "{DEATH_CHANCE}";
    private final String BIOME_PLACEHOLDER = "{BIOME}";
    private final String IS_VALID_BIOME_PLACEHOLDER = "{IS_VALID_BIOME}";
    private final String FERTILIZER_USED_PLACEHOLDER = "{FERTILIZER_USED}";
    private final String UV_LIGHT_USED_PLACEHOLDER = "{UV_LIGHT_USED}";
    private final String CAN_GROW_IN_DARK_PLACEHOLDER = "{CAN_GROW_IN_DARK}";
    private final String IS_DARK_PLACEHOLDER = "{IS_DARK}";


    private static final HashMap<UUID, Long> playerCooldownMap = new HashMap<>();


    public PlayerInteractListener(RealisticPlantGrowth instance) {
        this.instance = instance;
        this.cm = instance.getConfigManager();
        this.msgManager = instance.getMessageManager();

        logger = new Logger(this.getClass().getSimpleName(), instance, RealisticPlantGrowth.isVerbose(), RealisticPlantGrowth.isDebug());
        instance.getServer().getPluginManager().registerEvents(this, instance);

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e) {
        logger.verbose("Interact-Event");

        if (!cm.isDisplay_growth_rates())
            return;

        // Check if the world is enabled for plant growth modification
        World eventWorld = e.getPlayer().getWorld();
        if (instance.isWorldDisabled(eventWorld))
            return;

        if (!instance.isClickableSeed(e.getMaterial())) {
            return;
        }

        if ((e.getAction() != Action.LEFT_CLICK_BLOCK))
            return;


        Block clickedBlock = e.getClickedBlock();
        if (!e.hasItem() || clickedBlock == null || instance.isAPlant(clickedBlock)) {
            return;
        }

        logger.verbose("All checks passed.");
        logger.verbose("Getting Block Data...");

        // Using block above the clicked Block to avoid a SkyLight-Level of zero.
        Block eventBlock = clickedBlock.getRelative(BlockFace.UP);

        Player ePlayer = e.getPlayer();
        if (!ePlayer.hasPermission("rpg.info.interact")) {
            return;
        }

        // Using a BlockState to "change" the Block type without actually changing the Block type in the world.
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material plantMaterial = instance.getMaterialFromSeed(e.getMaterial());

        // getMaterialFromSeed is nullable.
        if (plantMaterial == null)
            return;

        // Adding a cooldown to stop click spamming which prevents unnecessary heavy area calculations.
        Long lastTime = playerCooldownMap.get(e.getPlayer().getUniqueId());
        long currentTime = System.currentTimeMillis();

        // cooldown in milliseconds
        int cooldown = (cm.getDisplay_cooldown() * 1000);
        if (lastTime != null) {
            logger.verbose("lastTime: " + lastTime);
            logger.verbose("currentTime: " + currentTime);
            logger.verbose("cooldown: " + cooldown);
            if ((currentTime - lastTime) < cooldown) {
                logger.verbose("PlayerInteractEvent-Cooldown.");
                return;
            }

        }

        playerCooldownMap.put(e.getPlayer().getUniqueId(), currentTime);

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
            e.setCancelled(true);

        if (!instance.isGrowthModifiedPlant(plantMaterial)) {
            logger.verbose("Vanilla behavior for: " + plantMaterial);

            // send a player message
            msgManager.sendLocalizedMsg(ePlayer, MessageType.PLANT_NOT_MODIFIED_MSG,
                    PLANT_PLACEHOLDER, plantMaterial.toString().toLowerCase(), true);

            return;
        }


        eventBlockState.setType(plantMaterial);

        logger.verbose("Calculating Data.");
        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlock, eventBlockState);


        double growthRate = surrounding.getGrowthRate();
        double deathChance = surrounding.getDeathChance();


        logger.verbose("growthRate: " + growthRate);
        logger.verbose("deathChance: " + deathChance);
        logger.verbose("Biome: " + surrounding.getBiome());

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
                instance.canGrowInDark(plantMaterial),
                surrounding.isInDarkness()
        );

        msgManager.sendLocalizedMsg(ePlayer, MessageType.GROWTH_RATE_MSG, placeholders, replacements, true);

    }

    public static void clearPlayerCooldownData(UUID uuid) {
        playerCooldownMap.remove(uuid);
    }

}
