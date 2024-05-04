---
description: Explore the diverse features that make Realistic Plant Growth stand out.
---

# ✨ Features

{% hint style="info" %}
N**ote:**

All features can be toggled and fine-tuned either directly in the `Config.yml` file or by adjusting the `GrowthModifiers.yml` file.
{% endhint %}

## Biome-Specific Plant Growth

Customize the growth rates of Minecraft plants based on specific biomes by adjusting the values in the `GrowthModifiers.yml` file. \\

For additional details about configuring the `GrowthModifiers.yml` file, refer to the information provided [here](../guides/configuration/growthmodifiers.yml.md).

## Biome-Specific Plant Death Chance

Whenever a plant attempts to advance to the next growth stage, a dice roll determines whether the plant either progresses in growth or faces the possibility of dying.\
This chance of death can be customized for each plant individually.

## Sounds and Effects

When a plant dies, a sound and visual effect can be triggered at the plant's location.

## Replace Farmland On Harvest

When harvesting fully or partially grown plants, the farmland undergoes a random transformation into either dirt or coarse dirt. This prompts users to re-till the farm area before planting new seeds.

## Agricultural Crops Require Hoe

To obtain drops from a harvested agricultural plant, users have to use a hoe.\
Not using a hoe cancels the drop of items.

## Plants Require Natural Sky Light

Plants can be set to exclusively thrive in natural skylight, preventing growth in underground and indoor farms. The light level necessary for plant growth can be adjusted.

## UV-Light Blocks

UV-Light enables the growth of plants that lack access to natural skylight, enabling their cultivation in dark and indoor environments.

## Boost Growth Rates With Fertilizer

Fertilizer blocks (Composter) enhance the growth rates of plants in their vicinity. By default, each time a plant advances to the next stage, the composter fill level is depleted, signifying the utilization of fertilizer to boost the growth rate. Players must refill the composter either manually or with the assistance of hoppers.

## Customizable Messages

Take control of almost every player message by effortlessly adjusting them through the default language files or by making personalized modifications to suit your preferences.\
_**Realistic Plant Growth**_ messages are designed to seamlessly integrate with the [miniMessage format](https://docs.advntr.dev/minimessage/format.html).

***

## Planned Features

* [ ] Support Custom Biomes ([Terra](https://github.com/PolyhedralDev/Terra))
* [ ] Support Custom Plants (Slimefun4 - [ExoticGarden Addon](https://github.com/TheBusyBiscuit/ExoticGarden))
* [ ] Enhance Event Logging
* [ ] Implement Visual Effects for Area Scans (Debugging Mode for Admins)
* [ ] Enhance UV-Light Detection
* [ ] Restrict Bonemeal Usage for Plants
* [ ] Introduce Offline Plant Growth Add-On
* [ ] Optimize Area Scan Performance
* [ ] Implement Developer API

***
