# ⚙ Config.yml

Default Config:\
[https://github.com/Happy-Hop7/RealisticPlantGrowth/blob/master/src/main/resources/Config.yml](https://github.com/Happy-Hop7/RealisticPlantGrowth/blob/master/src/main/resources/Config.yml)\\

***

## General Settings

### language\_code

#### default: `en-US`

Language setting for the user plugin messages.\
The language files are located in your server's "plugins" directory:\
`plugins/RealisticPlantGrowth/local`

Upon the first plugin start, all supported languages for your version should be available in this directory.\
If you wish to change the language, you'll need to identify and select the `.yml` file with the desired language. You also have the option to create your own language files and use them .

In cases where specific translations are missing, the default strings from the default language\
(`en-US`) will be automatically selected.

***

### plugin\_prefix

#### default: `'[RealisticPlantGrowth] '`

Plugin prefix for console logs. Does not affect chat messages.\
You can customize the prefix using MiniMessage format and legacy &-color codes.\
For details, refer to: [MiniMessage Format](https://docs.advntr.dev/minimessage/format.html)

### enabled\_worlds

#### default:

* `your_world`
* `your_world_nether`
* `your_world_the_end`

List of worlds where this plugin is active.\
To customize, modify this list to include your desired worlds.\
Any worlds not listed here will use the default growth behavior.

***

### bonemeal\_limit

#### default: `3`

Limits the use of bonemeal on plants to the specified amount.\
Setting 'bonemeal\_limit: 0' completely disables the use of bone meal.\
Negative numbers deactivate this feature (e.g., 'bonemeal\_limit: -1').

{% hint style="info" %}
`bonemeal_limit` not implemented yet.
{% endhint %}

***

### min\_natural\_light

#### default: `15`

Required natural sky light level for plant growth.

| SkyLightLevel | Description                                                             |
| ------------- | ----------------------------------------------------------------------- |
| 0             | Allows underground plant growth.                                        |
| 12            | <p>corresponds to sunlight<br>filtered through leaves.</p>              |
| 15            | <p>plants only grow in direct sunlight.<br>(Allows glass ceilings.)</p> |

***

### destroy\_farmland

#### default: `true`

When a player harvests a plant that has grown on farmland, the farmland is automatically replaced with a coarse dirt block.\
Consequently, the player is required to re-hoe the area to prepare it for seeding new plants.\
Setting '`destroy_farmland`' to '`true`' enables this feature.

***

### require\_hoe

#### default: `true`

Determines whether a player needs a hoe to harvest plants. If set to `true`, players must use a hoe for harvesting.\
If `require_hoe` is set to 'true' and a plant is harvested without a hoe, the plant will not drop anything.

***

### display\_growth\_rates

#### default: `true`

Displays potential growth rates for an item in hand when a player left-clicks the ground.\
Only shows growth rates for the biome the player is currently in.

***

### display\_cooldown

#### default: `5`

Only active, if `display_growth_rates` is enabled.\
Time in seconds before the growth rates are shown to the player again.\
Prevents spam clicking and therefore heavy area scans.

***

### use\_metrics

#### default: `true`

When this configuration setting is set to true, your plugin will anonymously contribute usage data to bStats. bStats is a service that assists plugin developers in understanding how their creations are utilized in the wider community.

This anonymized information is invaluable for enhancing and optimizing plugins based on real-world usage patterns. For additional details, please visit the [bStats page](https://bstats.org/plugin/bukkit/Realistic%20Plant%20Growth/20634) of _**Realistic Plant Growth**_.

***

### check\_for\_updates

#### default: `true`

Set this option to `true` to enable automatic update checks when the server starts.\
Setting it to `false` will disable update checking.

***

## Fertilizer

Fertilizer enables Composters to fertilize crops in the surrounding area, allowing for growth in normally uninhabitable areas.

***

### fertilizer\_enabled

#### default: `false`

Enables the use of composters to fertilize crops in the surrounding area.

***

### fertilizer\_radius

#### default: `8`

Plants within this block radius will grow at the `fertilizer_growth_rate` (see below).\
Higher values may increase server resource consumption and cause lag (Maximum 15 blocks).

***

### fertilizer\_passiv

#### default: `false`

`true`: Fertilizer effects do not deplete composters to fertilize the surroundings.\
`false`: Players must actively fill the composter to achieve fertilization effects.

***

### fertilizer\_boost\_growth\_rate

#### default: `20.0`

The `fertilizer_boost_growth_rate` is a ~~percentage~~ value added to biome-specific growth rates, if a composter is within the configured `fertilizer_radius`.\
For example, a biome-specific growth rate of wheat in a desert is 20%. A 'fertilizer\_boost\_growth\_rate' of 25.0% results in a growth rate of 45.0% for wheat plants.

***

### fertilizer\_allow\_growth\_rate\_above\_100

#### default: `false`

Allows a growth rate above 100.0%.\
If a plant has a biome-specific growth rate of 100.0% and this is set to be true, the resulting growth rate can exceed 100.0% (e.g., 125.0%). Otherwise, the growth rate is capped at 100.0%.

{% hint style="info" %}
`fertilizer_allow_growth_rate_above_100` not implemented yet.
{% endhint %}

***

### fertilizer\_enables\_growth\_in\_invalid\_biomes

#### default: `false`

If set to `true`, Fertilizer allows plants to thrive in invalid biomes.\
If set to `false`, plant growth is limited to biomes explicitly listed in `Biome: []` section within `GrowthModifiers.yml`.

***

### fertilizer\_invalid\_biome\_growth\_rate

#### default: `35.0`

Growth rate of plants in invalid biomes with fertilizer access.\
Only active if `fertilizer_enables_growth_in_invalid_biomes` is set to `true`, and the plant is in an invalid biome.

***

### fertilizer\_invalid\_biome\_death\_chance

#### default: `25.0`

Probability of plant death in an invalid biome despite fertilizer supply.\
This rule is effective only when `fertilizer_enables_growth_in_invalid_biomes` is set to `true` and the plant is located in an inappropriate biome.

If the Growth process of the plant consists of multiple growth stages, the death rate is internally adjusted by dividing it by the number of growth stages.

***

## UV-Light

Artificial light source that allows plants within its radius to grow where the sky light level is lower than the `min_natural_light` value.\
This allows the cultivation of plants underground or in buildings without direct sky light access.\\

{% hint style="info" %}
**Note:** Plants still require the minimal vanilla light level to grow.
{% endhint %}

***

### uv\_enabled

#### default: `false`

Enable the use of UV-Light Blocks.

***

### uv\_blocks

#### default:

* `OCHRE_FROGLIGHT`
* `PEARLESCENT_FROGLIGHT`
* `VERDANT_FROGLIGHT`

List of blocks that can function as UV-Light sources.\
Use the official [MATERIAL\_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.

{% hint style="info" %}
**Hint:** `uv_blocks` don't need to emit light, but when they do, you don't require an additional light source.
{% endhint %}

***

### require\_all\_uv\_blocks

#### default: `true`

Determines whether a plant requires all UV-Light Blocks listed above to grow.

* `true`: All blocks in `uv_blocks` are necessary for plant growth.
* `false`: Only one of the blocks listed in `uv_blocks` is required for plant growth.

***

### uv\_radius

#### default: `5`

Radius around a UV-Light block that affects growth.\
Higher values consume more server resources and may lead to server lag. _(Maximum 15 blocks)_\
Use the official [MATERIAL\_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.

### grow\_in\_dark

#### default:

* `RED_MUSHROOM`
* `BROWN_MUSHROOM`
* `NETHER_WART`
* `COCOA`

List of plants that grow without requiring natural sky light, allowing growth in artificial light sources like torches and lanterns _(not the same as UV-Light)_.\
Use the official [MATERIAL\_NAMES](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to add items to this list.

***

## Sounds & Effects

Configure played sounds or particle effects used by this plugin.

***

### plant\_death\_sound\_effect

#### defaults:

* enabled: `true` - Enables the sound and particle effects for dying plants.
* sound: `BLOCK_CROP_BREAK` - The sound played when a plant dies.
* volume: `0.5` - Adjust the volume _(0.0 to 1.0)_.
* pitch: `0.5` - Adjust the pitch _(0.5 to 2.0, 1.0 is normal pitch)_.
* effect: `SMOKE` - The particle effect played when a plant dies.
* data: `1` - Adjust effect data if applicable.

Available sounds and effects:\
Sound: [https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html)\
Particle Effects: [https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html)

***

## Logging & Debugging

Provides various debug logs to offer additional plugin data for troubleshooting and configuration.

***

### debug\_log

#### default: `false`

Enables Debug-Mode. Can get spammy, use for only for debugging.

***

### tree\_log

#### default: `false`

Logs Tree Growing Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}

***

### plant\_log

#### default: `false`

Logs Plant Growing Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}

***

### bonemeal\_log

#### default: `false`

Logs Bonemeal Usage Events in a separate log file.

{% hint style="info" %}
Not implemented yet.
{% endhint %}

***
