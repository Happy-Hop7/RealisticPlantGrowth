---
description: 🚧 Work in Progress 🚧
---

# Permissions



<details>

<summary>plugin.yml</summary>

```
permissions:
  rpg.*:
    description: Gives access to all RealisticPlantGrowth features.
    default: op
    children:
        rpg.help: true
        rpg.info: true
        rpg.reload: true

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

</details>

***
