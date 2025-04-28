package de.nightevolution.realisticplantgrowth.listeners.player;

import de.nightevolution.realisticplantgrowth.ConfigManager;
import de.nightevolution.realisticplantgrowth.MessageManager;
import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.enums.MessageType;
import de.nightevolution.realisticplantgrowth.utils.enums.PlaceholderInterface;
import de.nightevolution.realisticplantgrowth.utils.mapper.VersionMapper;
import de.nightevolution.realisticplantgrowth.utils.plant.SpecialBlockSearch;
import de.nightevolution.realisticplantgrowth.utils.plant.Surrounding;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;

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
    private final Random randomNumberGenerator;
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

        if (RealisticPlantGrowth.isDebug())
            randomNumberGenerator = new Random(1);
        else {
            randomNumberGenerator = new Random();
        }

        logger.verbose("Registered new " + this.getClass().getSimpleName() + ".");
    }

    /**
     * Handles player interactions with blocks, specifically when holding clickable seeds.
     *
     * @param e The PlayerInteractEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEventWithClickableSeeds(PlayerInteractEvent e) {

        logEvent(e);

        // Check if growth rate display is enabled
        if (!cm.isDisplay_growth_rates()) {
            if (logEvent)
                logger.logToFile("  display_growth_rates deactivated.", logFile);
            return;
        }

        if (instance.isWorldDisabled(e.getPlayer().getWorld())) {
            if (logEvent) {
                logger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
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
                if (logEvent)
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
            if (logEvent)
                logger.logToFile("  Vanilla behavior for plant material: " + plantMaterial, logFile);

            // Send a message to the player
            msgManager.sendLocalizedMsg(ePlayer, MessageType.PLANT_NOT_MODIFIED_MSG,
                    PLANT_PLACEHOLDER, plantMaterial.toString().toLowerCase(), true);

            return;
        }

        // Temporarily set the event block state to the plant material
        eventBlockState.setType(plantMaterial);

        if (logEvent)
            logger.logToFile("  Growth Modifier Data:", logFile);

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

    /**
     * Handles quick fill interactions with Composter blocks.
     * <p>
     * When a player shift-right-clicks a Composter while holding a compostable item,
     * all items needed to fully fill the composter will be instantly consumed from the player's hand,
     * based on the vanilla compostable chances.
     * <p>
     * If the amount of items in the player's hand is insufficient to fully fill the composter,
     * the composter will be partially filled according to the number of successful compost actions,
     * and all items will be consumed.
     * <p>
     * This bypasses the normal, gradual composting process.
     * <p>
     * Preconditions:<br>
     * - Quick fill feature must be enabled in {@code Config.yml}.<br>
     * - The world must not be disabled for this plugin.<br>
     * - The player must be sneaking and holding a compostable item.<br>
     *
     * @param e The {@link PlayerInteractEvent} triggered when the player interacts with a block.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onComposterFillEvent(PlayerInteractEvent e) {

        logEvent(e);

        // Check if quick fill feature is enabled
        if (!cm.getShiftComposterFill()) {
            if (logEvent)
                logger.logToFile("  Quick fill of composters is disabled.", logFile);
            return;
        }

        // Check if the event occurred in a disabled world
        if (instance.isWorldDisabled(e.getPlayer().getWorld())) {
            if (logEvent) {
                logger.logToFile("  -> World is disabled for RealisticPlantGrowth.", logFile);
            }
            return;
        }

        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // Validate interaction: right-click on block, holding an item
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || clickedBlock == null || e.getItem() == null) {
            return;
        }

        // Check if the clicked block is a composter
        if (clickedBlock.getType() != Material.COMPOSTER) {
            return;
        }

        // Check if player is sneaking
        if (!player.isSneaking()) {
            return;
        }

        // Holds compost chance for the held material
        float compostChance;

        // Check if the held item is compostable or if bonemeal can be used in composters
        if (e.getMaterial().isCompostable()) {
            compostChance = e.getMaterial().getCompostChance();

        } else if (cm.getAllowBonemealInComposters() && e.getMaterial().equals(Material.BONE_MEAL)) {
            compostChance = 1.0f;

            if (logEvent) {
                logger.logToFile("  Using Bonemeal in Composter.", logFile);
            }

        } else {
            if (logEvent) {
                logger.logToFile("  Item '" + e.getMaterial() + "' is not compostable.", logFile);
            }
            return;
        }


        if (compostChance <= 0 || compostChance > 1.0) {
            if (logEvent) {
                logger.error("  Invalid compost chance for item '" + e.getMaterial() + "'.");
            }
            return;
        }


        // Get the current composter fill level and maximum level
        Levelled composterLevel = (Levelled) clickedBlock.getBlockData();
        int currentLevel = composterLevel.getLevel();
        int maxLevel = composterLevel.getMaximumLevel()-1;
        int neededSuccesses = maxLevel - currentLevel;

        ItemStack itemsInHand = e.getItem();
        int numberOfItemsInHand = itemsInHand.getAmount();
        int appliedSuccesses = 0;
        int itemsConsumed = 0;

        // Calculate how many items would be consumed and how many compost successes would be achieved
        // without actually modifying the composter or the player's inventory yet.
        while (itemsConsumed < numberOfItemsInHand && (currentLevel + appliedSuccesses) < maxLevel) {
            assert appliedSuccesses <= neededSuccesses : "Error: Applied more compost successes than needed!";

            if (randomNumberGenerator.nextFloat() <= compostChance) {
                // Compost succeeded
                appliedSuccesses++;
            }

            itemsConsumed++;
        }

        if (logEvent) {
            logger.logToFile("  Compost chance for '" + e.getMaterial() + "': " + compostChance, logFile);
            logger.logToFile("  Successes needed to fully fill composter: " + neededSuccesses, logFile);
            logger.logToFile("  Successful compost actions applied: " + appliedSuccesses, logFile);
        }

        // Validate inventory state after composting
        assert e.getItem() != null : "Item in Player hand not found!";
        assert numberOfItemsInHand >= itemsConsumed : "Composter Consumed Items is higher than items in player hand!";

        if (itemsInHand.getAmount() - itemsConsumed == 0) {
            // Remove item from hand
            assert e.getHand() != null;
            player.getInventory().setItem(e.getHand(), null);

            // Update the inventory to reflect the change (only necessary when the item is fully removed)
            player.updateInventory();

            if (logEvent) {
                logger.logToFile("  All items in hand were consumed.", logFile);
                logger.logToFile("  -> ItemStack removed from player inventory.", logFile);
            }
        } else {
            // Reduce item amount without removing the item
            e.getItem().setAmount(itemsInHand.getAmount() - itemsConsumed);
            // No need to call updateInventory() here, Minecraft handles it
        }


        // Instantly update composter fill level
        int newComposterLevel = currentLevel + appliedSuccesses;
        assert newComposterLevel <= maxLevel : "Error: New composter level exceeds maximum allowed!";
        composterLevel.setLevel(newComposterLevel);

        // Update the block data and apply changes to the world
        clickedBlock.setBlockData(composterLevel);

        // Cancel normal composting behavior
        e.setCancelled(true);

        if (logEvent) {
            logger.logToFile("  Composter at " + clickedBlock.getLocation() + " was quick-filled to level " + newComposterLevel + ".", logFile);
        }
    }


    private void logEvent(PlayerInteractEvent e) {
        if (logEvent) {
            logger.logToFile("", logFile);
            logger.logToFile("-------------------- Player Interact Event --------------------", logFile);
            logger.logToFile("  Player: " + e.getPlayer().getName(), logFile);
            logger.logToFile("  Player location: " + e.getPlayer().getLocation(), logFile);
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