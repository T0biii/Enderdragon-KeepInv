# Modrinth project metadata

Everything needed to create the project at https://modrinth.com/dashboard/projects (create a *Plugin*).

| Field | Value |
| --- | --- |
| Name | EnderDragonKeepInv |
| URL / slug | `enderdragon-keepinv` |
| Summary | see [`summary.txt`](summary.txt) (max. 256 characters) |
| Description | see [`description.md`](description.md) (Markdown) |
| Icon | [`icon.png`](icon.png) (512x512 PNG) |
| Project type | Plugin |
| Categories | `game-mechanics`, `utility` |
| Additional (optional) | `adventure` |
| Client side | Unsupported |
| Server side | Required |
| License | MIT (https://github.com/T0biii/Enderdragon-KeepInv/blob/main/LICENSE) |
| Source code | https://github.com/T0biii/Enderdragon-KeepInv |
| Issue tracker | https://github.com/T0biii/Enderdragon-KeepInv/issues |
| Wiki / Discord / Donation | not set |
| Tags (search) | ender dragon, keep inventory, keepinventory, the end, death, paper |

## Version upload

| Field | Value |
| --- | --- |
| Loaders | Paper (the plugin uses the Paper API and Paper-only events) |
| Game versions | 26.1+ (tested with 26.3) |
| Version number | tag without `v`, e.g. `1.0.0` |
| Release channel | Release (use Beta/Alpha for tags with a `-suffix`) |
| Dependencies | none |
| File | `EnderDragonKeepInv-<version>.jar` from the GitHub release |
| Changelog | see [`changelog.md`](changelog.md) / GitHub release notes |

## Notes

- Requires Java 25 (Paper 26.x requirement).
- The bStats service id is set in `EnderDragonKeepInv.java` (`BSTATS_ID`). While it is `0`, bStats is not started.
