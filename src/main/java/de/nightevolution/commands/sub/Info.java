package de.nightevolution.commands.sub;


import de.nightevolution.RealisticPlantGrowth;
import de.nightevolution.utils.biome.BiomeChecker;
import de.nightevolution.utils.enums.MessageType;
import de.nightevolution.utils.enums.PlaceholderInterface;
import org.bukkit.Material;
import org.bukkit.World;
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
        if (!super.executeCommand())
            return false;

        // Command can only be executed by a player In-Game.
        if (!(commandSender instanceof Player player)) {
            msgManager.sendLocalizedMsg(commandSender, MessageType.NO_CONSOLE, false);
            return false;
        }

        World world = player.getWorld();

        // Check if the world is enabled for plant growth modification
        if (instance.isWorldDisabled(world))
            return false;


        if (!isPlayerHoldingAGrowthModifiedPlantSeed(player)) {
            if (notGrowthModifiedSeed != null)
                msgManager.sendLocalizedMsg(player, MessageType.PLANT_NOT_MODIFIED_MSG,
                        PLANT_PLACEHOLDER, notGrowthModifiedSeed.toString().toLowerCase(), true);
            else
                msgManager.sendLocalizedMsg(player, MessageType.INFO_CMD_NO_ITEM, false);
            return false;
        }

        BiomeChecker bc = new BiomeChecker(plantMaterial, player.getLocation().getBlock().getBiome());

        String defaultBiomesList = formatBiomeList(bc.getDefaultBiomes());


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

        if (verbose) {
            superLogger.logToFile("  Plant: " + plantMaterial, logFile);
            superLogger.logToFile("  Seed: " + seedMaterial, logFile);
            superLogger.logToFile("  CanGrowInDark: " + mapper.getMaterialMapper().canGrowInDark(plantMaterial), logFile);
            superLogger.logToFile("  BiomeGroups: " + bc.getBiomeGroupStringList(), logFile);
            superLogger.logToFile("  BiomeList: " + defaultBiomesList, logFile);
        }

        msgManager.sendLocalizedMsg(player, MessageType.INFO_CMD_RESULT, placeholders, replacements, true);

        return true;
    }

    /**
     * Checks if the {@link Player} is holding a plant in their hand.
     *
     * @param player The {@link Player} to check.
     * @return True if the {@link Player} is holding a plant, false otherwise.
     */
    private boolean isPlayerHoldingAGrowthModifiedPlantSeed(@NotNull Player player) {
        Material mainHand = player.getInventory().getItemInMainHand().getType();
        Material offHand = player.getInventory().getItemInOffHand().getType();
        Material mainHandSeed;
        Material offHandSeed;
        notGrowthModifiedSeed = null;

        if (mapper.isClickableSeed(mainHand))
            mainHandSeed = mainHand;
        else
            mainHandSeed = Material.AIR;

        if (mapper.isClickableSeed(offHand))
            offHandSeed = offHand;
        else
            offHandSeed = Material.AIR;


        if (mainHandSeed == Material.AIR && offHandSeed == Material.AIR){
            return false;
        }

        for (Material material : Arrays.asList(mainHandSeed, offHandSeed)) {
            Material tempPlantMaterial = mapper.getMaterialFromSeed(material);

            if (tempPlantMaterial == null)
                continue;

            if (mapper.isGrowthModifiedPlant(tempPlantMaterial)) {
                plantMaterial = tempPlantMaterial;
                seedMaterial = material;
                return true;
            }
            else if (mapper.isAPlant(tempPlantMaterial)) {
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

        if (biomeList.isEmpty()) {
            return "[]";
        }

        if (biomeList.size() == 1 && biomeList.get(0).equalsIgnoreCase("ALL"))
            return biomeList.get(0);

        builder.append("<newline>");
        for (String element : biomeList) {
            builder.append("     - ")
                    .append("<lang:biome.minecraft.")
                    .append(element.toLowerCase())
                    .append(">")
                    .append("<newline>");
        }

        return builder.toString();
    }


}
