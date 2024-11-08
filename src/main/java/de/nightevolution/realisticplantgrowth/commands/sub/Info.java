package de.nightevolution.realisticplantgrowth.commands.sub;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.biome.BiomeChecker;
import de.nightevolution.realisticplantgrowth.utils.enums.MessageType;
import de.nightevolution.realisticplantgrowth.utils.enums.PlaceholderInterface;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Subcommand class for the 'info' command, providing information about plant growth.
 */
public class Info extends SubCommand implements PlaceholderInterface {

    private Material plantMaterial;
    private Material seedMaterial;
    private Material notGrowthModifiedSeed;
    private NamespacedKey biomeKey;

    /**
     * Constructor for the 'info' subcommand.
     *
     * @param commandSender The sender of the command.
     * @param args          The arguments passed with the command.
     * @param instance      The main plugin instance.
     */
    public Info(CommandSender commandSender, String[] args, RealisticPlantGrowth instance) {
        super(commandSender, args, instance);
        permission = new Permission("rpg.info");
    }

    /**
     * Executes the 'info' command, providing information about plant growth.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand() {
        if (!super.executeCommand()) {
            return false;
        }

        // Ensure the command is executed by a player in-game
        if (!(commandSender instanceof Player player)) {
            msgManager.sendLocalizedMsg(commandSender, MessageType.NO_CONSOLE, false);
            return false;
        }

        World world = player.getWorld();

        // Check if the world is enabled for plant growth modification
        if (instance.isWorldDisabled(world)) {
            return false;
        }

        // Check if the player is holding a growth-modified plant seed
        if (!isPlayerHoldingAGrowthModifiedPlantSeed(player)) {
            if (notGrowthModifiedSeed != null) {
                msgManager.sendLocalizedMsg(player, MessageType.PLANT_NOT_MODIFIED_MSG,
                        PLANT_PLACEHOLDER, notGrowthModifiedSeed.toString().toLowerCase(), true);
            } else {
                msgManager.sendLocalizedMsg(player, MessageType.INFO_CMD_NO_ITEM, false);
            }
            return false;
        }

        Location playerLocation = player.getLocation();
        BiomeChecker bc;

        // New custom biome handling
        if (instance.isPaperFork()) {
            biomeKey = Bukkit.getUnsafe().getBiomeKey(player.getWorld(), playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
             bc = new BiomeChecker(plantMaterial, biomeKey);
        } else {
            // Check the biomes where the plant can grow
            bc = new BiomeChecker(plantMaterial, playerLocation.getBlock().getBiome());
        }

        String defaultBiomesList = formatBiomeList(bc.getDefaultBiomes());

        // Prepare placeholders and replacements for the message
        List<String> placeholders = Arrays.asList(
                PLANT_PLACEHOLDER,
                CAN_GROW_IN_DARK_PLACEHOLDER,
                BIOME_GROUP_LIST_PLACEHOLDER,
                BIOME_LIST_PLACEHOLDER
        );

        List<Object> replacements = Arrays.asList(
                seedMaterial.toString().toLowerCase(),
                mapper.getMaterialMapper().canGrowInDark(plantMaterial),
                bc.getBiomeGroupStringList().toString(),
                defaultBiomesList
        );

        // Logging the details if verbose mode is enabled
        if (logEvent) {
            superLogger.logToFile("    Plant: " + plantMaterial, logFile);
            superLogger.logToFile("    Seed: " + seedMaterial, logFile);
            superLogger.logToFile("    CanGrowInDark: " + mapper.getMaterialMapper().canGrowInDark(plantMaterial), logFile);
            superLogger.logToFile("    BiomeGroups: " + bc.getBiomeGroupStringList(), logFile);
            superLogger.logToFile("    BiomeList: " + defaultBiomesList, logFile);
        }

        // Send the localized message to the player
        msgManager.sendLocalizedMsg(player, MessageType.INFO_CMD_RESULT, placeholders, replacements, true);

        return true;
    }

    /**
     * Checks if the {@link Player} is holding a plant seed in their hand.
     *
     * @param player The {@link Player} to check.
     * @return True if the {@link Player} is holding a plant seed, false otherwise.
     */
    private boolean isPlayerHoldingAGrowthModifiedPlantSeed(@NotNull Player player) {
        Material mainHand = player.getInventory().getItemInMainHand().getType();
        Material offHand = player.getInventory().getItemInOffHand().getType();
        Material mainHandSeed;
        Material offHandSeed;
        notGrowthModifiedSeed = null;

        // Check if the player is holding a clickable seed in their main or off hand
        if (mapper.isClickableSeed(mainHand)) {
            mainHandSeed = mainHand;
        } else {
            mainHandSeed = Material.AIR;
        }

        if (mapper.isClickableSeed(offHand)) {
            offHandSeed = offHand;
        } else {
            offHandSeed = Material.AIR;
        }

        // If the player is not holding any clickable seed, return false
        if (mainHandSeed == Material.AIR && offHandSeed == Material.AIR) {
            return false;
        }

        // Check if the seed corresponds to a growth-modified plant
        for (Material material : Arrays.asList(mainHandSeed, offHandSeed)) {
            Material tempPlantMaterial = mapper.getMaterialFromSeed(material);

            if (tempPlantMaterial == null) {
                continue;
            }

            if (mapper.isGrowthModifiedPlant(tempPlantMaterial)) {
                plantMaterial = tempPlantMaterial;
                seedMaterial = material;
                return true;
            } else if (mapper.isAPlant(tempPlantMaterial)) {
                notGrowthModifiedSeed = material;
            }
        }

        return false;
    }

    /**
     * Formats a list of biomes for display in messages.
     *
     * @param biomeList The list of biomes to format.
     * @return The formatted list as a string.
     */
    private String formatBiomeList(@NotNull List<String> biomeList) {
        StringBuilder builder = new StringBuilder();

        // If the list is empty, return an empty bracket
        if (biomeList.isEmpty()) {
            return "[]";
        }

        // If the list contains only 'ALL', return it as is
        if (biomeList.size() == 1 && biomeList.getFirst().equalsIgnoreCase("ALL")) {
            return biomeList.getFirst();
        }

        builder.append("<newline>");

            for (String element : biomeList) {

                String[] fragmentedString = element.split(":");
                if (fragmentedString.length == 1) { // if length == 1 -> Not a custom biome
                    builder.append("     - ")
                            .append("<lang:biome.minecraft.")
                            .append(element.toLowerCase())
                            .append(">")
                            .append("<newline>");
                } else {

                    builder.append("     - ")
                            .append(element)
                            .append("<newline>");
                }
            }

        return builder.toString();
    }
}
