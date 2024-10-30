package de.nightevolution.realisticplantgrowth.utils.biome;

import org.bukkit.NamespacedKey;

import java.util.Set;

public record BiomeGroup (String name, Set<NamespacedKey> key, GrowthModifier modifier){}
