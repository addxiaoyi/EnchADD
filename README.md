# EnchADD

EnchADD is a Paper plugin that adds vanilla-style enchantments with compatibility and runtime protection for modern Paper servers.

## Version

Current release: **2.1.1**

Supported baseline: Paper 1.21.11 and newer compatible Paper builds, including Paper 26.3+. Java 21 is required for Paper 1.21.11; Paper 26.3+ requires the server runtime supported by that Paper release (Java 25 or newer).

## Install

1. Download `enchadd-plugin-2.1.1-protected.jar` from [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases).
2. Copy the jar to the server `plugins/` directory.
3. Start the server and configure `plugins/EnchADD/config.yml`.

The protected artifact excludes Maven coordinate metadata and verifies its release checksum at startup.

## Spear enchants

The plugin includes four spear-focused enchants: Lancer (sprinting burst), Reach (distance-scaled hit), Skewer (short slow with cooldown), and Counterthrust (sneaking damage reduction). All four support the wooden, stone, copper, iron, gold, diamond, and netherite spear materials plus tridents, with bounded values and per-player cooldowns.

## Update checks

Update checks are disabled by default. To enable notification-only checks in `config.yml`:

```yaml
update-checker:
  enabled: true
  repository: addxiaoyi/EnchADD
  timeout-seconds: 8
  interval-hours: 12
  auto-download: false
```

By default the plugin only reads the latest GitHub release and logs a notification. To enable safe staged updates, add `auto-download: true`; the plugin downloads only the matching protected jar, verifies GitHub SHA-256, and places it in Paper's `plugins/update` directory for the next restart. It never overwrites the jar currently running.

## Build

```bash
mvn -pl plugin -am test
mvn -pl plugin -am -Pprotected-release -DskipTests verify
```

The protected jar is written to `plugin/target/`.
