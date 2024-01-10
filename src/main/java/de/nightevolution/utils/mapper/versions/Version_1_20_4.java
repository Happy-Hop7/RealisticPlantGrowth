package de.nightevolution.utils.mapper.versions;

import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.Material;

public class Version_1_20_4 extends VersionMapper {


    public Version_1_20_4() {
        super();
        grassMaterial = Material.valueOf("SHORT_GRASS");
        reload();
    }
}
