package de.nightevolution.utils.mapper.versions;

import de.nightevolution.utils.mapper.VersionMapper;
import org.bukkit.Material;

public class Version_1_20 extends VersionMapper {
    public Version_1_20() {
        super();
        grassMaterial = Material.valueOf("GRASS");
        reload();
    }
}
