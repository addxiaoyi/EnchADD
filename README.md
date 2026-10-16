# EnchADD

EnchADD is a Minecraft enchantment project with two independent distributions:

- A Paper plugin for server-side enchantments.
- A cross-loader mod for Fabric, Forge, and NeoForge.

The current release version is `2.1.1`.

## Choose Your Distribution

| Target | Documentation | Artifact |
| --- | --- | --- |
| Paper | [Plugin guide](#paper-plugin) | `enchadd-plugin-2.1.1-protected.jar` |
| Fabric / Forge / NeoForge | [Mod guide](mod/README.md) | Six loader-specific JARs |

The Paper plugin and Mod distribution are separate runtime implementations. Do not install both unless you intentionally want both active.

## Paper Plugin

The plugin targets Paper `1.21.11` and newer compatible releases. Java 21 is required for Paper 1.21.11; Paper 26.3+ follows the Java version required by that server release.

### Install

1. Download `enchadd-plugin-2.1.1-protected.jar` from [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases).
2. Copy it to the server `plugins/` directory.
3. Start the server and edit `plugins/EnchADD/config.yml`.

The protected artifact verifies its release checksum at startup and never overwrites the currently running JAR during updates.

### Paper Build

```bash
mvn -pl plugin -am test
mvn -pl plugin -am -Pprotected-release -DskipTests verify
```

## Mod Icon

![EnchADD icon](mod/common/src/main/resources/assets/enchadd/enchadd-icon.svg)

## Evidence

Build and runtime evidence is maintained in [docs/build-evidence.md](docs/build-evidence.md). Build success, loader startup, and real in-game behavior are tracked separately.

The repository does not claim full 111-enchantment behavior parity from static source inspection alone. Minecraft `26.3` player behavior requires a client or protocol implementation that supports `26.3`; the commonly available `minecraft-data` and `minecraft-protocol` packages currently stop at `26.1`.

## License

See [LICENSE](LICENSE).
