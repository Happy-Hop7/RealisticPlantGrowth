# 👋 Welcome to the Realistic Plant Growth Plugin

![GitHub branch status](https://img.shields.io/github/checks-status/Happy-Hop7/RealisticPlantGrowth/master?style=for-the-badge)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/TcGxLk2t?style=for-the-badge&logo=modrinth&logoSize=auto&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Frealistic-plant-growth)
![Modrinth Version](https://img.shields.io/modrinth/v/TcGxLk2t?style=for-the-badge&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Frealistic-plant-growth%2Fversion%2Flatest)

![Discord](https://img.shields.io/discord/1241334817098956851?style=for-the-badge&logo=discord&link=https%3A%2F%2Fdiscord.gg%2FPgUhUNGu2A)
![Static Badge](https://img.shields.io/badge/wiki-RealisticPlantGrowth?style=for-the-badge&logo=gitbook&labelColor=gray&color=c73636&link=https%3A%2F%2Fdocs.nightevolution.de%2F)



> [!IMPORTANT]
> RealisticPlantGrowth is still in an early phase of development. <br>
> If you encounter any problems, please open an issue with a detailed error description. <br>
> I will investigate the occurring problem and try to fix it as soon as possible.

<br>

Realistic Plant Growth is a powerful Spigot/Paper plugin that brings a new level of realism to
the Minecraft flora, allowing you to fine-tune growth parameters on a per-biome basis.

- Do you want to stop Nether Wart farming or make it only work in the Nether like it used to be?
- Do you think Melon farms are too powerful and want to balance them?
- Maybe you want cocoa to grow only in jungles or make plants grow slower in deserts?
- Or perhaps you want farms to be above ground and need natural light?

You can do all that with **Realistic Plant Grotwh**!


<br>

**Realistic Plant Growth** actively monitors grow events, allowing plants to progress through their growth stages based
on a configured growth rate. <br>
What sets this plugin apart is its biome-dependent growth rate, providing an immersive experience tailored to the
in-game environment.

<br>

## Balancing Automatic Farms

You can customize your farming experience by adjusting the required light level for plant growth. This feature can not
only ensure that growth occurs only above ground but also disables the dominance of stacked farms, preventing
overpowering setups like giant cactus farms.

To add an extra layer of complexity, each plant introduces a configurable death chance. This element adds a strategic
component to the game, as plants now have a chance to perish during their growth process. The introduction of a death
chance serves as a countermeasure against fully automatic farms, adding a balancing factor to the gameplay.


<br>

## UV-Light Blocks

UV-Light enables the growth of plants that lack access to natural skylight, enabling their cultivation in dark and
indoor environments. Easily customize the effect radius and the UV-light blocks in the configuration settings.

<br>

## Fertilizer

Fertilizer blocks (Composter) enhance the growth rates of plants in their vicinity. By default, each time a plant
advances to the next stage, the composter fill level is depleted, signifying the utilization of fertilizer to boost the
growth rate. Players must refill the composter either manually or with the assistance of hoppers.


<br>

## Features

All features can be toggled and fine-tuned either directly in the ```Config.yml``` file or by adjusting
the ```GrowthModifiers.yml``` file.

- Biome-Specific Plant Growth
- Biome-Specific Plant Death Chance
- Sounds and Effects On Plant Death
- Replace Farmland On Harvest
- Agricultural Crops Require Hoe
- Plants Require Natural Sky Light
- UV-Light Blocks
- Boost Growth Rates With Fertilizer
- Customizable Messages With [miniMessage](https://docs.advntr.dev/minimessage/format.html) Support.



<br>
<br>

## Wiki
Check out our [wiki](https://docs.nightevolution.de/) for detailed documentation.

<br>
<br>

## Requirements

**Minecraft version:** <br>
-> **1.20** _or above_.

**Java version:** <br>
-> _At least_ **Java 17**. _(recommend using_ **Java 21**_)_

<br>
<br>


![IMG](https://bstats.org/signatures/bukkit/Realistic%20Plant%20Growth.svg)

<br>
<br>

<details>
<summary>Useful Bukkit/Spigot Resources</summary>

## Useful Bukkit/Spigot Resources

- Item/Plant Materials: [Material Documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)
- All available Biomes: [Biome Documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html)
- Sounds: [Sound Documentation](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html)
- Effects: [Effect Documentation](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Effect.html)

</details>


<br>

<details>
  <summary>License</summary>

## License

Realistic Plant Growth is licensed under the terms of the GNU General Public License (GPL) version 3
or any later versions, as published by the Free Software Foundation.
This means you are free to redistribute and modify the program, subject to the conditions outlined
in the license.

### Warranty Disclaimer

Realistic Plant Growth is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Realistic Plant Growth. <br>
If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).

### Contribution and Collaboration

Contributions to Realistic Plant Growth are welcome under the terms of the GPL.
If you choose to contribute, you agree that your contributions will also be subject to the license terms outlined in the GPL.

</details>

<br>

---


_This plugin draws inspiration from [PwnPlantGrowth](https://github.com/Pwn9/PwnPlantGrowth)
by [Pwn9](https://github.com/Pwn9)._ <br>


