---
description: Permissions utilized by the Realistic Plant Growth Plugin
---

# Permissions

<details>

<summary>plugin.yml</summary>

{% code lineNumbers="true" fullWidth="true" %}
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
{% endcode %}

</details>

***

## Admin

The permissions listed in this section are intended for use by server administrators who have knowledge of the plugin's behavior and configurations.

### `rpg.*`

This permission grants access to all commands offered by the **Realistic Plant Growth** plugin.

### `rpg.reload`

This permission, included in the `rpg.*` group, functions as a negative permission option when providing users with access to the `rpg.*` permission.\
It is essential for the use of the [`/rpg reload`](commands.md#rpg-reload) command.

***

## User

Recommended permissions for regular users.

### `rpg.help`

This permission is necessary to receive a help message when either the [`/rpg help`](commands.md#rpg-help) command or an unknown subcommand of the plugin is used.

### `rpg.info`

This permission provides access to the [`/rpg info`](commands.md#rpg-info) command.

### `rpg.info.interact`

This permission allows players to gather growth information about a plant by performing a left-click on the ground with a seed, within the context of the location of the clicked block.\
To provide this information, a comprehensive area search is conducted to determine the exact modifiers applicable in the current environment.

{% hint style="warning" %}
Prevent server lag caused by players spam-clicking with seeds by utilizing the [`display_cooldown`](../guides/configuration/config.yml.md#display\_cooldown) setting in the [`Config.yml`](../guides/configuration/config.yml.md) file!
{% endhint %}

***
