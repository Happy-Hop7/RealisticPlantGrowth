---
description: Realistic Plant Growth Commands
---

# Commands

<details>

<summary>plugin.yml</summary>

{% code lineNumbers="true" fullWidth="false" %}
```yaml
commands:
   rpg:
      description: Execute a RealisticPlantGrowth command.
      usage: /rpg <help|info|reload>
      permission: rpg.help
      aliases: [realisticPlantGrowth, realisticplantgrowth]
```
{% endcode %}

</details>

## Overview

<table><thead><tr><th width="254">Command</th><th>Permission</th><th align="center">Can be used in the console</th></tr></thead><tbody><tr><td><a href="commands.md#rpg-help"><code>/rpg help</code></a></td><td><a href="permissions.md#rpg.help"><code>rpg.help</code></a></td><td align="center"><mark style="color:green;">✓</mark></td></tr><tr><td><a href="commands.md#rpg-info"><code>/rpg info</code></a></td><td><a href="permissions.md#rpg.info"><code>rpg.info</code></a></td><td align="center">-</td></tr><tr><td><a href="commands.md#rpg-reload"><code>/rpg reload</code></a></td><td><a href="permissions.md#rpg.reload"><code>rpg.reload</code></a></td><td align="center"><mark style="color:green;">✓</mark></td></tr></tbody></table>

## `/rpg help`

Shows the Help menu if the user has the required permission.

## `/rpg info`

Provides general information about the plant currently held in the player's hand.\
This includes details such as biome groups, valid biomes, and whether the plant can grow in darkness.

For more detailed and location-specific information, utilize the left-click feature of the [`display_growth_rates`](../guides/configuration/config.yml.md#display\_growth\_rates) setting.

## `/rpg reload`

Reloads the plugin and updates all configuration files, eliminating the need for a server restart to apply changes to the configured settings.

***
