package de.nightevolution.realisticplantgrowth.plant;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public class PlantData {

    // TODO: Don't use Material for Plants
    private Material plantMaterial;
    private List<String> validBiomeGroups;

    // Default can also be a biomeGroup
    //private GrowthModifier defaultModifier;
    //private NamespacedKey defaultValidBiomes;

}
