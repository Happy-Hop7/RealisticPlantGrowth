package de.nightevolution.realisticplantgrowth.utils.version.mapper.versions;

import de.nightevolution.realisticplantgrowth.utils.version.mapper.VersionMapper;
import org.bukkit.Material;

public class Version_1_20 extends VersionMapper {
    public Version_1_20() {
        super();
        reload();
    }

    @Override
    public Material getGrassMaterial() {
        return Material.valueOf("GRASS");
    }

    @Override
    public Material getPitcherPlacedMaterial() {
        return Material.valueOf("PITCHER_CROP");
    }

    @Override
    public Material getTorchflowerPlacedMaterial() {
        return Material.valueOf("TORCHFLOWER_CROP");
    }

    @Override
    public Material getTorchflowerMaterial() {
        return Material.valueOf("TORCHFLOWER");
    }
}
