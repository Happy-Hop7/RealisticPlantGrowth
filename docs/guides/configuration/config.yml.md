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
If you wish to change the language, you'll need to identify and select the `.yml` file with the desired language. You also have the option to create your own language files and use them .

In cases where specific translations are missing, the default strings from the default language (`en-GB`) will be automatically selected.

### plugin\_prefix

#### default: _'\[RealisticPlantGrowth] '_

Plugin prefix for console logs. Does not affect chat messages.\
You can customize the prefix using miniMessage format and legacy &-color codes. \
For details, refer to: [MiniMessage Format](https://docs.advntr.dev/minimessage/format.html)

### enabled\_worlds

#### default:

* your\_world
* your\_world\_nether
* your\_world\_end

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

## UV-Light

## Sounds & Effects

## Logging & Debugging
