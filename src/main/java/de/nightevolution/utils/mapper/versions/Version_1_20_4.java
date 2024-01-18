package de.nightevolution.utils.mapper.versions;

import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.Material;

public class Version_1_20_4 extends VersionMapper {


    public Version_1_20_4() {
        super();
        reload();
    }

    @Override
    public Material getGrassMaterial() {
        return Material.valueOf("SHORT_GRASS");
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

