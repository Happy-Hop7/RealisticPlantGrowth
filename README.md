<img src="https://github.com/Happy-Hop7/RealisticPlantGrowth-Wiki/blob/master/.gitbook/assets/Plugin%20Logo%202k.png" alt="Realistic Plant Growth Plugin Logo" width="80%"/>

---

<br>

![GitHub branch status](https://img.shields.io/github/checks-status/Happy-Hop7/RealisticPlantGrowth/master?style=for-the-badge)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/TcGxLk2t?style=for-the-badge&logo=modrinth&logoSize=auto)](https://modrinth.com/plugin/realistic-plant-growth)
[![Modrinth Version](https://img.shields.io/modrinth/v/TcGxLk2t?style=for-the-badge)](https://modrinth.com/plugin/realistic-plant-growth/version/latest)

[![Discord](https://img.shields.io/discord/1241334817098956851?style=for-the-badge&logo=discord)](https://discord.gg/PgUhUNGu2A)
[![Static Badge](https://img.shields.io/badge/wiki-RealisticPlantGrowth?style=for-the-badge&logo=gitbook&labelColor=gray&color=c73636)](https://docs.nightevolution.de/)

<br>



# 👋 Welcome to the Realistic Plant Growth Plugin

Realistic Plant Growth is a powerful Spigot/Paper plugin that brings a new level of realism to
the Minecraft flora, allowing you to fine-tune growth parameters on a per-biome basis.

<br>

- Do you want to stop Nether Wart farming or make it only work in the Nether like it used to be?
- Do you think Melon farms are too powerful and want to balance them?
- Maybe you want cocoa to grow only in jungles or make plants grow slower in deserts?
- Or perhaps you want farms to be above ground and need natural light?

You can do all that with **Realistic Plant Growth**!


<br>

**Realistic Plant Growth** actively monitors grow events, allowing plants to progress through their growth stages based on a configured growth rate. <br>
What sets this plugin apart is its biome-dependent growth rate, providing an immersive experience tailored to the in-game environment.

<br>



## Supported Languages

Realistic Plant Growth is currently available in the following languages:

- **English** (default)
- **German** (Deutsch)
- **Finnish** (Suomi)
- **Russian** (Русский)
- **Chinese** (Traditional) (繁體中文)

Want to add another language? See the [Translators page](https://realistic-plant-growth.nightevolution.de/for-contributors/for-translators)!

<br>
<br>

## Wiki
Check out our [wiki](https://docs.nightevolution.de/) for detailed documentation.

<br>
<br>

## Features

All features are highly customizable! <br>
You can tweak them to your preference in the ```Config.yml``` and ```GrowthModifiers.yml``` files.

- **🌍 Biome-Based Growth:** Customize plant growth by biome.
- **🏞️ Custom Biome Support**: Works with Terra, Terralith, and more.
- **🌱 Survival Chance:** Plants may die at each growth stage.
- **🪓 Tool-Based Harvesting:** Use hoes for crop drops.
- **🚜 Farmland Decay:** Farmland revert to dirt after harvest.
- **☀️ Sunlight Requirement:** Plants need natural light to grow.
- **💡 UV Blocks for Indoor Farming:** Grow indoors with UV blocks.
- **🌾 Fertilizer Boost:** Nearby composters speed up growth, requiring refills.
- **🎶 Atmospheric Effects:** Optional sounds/effects on plant death.
- **🗨️ Custom Messages:** Fully customizable player messages.

<br>

For a full and detailed feature description, visit the [wiki](https://realistic-plant-growth.nightevolution.de/overview/features).

<br>



## Requirements

**Minecraft version:** <br>
-> **1.20.1** _or above_.

**Java version:** <br>
-> _At least_ **Java 21**.

<br>

---

## Commands

```yaml
commands:
   rpg:
      description: Execute a RealisticPlantGrowth command.
      usage: /rpg <help|info|reload>
      permission: rpg.help
      aliases: [realisticPlantGrowth, realisticplantgrowth]
```


## Permissions

```yaml
permissions:
  rpg.*:
    description: Gives access to all RealisticPlantGrowth features.
    default: op
    children:
      rpg.reload: true
      rpg.help: true
      rpg.info: true

  rpg.reload:
    description: Gives access to the /rpg reload command.
    default: op

  rpg.help:
    description: Base-Permission to access /rpg command.
    default: op

  rpg.info:
    description: Gives access to the /rpg info command.
    default: op
    children:
      rpg.info.interact: true

  rpg.info.interact:
    description: Gives growth information of plants when interacting with them.
    default: true
```

---

<br>

[![IMG](https://bstats.org/signatures/bukkit/Realistic%20Plant%20Growth.svg)](https://bstats.org/plugin/bukkit/Realistic%20Plant%20Growth/20634)

<br>

---


# License

Realistic Plant Growth is licensed under the terms of the GNU General Public License (GPL) version 3
or any later versions, as published by the Free Software Foundation.
This means you are free to redistribute and modify the program, subject to the conditions outlined
in the license.

## Warranty Disclaimer

Realistic Plant Growth is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Realistic Plant Growth. <br>
If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).

## Contribution and Collaboration

Contributions to Realistic Plant Growth are welcome under the terms of the GPL.
If you choose to contribute, you agree that your contributions will also be subject to the license terms outlined in the GPL.

<br>

---


_This plugin draws inspiration from [PwnPlantGrowth](https://github.com/Pwn9/PwnPlantGrowth)
by [Pwn9](https://github.com/Pwn9)._ <br>
