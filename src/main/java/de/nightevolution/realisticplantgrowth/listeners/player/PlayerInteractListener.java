package de.nightevolution.realisticplantgrowth.listeners.player;

import de.nightevolution.realisticplantgrowth.ConfigManagerOld;
import de.nightevolution.realisticplantgrowth.MessageManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.enums.MessageType;
import de.nightevolution.realisticplantgrowth.utils.enums.PlaceholderInterface;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.plant.SpecialBlockSearch;
import de.nightevolution.realisticplantgrowth.plant.Surrounding;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Listens to player block interactions in order to provide information
 * about crop growth rates in the current biome.
 */
public class PlayerInteractListener implements Listener, PlaceholderInterface {

    private final RealisticPlantGrowth instance;
    private final ConfigManagerOld cm;
    private final Logger logger;
    private final MessageManager msgManager;
    private final VersionMapper versionMapper;
    private final Random randomNumberGenerator;
    private final boolean logEvent;
    private static final String LOG_FILE = "PlayerInteractEvent";

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

        this.randomNumberGenerator = RealisticPlantGrowth.isDebug() ? new Random(1) : new Random();

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    /**
     * Handles player interactions with blocks, specifically when holding clickable seeds.
     *
     * @param e The PlayerInteractEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        logEventHeader(e);

        Player player = e.getPlayer();
        if (instance.isWorldDisabled(player.getWorld())) {
            if (logEvent) logger.logToFile("  -> World is disabled for RealisticPlantGrowth.", LOG_FILE);
            return;
        }

        Action action = e.getAction();
        Material material = e.getMaterial();
        Block clickedBlock = e.getClickedBlock();

        if (action == Action.LEFT_CLICK_BLOCK && versionMapper.isClickableSeed(material)) {
            onPlayerInteractEventWithClickableSeeds(e);
        }

        else if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && clickedBlock.getType() == Material.COMPOSTER) {
            onComposterFillEvent(e);
        }
    }

    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e) {

        // Check if growth rate display is enabled
        if (!cm.isDisplay_growth_rates()) {
            if (logEvent)
                logger.logToFile("  display_growth_rates deactivated.", LOG_FILE);
            return;
        }

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
                logger.logToFile("Player " + ePlayer.getName() + " lacks 'rpg.info.interact' permission.", LOG_FILE);
            return;
        }

        // Use a BlockState to "change" the block type without actually changing the block type in the world
        BlockState eventBlockState = eventBlock.getBlockData().createBlockState();
        Material plantMaterial = versionMapper.getMaterialFromSeed(e.getMaterial());

        // getMaterialFromSeed is nullable
        if (plantMaterial == null) {
            if (logEvent)
                logger.logToFile("Could not retrieve plant material from seed material: " + e.getMaterial(), LOG_FILE);
            return;
        }

        // Add a cooldown to stop click spamming, preventing unnecessary heavy area calculations
        Long lastTime = playerCooldownMap.get(ePlayer.getUniqueId());
        long currentTime = System.currentTimeMillis();

        // Cooldown in milliseconds
        int cooldown = cm.getDisplay_cooldown() * 1000;
        if (lastTime != null) {
            if (logEvent) {
                logger.logToFile("  Last interact time: " + lastTime, LOG_FILE);
                logger.logToFile("  Current time: " + currentTime, LOG_FILE);
                logger.logToFile("  Cooldown: " + cooldown + " ms", LOG_FILE);
            }

            if ((currentTime - lastTime) < cooldown) {
                if (logEvent)
                    logger.logToFile("  PlayerInteractEvent triggered during cooldown period.", LOG_FILE);
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
            logger.logToFile("  All pre-checks passed.", LOG_FILE);
            logger.logToFile("  Seed material in player's hand: " + e.getMaterial(), LOG_FILE);
            logger.logToFile("  Material derived from seed: " + plantMaterial, LOG_FILE);
        }

        // Check if the plant's growth is modified by the plugin
        if (!versionMapper.isGrowthModifiedPlant(plantMaterial)) {
            if (logEvent)
                logger.logToFile("  Vanilla behavior for plant material: " + plantMaterial, LOG_FILE);

            // Send a message to the player
            msgManager.sendLocalizedMsg(ePlayer, MessageType.PLANT_NOT_MODIFIED_MSG,
                    PLANT_PLACEHOLDER, plantMaterial.toString().toLowerCase(), true);

            return;
        }

        // Temporarily set the event block state to the plant material
        eventBlockState.setType(plantMaterial);

        if (logEvent)
            logger.logToFile("  Growth Modifier Data:", LOG_FILE);

        // Calculate the surrounding environment's effect on the plant's growth
        Surrounding surrounding = SpecialBlockSearch.get().surroundingOf(eventBlock, eventBlockState);

        double growthRate = surrounding.getGrowthRate();
        double deathChance = surrounding.getDeathChance();

        if (logEvent) {
            logger.logToFile("    Growth rate: " + growthRate, LOG_FILE);
            logger.logToFile("    Death chance: " + deathChance, LOG_FILE);
            logger.logToFile("    Biome: " + surrounding.getBiome(), LOG_FILE);
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
                surrounding.getBiome().toLowerCase(),
                surrounding.isInValidBiome(),
                surrounding.usedFertilizer(),
                surrounding.hasUVLightAccess(),
                versionMapper.getMaterialMapper().canGrowInDark(plantMaterial),
                surrounding.isInDarkness()
        );

        // Send the localized message to the player
        msgManager.sendLocalizedMsg(ePlayer, MessageType.GROWTH_RATE_MSG, placeholders, replacements, true);
    }

    public void onComposterFillEvent(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        final Player player = e.getPlayer();
        final Block block = e.getClickedBlock();
        final Material heldMaterial = e.getMaterial();

        if (block.getType() != Material.COMPOSTER) return;

        // Prevent bonemeal output if configured and returns if composter is full
        if (!handleComposterBonemealOutput(block)) {
            e.setCancelled(true);
            return;
        }

        if (e.getItem() == null) return;

        // Handle single bonemeal input
        if (!player.isSneaking() && heldMaterial == Material.BONE_MEAL && cm.isComposterBonemealInputAllowed()) {
            handleComposterBonemealInput(e);
            return;
        }

        // Handle quick fill
        if (player.isSneaking() && cm.isComposterQuickFillEnabled()) {
            handleComposterQuickFill(e);
        }
    }

    private void handleComposterQuickFill(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final Block composterBlock = e.getClickedBlock();
        final ItemStack itemsInHand = e.getItem();
        final Material heldMaterial = e.getMaterial();
        final float compostChance = getCompostChance(heldMaterial);

        if (compostChance <= 0.0f) {
            if (logEvent) logger.logToFile("  Item '" + heldMaterial + "' is not compostable.", LOG_FILE);
            return;
        }

        assert composterBlock != null;
        final Levelled composterData = (Levelled) composterBlock.getBlockData();
        final int currentLevel = composterData.getLevel();
        final int maxLevel = composterData.getMaximumLevel() - 1;

        if (currentLevel >= maxLevel) return;

        assert itemsInHand != null;
        final int itemsAvailable = itemsInHand.getAmount();
        final int neededSuccesses = maxLevel - currentLevel;

        int compostSuccesses = 0;
        int itemsConsumed = 0;

        while (itemsConsumed < itemsAvailable && compostSuccesses < neededSuccesses) {
            if (randomNumberGenerator.nextFloat() <= compostChance) {
                compostSuccesses++;
            }
            itemsConsumed++;
        }

        assert itemsAvailable >= itemsConsumed :
                "Consumed more items than available. ItemsAvailable=" + itemsAvailable + ", Consumed=" + itemsConsumed;

        if (logEvent) {
            logger.logToFile("  Material: " + heldMaterial, LOG_FILE);
            logger.logToFile("  Compost chance: " + compostChance, LOG_FILE);
            logger.logToFile("  Compost successes applied: " + compostSuccesses, LOG_FILE);
            logger.logToFile("  Items consumed: " + itemsConsumed, LOG_FILE);
        }

        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            assert e.getHand() != null;
            removeItemsFromPlayerInventory(player, e.getHand(), itemsInHand, itemsConsumed);
        }

        updateComposterLevel(composterBlock, composterData, currentLevel + compostSuccesses);
        e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  Composter at " + composterBlock.getLocation() +
                    " quick-filled to level " + (currentLevel + compostSuccesses) + ".", LOG_FILE);
        }
    }

    private void handleComposterBonemealInput(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final Block composterBlock = e.getClickedBlock();
        assert composterBlock != null;

        final Levelled composterData = (Levelled) composterBlock.getBlockData();

        if (composterData.getLevel() >= composterData.getMaximumLevel() - 1) return;

        final int currentLevel = composterData.getLevel();
        final int newLevel = currentLevel + 1;

        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            assert e.getItem() != null;
            assert e.getHand() != null;
            removeItemsFromPlayerInventory(player, e.getHand(), e.getItem(), 1);
        }

        updateComposterLevel(composterBlock, composterData, newLevel);
        e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  Bonemeal applied to composter. New level: " + newLevel, LOG_FILE);
        }
    }

    /**
     * Prevents players from extracting bonemeal from a full composter if disabled in config.
     * @return true if interaction is allowed, false if blocked
     */
    private boolean handleComposterBonemealOutput(Block composterBlock) {
        if (cm.isComposterBonemealOutputDisabled()) {
            final Levelled composterData = (Levelled) composterBlock.getBlockData();
            final boolean isFull = composterData.getLevel() >= composterData.getMaximumLevel();
            if (isFull) {
                if (logEvent) {
                    logger.logToFile("  Bonemeal output is disabled. Prevented player from extracting.", LOG_FILE);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Removes a specified number of items from the player's inventory hand.
     *
     * @param player          The player whose inventory is being modified.
     * @param playerHand      The hand (main or off) holding the item stack.
     * @param itemStackInHand The item stack currently in the player's hand.
     * @param itemsConsumed   The number of items to consume from the stack.
     *
     * @throws AssertionError If attempting to consume more items than are present.
     */
    private void removeItemsFromPlayerInventory(
            @NotNull Player player,
            @NotNull EquipmentSlot playerHand,
            @NotNull ItemStack itemStackInHand,
            int itemsConsumed
    ) {
        int currentAmount = itemStackInHand.getAmount();

        // Ensure we do not consume more items than are available
        assert itemsConsumed <= currentAmount :
                "Tried to consume more items than the player is holding! Requested: " + itemsConsumed + ", Available: " + currentAmount;

        int remaining = currentAmount - itemsConsumed;

        if (remaining <= 0) {
            // Remove the item stack entirely
            player.getInventory().setItem(playerHand, null);
            player.updateInventory(); // Only needed when removing the item completely

            if (logEvent) {
                logger.logToFile("  All items in hand were consumed.", LOG_FILE);
                logger.logToFile("  -> ItemStack removed from player inventory.", LOG_FILE);
            }
        } else {
            // Update the item stack with the reduced amount
            itemStackInHand.setAmount(remaining);
            // No need to call updateInventory(), Minecraft handles partial updates
        }
    }

    /**
     * Updates the composter block to a new level, plays sound, and spawns particles.
     *
     * @param composterBlock      The composter block to update.
     * @param composterLevelData  The block data representing the current composter level.
     * @param newComposterLevel   The new level to set for the composter (must be within valid bounds).
     *
     * @throws AssertionError If the new level exceeds the maximum allowed composter level.
     */
    private void updateComposterLevel(
            @NotNull Block composterBlock,
            @NotNull Levelled composterLevelData,
            int newComposterLevel
    ) {
        int maxLevel = composterLevelData.getMaximumLevel();

        // Ensure the new level is valid
        assert newComposterLevel <= maxLevel - 1 :
                "New composter level exceeds maximum allowed level! Max: " + (maxLevel - 1) + ", Given: " + newComposterLevel;

        // Update block data
        composterLevelData.setLevel(newComposterLevel);
        composterBlock.setBlockData(composterLevelData);

        Location blockCenter = composterBlock.getLocation().add(0.5, 0.5, 0.5);

        // Play composting success sound
        composterBlock.getWorld().playSound(
                blockCenter,
                Sound.BLOCK_COMPOSTER_FILL_SUCCESS,
                1.0f,
                1.0f
        );

        // Spawn happy villager particles to indicate success
        composterBlock.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                blockCenter.clone().add(0, -0.4 + 0.15 * newComposterLevel, 0),
                3,
                0.15, 0.25, 0.15
        );
    }

    private float getCompostChance(Material material) {
        if (material.isCompostable()) return material.getCompostChance();
        if (material == Material.BONE_MEAL && cm.isComposterBonemealInputAllowed()) return 1.0f;
        return -1.0f;
    }

    private void logEventHeader(PlayerInteractEvent e) {
        if (logEvent) {
            logger.logToFile("", LOG_FILE);
            logger.logToFile("-------------------- Player Interact Event --------------------", LOG_FILE);
            logger.logToFile("  Player: " + e.getPlayer().getName(), LOG_FILE);
            logger.logToFile("  Player location: " + e.getPlayer().getLocation(), LOG_FILE);
        }
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