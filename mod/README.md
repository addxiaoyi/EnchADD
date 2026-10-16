# EnchADD Mod

The EnchADD Mod distribution provides the same enchantment catalog across six loader-specific artifacts. The current release is `2.1.1`.

## Supported Artifacts

| Artifact | Minecraft | Java |
| --- | --- | --- |
| `enchadd-fabric-2.1.1.jar` | Fabric `1.21.11` | 21 |
| `enchadd-fabric-2.1.1.jar` | Fabric `26.3` | 25 |
| `enchadd-forge-2.1.1.jar` | Forge `1.21.11` | 21 |
| `enchadd-forge-2.1.1.jar` | Forge `26.3` | 25 |
| `enchadd-neoforge-2.1.1.jar` | NeoForge `1.21.11` | 21 |
| `enchadd-neoforge-2.1.1.jar` | NeoForge `26.3` | 25 |

The six artifacts are not interchangeable. Select the JAR whose loader and Minecraft version match the server.

## Install

1. Download the matching JAR from [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases).
2. Put the JAR in the server's `mods/` directory.
3. Confirm the server has the required loader and Java version.
4. Start the server and check the loader log for EnchADD initialization.

Do not install the Fabric, Forge, and NeoForge variants together. The Paper plugin is a separate implementation.

## Features

The Mod distribution contains 111 generated enchantment definitions backed by shared, loader-independent behavior rules and loader adapters. Coverage includes combat, defense, projectiles, movement, tools, drops, fishing, durability, and utility effects.

The catalog is generated from:

```text
common/src/main/resources/data/enchadd/enchantments.list
```

## Build

Use JDK 21 for the `1.21.11` modules and JDK 25 for the `26.3` modules. Gradle 9.7 or newer is required.

```powershell
gradle -p mod :fabric-1.21.11:build
gradle -p mod :fabric-26.3:build
gradle -p mod :forge-1.21.11:build
gradle -p mod :forge-26.3:build
gradle -p mod :neoforge-1.21.11:build
gradle -p mod :neoforge-26.3:build
```

Each build also runs the generated resource and behavior-adapter validators.

## Verification Status

The evidence policy separates three claims:

- Build and resource validation.
- Mod discovery, loader startup, and Mixin/event registration.
- Real player-driven enchantment behavior.

The first two claims are available for all six modules. Formal `Block.getDrops` Smelting behavior is verified for Fabric, Forge, and NeoForge `1.21.11`. The `26.3` player behavior matrix remains open until a client or protocol implementation supporting `26.3` is available.

See [the current evidence report](../docs/build-evidence.md) and [the 111-enchantment matrix](../docs/enchantment-evidence-matrix-20261009.csv).

## Icon

![EnchADD icon](common/src/main/resources/assets/enchadd/enchadd-icon.svg)

## License

See [../LICENSE](../LICENSE).
