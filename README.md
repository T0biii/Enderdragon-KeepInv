# EnderDragonKeepInv

[![Build](https://github.com/T0biii/Enderdragon-KeepInv/actions/workflows/build.yml/badge.svg)](https://github.com/T0biii/Enderdragon-KeepInv/actions/workflows/build.yml)

A [Paper](https://papermc.io) plugin by **T0biii**: while the Ender Dragon is alive, every player keeps their inventory (and XP) when dying. As soon as the dragon is dead, vanilla death behaviour is back.

- Activates automatically on first dragon spawn and on every respawn
- State survives restarts
- Console notice when a new GitHub release is available (`update-checker.enabled`)
- [bStats](https://bstats.org) metrics ([public stats](https://bstats.org/plugin/bukkit/Enderdragon%20KeepInv/34925)), disable with `metrics.enabled: false`

Requires Paper 26.1+ and Java 25. See [`docs/modrinth`](docs/modrinth) for the Modrinth listing texts and icon.

## Build (Docker only)

The project is always built in Docker, no local JDK needed:

```bash
docker build --target artifact --output type=local,dest=dist .
```

The jar ends up in `dist/`. To set a specific version:

```bash
docker build --target artifact --build-arg VERSION=1.2.3 --output type=local,dest=dist .
```

## Smoke test

After every build a real Paper server is started in Docker, the built jar is loaded and the dragon logic is checked (spawn, `/edki status`, kill). It runs automatically as part of the `Build` workflow (a release is only created if it passes) and can be started manually via **Actions > Smoke Test > Run workflow**, optionally with a different Minecraft version. Locally:

```bash
scripts/smoke-test.sh dist/EnderDragonKeepInv-<version>.jar [minecraft-version]
```

## Releases

Every push and pull request builds the plugin via GitHub Actions and attaches the jar as workflow artifact. Pushing a tag creates a GitHub release with the jar attached:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The tag (without `v`) becomes the plugin version. Tags with a suffix (`v1.1.0-beta.1`) are marked as pre-release.

## bStats

The plugin reports anonymous usage statistics to [bStats](https://bstats.org/plugin/bukkit/Enderdragon%20KeepInv/34925) (service id `34925`, set in `EnderDragonKeepInv.java` as `BSTATS_ID`). Server owners can opt out with `metrics.enabled: false` in the plugin config or globally in `plugins/bStats/config.yml`.

## License

[MIT](LICENSE)
