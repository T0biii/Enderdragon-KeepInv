# EnderDragonKeepInv

**Keep inventory - but only while the Ender Dragon is alive.**

The End is brutal. Dying there usually means losing your whole kit and a long run back for your items. With EnderDragonKeepInv the server automatically turns on *keep inventory* for **all players** as soon as an Ender Dragon exists, and turns it off again the moment the dragon is dead. No gamerule juggling, no commands, no setup.

## Features

- **Automatic**: activates when the Ender Dragon spawns (first spawn or a respawn with end crystals) and deactivates when it dies.
- **Safe across restarts**: the state is saved, so keep inventory stays active after a restart while the dragon is still alive.
- **Works everywhere**: applies to deaths in every dimension while the dragon lives, not only in the End.
- **Keeps XP too** (configurable), exactly like the vanilla `keepInventory` gamerule.
- **Does not touch your gamerules**: it works through the death event, so your world settings stay as they are.
- **Update notifications**: a message in the console when a new version is released on GitHub (can be turned off).
- **bStats** anonymous metrics (can be turned off).
- Lightweight, no dependencies, no commands required.

## Installation

1. Requires **Paper 26.1 or newer** and **Java 25**.
2. Drop the jar into your `plugins` folder.
3. Restart the server. Done.

## Configuration

```yaml
keep-inventory:
  keep-level: true      # also keep experience levels

update-checker:
  enabled: true         # console message when a new version is available

metrics:
  enabled: true         # anonymous bStats statistics
```

## Commands & permissions

| Command | Description |
| --- | --- |
| `/enderdragonkeepinv status` (`/edki status`) | Shows whether keep inventory is currently active |
| `/enderdragonkeepinv reload` | Reloads the config |
| `/enderdragonkeepinv reset` | Clears the saved dragon state and rescans loaded worlds |

Permission: `enderdragonkeepinv.admin` (default: operators).

## Metrics

This plugin uses [bStats](https://bstats.org) to collect anonymous usage statistics. You can opt out by setting `metrics.enabled: false` in the plugin's `config.yml`, or globally in `plugins/bStats/config.yml`.

## Links

- Source code & issues: https://github.com/T0biii/Enderdragon-KeepInv
- License: MIT

Made by **T0biii**.
