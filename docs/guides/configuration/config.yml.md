# Config.yml

{% file src="../../.gitbook/assets/Config.yml" %}
Default Config File
{% endfile %}

## General Settings

### language\_code
#### default: _en-GB_

Language setting for the user plugin messages. \
The language files are located in your server's "plugins" directory: \
`plugins/RealisticPlantGrowth/local`

Upon the first plugin start, all supported languages for your version should be available in this directory. \
If you wish to change the language, you'll need to identify and select the `.yml` file with the desired language. 
You also have the option to create your own language files and use them .

In cases where specific translations are missing, the default strings from the default language (`en-GB`) will be automatically selected.

### plugin\_prefix
#### default: _'\[RealisticPlantGrowth] '_

Plugin prefix for console logs.
Does not affect chat messages.\
You can customize the prefix using miniMessage format and legacy &-color codes.\
For details, refer to: [MiniMessage Format](https://docs.advntr.dev/minimessage/format.html)

### enabled\_worlds
#### default:
- your\_world
- your\_world\_nether
- your\_world\_end

List of worlds where this plugin is active. \
To customize, modify this list to include your desired worlds. \
Any worlds not listed here will use the default growth behavior.

### bonemeal\_limit
#### default: _3_

Limits the use of bonemeal on plants to the specified amount. \
Setting 'bonemeal\_limit: 0' completely disables the use of bone meal. \
Negative numbers deactivate this feature (e.g., 'bonemeal\_limit: -1').

{% hint style="info" %}
`bonemeal_limit` not implemented yet.
{% endhint %}

### min\_natural\_light
#### default: _12_

Required natural sky light level for plant growth.&#x20;

| SkyLightLevel | Description                                                              |
| ------------- | ------------------------------------------------------------------------ |
| 0             | Allows underground plant growth.                                         |
| 12            | <p>corresponds to sunlight <br>filtered through leaves.</p>              |
| 15            | <p>plants only grow in direct sunlight. <br>(Allows glass ceilings.)</p> |

### destroy\_farmland
#### default: _true_

When a player harvests a plant that has grown on farmland, the farmland is automatically replaced with a coarse dirt block. \
Consequently, the player is required to re-hoe the area to prepare it for seeding new plants. \
Setting '`destroy_farmland`' to '`true`' enables this feature.

### require_hoe
#### default: true

Determines whether a player needs a hoe to harvest plants. 
If set to `true`, players must use a hoe for harvesting.\
If `require_hoe` is set to 'true' and a plant is harvested without a hoe, the plant will not drop anything.

### display_growth_rates
#### default: true

Displays potential growth rates for an item in hand when a player left-clicks the ground.\
Only shows growth rates for the biome the player is currently in.

### display_cooldown
#### default: 3

Only active, if `display_growth_rates` is enabled.\
Time in seconds before the growth rates are shown to the player again.\
Prevents spam clicking and therefore heavy area scans.


## Fertilizer

Fertilizer enables Composters to fertilize crops in the surrounding area, allowing for growth in normally uninhabitable areas.

### fertilizer_enabled
#### default: false

Enables the use of composters to fertilize crops in the surrounding area.

### fertilizer_radius
#### default: 10

Plants within this block radius will grow at the `fertilizer_growth_rate` (see below).\
Higher values may increase server resource consumption and cause lag (Maximum 15 blocks).

### fertilizer_passiv
#### default: true

`true`: Fertilizer effects do not deplete composters to fertilize the surroundings.\
`false`: Players must actively fill the composter to achieve fertilization effects.

### fertilizer_boost_growth_rate
#### default: 20.0

The `fertilizer_boost_growth_rate` is a ~~percentage~~ value added to biome-specific growth rates,
if a composter is within the configured `fertilizer_radius`.\
For example, a biome-specific growth rate of wheat in a desert is 20%. A 'fertilizer_boost_growth_rate' of 25.0%
results in a growth rate of 45.0% for wheat plants.

### fertilizer_allow_growth_rate_above_100
#### default: false

Allows a growth rate above 100.0%.\
If a plant has a biome-specific growth rate of 100.0% and this is set to be true,
the resulting growth rate can exceed 100.0% (e.g., 125.0%).
Otherwise, the growth rate is capped at 100.0%.

{% hint style="info" %}
`fertilizer_allow_growth_rate_above_100` not implemented yet.
{% endhint %}


### fertilizer_enables_growth_in_invalid_biomes
#### default: false

If set to `true`, Fertilizer allows plants to thrive in invalid biomes. \
If set to `false`, plant growth is limited to biomes explicitly listed in `Biome: []` section within `GrowthModifiers.yml`.

### fertilizer_invalid_biome_growth_rate
#### default: 35.0

Growth rate of plants in invalid biomes with fertilizer access.\
Only active if `fertilizer_enables_growth_in_invalid_biomes` is set to `true`,
and the plant is in an invalid biome.

### fertilizer_invalid_biome_death_chance
#### default: 25.0

Probability of plant death in an invalid biome despite fertilizer supply.\
This rule is effective only when `fertilizer_enables_growth_in_invalid_biomes`
is set to `true` and the plant is located in an inappropriate biome.

If the Growth process of the plant consists of multiple growth stages, 
the death rate is internally adjusted by dividing it by the number of growth stages.


## UV-Light

Artificial light source that allows plants within its radius to grow where the sky light level is lower
than the `min_natural_light` value.\
This allows the cultivation of plants underground or in buildings without direct sky light access.\

{% hint style="info" %}
**Note:** Plants still require the minimal vanilla light level to grow.
{% endhint %}

### uv_enabled
#### default: false

Enable the use of UV-Light Blocks.

### uv_blocks
#### default:
- OCHRE_FROGLIGHT
- PEARLESCENT_FROGLIGHT
- VERDANT_FROGLIGHT

List of blocks that can function as UV-Light sources.\
Use the official [MATERIAL_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.

{% hint style="info" %}
**Hint:** `uv_blocks` don't need to emit light, but when they do, 
you don't require an additional light source.
{% endhint %}


### require_all_uv_blocks
#### default: false

Determines whether a plant requires all UV-Light Blocks listed above to grow.
- `true`: All blocks in `uv_blocks` are necessary for plant growth.
- `false`: Only one of the blocks listed in `uv_blocks` is required for plant growth.

### uv_radius
#### default: 7

Radius around a UV-Light block that affects growth.\
Higher values consume more server resources and may lead to server lag. _(Maximum 15 blocks)_\
Use the official [MATERIAL_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.
### grow_in_dark
#### default:
- RED_MUSHROOM
- BROWN_MUSHROOM
- NETHER_WART
- COCOA

List of plants that grow without requiring natural sky light, allowing growth
in artificial light sources like torches and lanterns _(not the same as UV-Light)_.\
Use the official [MATERIAL_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.



## Sounds & Effects
Configure played sounds or particle effects used by this plugin.

### plant_death_sound_effect
#### defaults:
- enabled: 
- sound: 
- volume: 
- pitch: 
- effect: 
- data: 

## Logging & Debugging
Provides various debug logs to offer additional plugin data for troubleshooting and configuration.


### debug_log
#### default: false
Enables Debug-Mode. Can get spammy, use for only for debugging.

### tree_log
#### default: false
Logs Tree Growing Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}

### plant_log
#### default: false
Logs Plant Growing Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}

### bonemeal_log
#### default: false
Logs Bonemeal Usage Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}
