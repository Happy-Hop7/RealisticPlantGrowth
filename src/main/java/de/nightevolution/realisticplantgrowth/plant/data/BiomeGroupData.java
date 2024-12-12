package de.nightevolution.realisticplantgrowth.plant.data;

import de.nightevolution.realisticplantgrowth.RealisticPlantGrowth;
import de.nightevolution.realisticplantgrowth.utils.Logger;
import de.nightevolution.realisticplantgrowth.utils.enums.ModifierType;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BiomeGroupData {
    private final String groupId;
    private final boolean defaultGroup;
    private final boolean greenhouseRequired;
    private final List<GrowthRates> growthRates;
    private final List<World> worlds;
    private final List<String> applyTo;


    public BiomeGroupData(@NotNull String groupId, boolean defaultGroup, boolean greenhouseRequired,
                          @NotNull List<GrowthRates> growthRates, @NotNull List<String> worlds,
                          @NotNull List<String> applyTo) throws IllegalArgumentException {
        this.groupId = groupId;
        this.defaultGroup = defaultGroup;
        this.greenhouseRequired = greenhouseRequired;
        this.growthRates = growthRates;
        this.applyTo = applyTo;

        try {
            this.worlds = checkWorlds(worlds);
            checkGrowthRates();
        } catch (IllegalArgumentException e) {
            String biomeGroupType = defaultGroup ? "default" : "custom_groups";
            Logger logger = new Logger(BiomeGroupData.class.getSimpleName(), false, false);
            logger.error("Detected an error in your GrowthModifiers.yml!");
            logger.error("group_id: " + groupId + ", of " + biomeGroupType);
            throw e;
        }
    }

    private List<World> checkWorlds(List<String> worldNames) {
        RealisticPlantGrowth instance = RealisticPlantGrowth.getInstance();
        String biomeGroupType = defaultGroup ? "default" : "custom_groups";
        Logger logger = new Logger(BiomeGroupData.class.getSimpleName(), false, false);
        List<World> checkedWorlds = new ArrayList<>(worldNames.size());
        List<World> realWorlds = instance.getServer().getWorlds();
        for (String world : worldNames) {

        }
        return null;
    }

    private void checkGrowthRates() {
        if (growthRates.size() != 2) {
            throw new IllegalArgumentException("Expected 2 GrowthRates, but got: " + growthRates.size());
        }

        boolean naturalGrowthRates = false;
        boolean uvLightGrowthRates = false;

        for (GrowthRates gr : growthRates) {
            switch (gr.getModifierType()) {
                case NATURAL -> naturalGrowthRates = true;
                case UV_LIGHT -> uvLightGrowthRates = true;
                default -> throw new IllegalArgumentException("Provided unexpected ModifierType '" + gr.getModifierType() + "' for GrowthRates: " + gr);
            }
        }

        if (naturalGrowthRates && uvLightGrowthRates)
            return;
        else
            throw new IllegalArgumentException("Expected NATURAL and UV_LIGHT GrowthRates: " );

    }


    // Getters and setters
    public String getGroupId() {
        return groupId;
    }

    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    public boolean isGreenhouseRequired() {
        return greenhouseRequired;
    }

    public GrowthRates getGrowthRates(ModifierType modifierType) {
        return switch (modifierType) {
            case NATURAL -> growthRates.getFirst();//naturalGrowthRates.getValue();
            //case UV_LIGHT -> uvLightGrowthRates.getValue();
            default -> throw new IllegalArgumentException("Unknown ModifierType: " + modifierType);
        };
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public List<String> getApplyTo() {
        return applyTo;
    }

    public boolean appliesToWorld(String worldToTest) {
//        for (String world : worlds) {
//            if (world.equalsIgnoreCase("ALL")) {
//                return true;
//            }
//            if (world.equalsIgnoreCase(worldToTest)) {
//                return true;
//            }
//        }

        return false;
    }

}
