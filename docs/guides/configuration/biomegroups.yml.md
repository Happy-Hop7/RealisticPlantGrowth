---
description: Configuration Guide for Biome Groups.
---

# 🌅 BiomeGroups.yml

{% embed url="https://github.com/Happy-Hop7/RealisticPlantGrowth/blob/master/src/main/resources/BiomeGroups.yml" %}
Default BiomeGroups.yml
{% endembed %}

***

## Configuration Guide for Biome Groups

```yaml
# Example Biome group:

Arid:
  - BADLANDS
  - DESERT
  - ERODED_BADLANDS
  - WOODED_BADLANDS
```



## Steps to Create Your Own Biome Group:

### 1. Choose a Group Name:

Decide on a name for your biome group. \
This name is arbitrary and can be anything that helps you identify the group.

```yaml
GroupName:
# GroupName can be arbitrary chosen.
```



### 2. List the Biomes:

Under the group name, list the biomes that belong to this group.&#x20;

```yaml
GroupName:
  - BIOME1
  - BIOME2
  - BIOME3
```

{% hint style="info" %}
Currently, only official Minecraft biomes listed [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html) are supported.\
Support for custom biomes from other plugins and datapacks will be added in future updates.
{% endhint %}

***
