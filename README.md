# EnchADD

EnchADD is a Paper plugin that adds vanilla-style enchantments with compatibility and runtime protection for modern Paper servers.

## Version

Current release: **2.0.5**

Supported baseline: Paper 1.21.6 and newer compatible Paper builds. Java 21 or newer is required.

## Install

1. Download `enchadd-plugin-2.0.5-protected.jar` from [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases).
2. Copy the jar to the server `plugins/` directory.
3. Start the server and configure `plugins/EnchADD/config.yml`.

The protected artifact excludes Maven coordinate metadata and verifies its release checksum at startup.

## Update checks

Update checks are disabled by default. To enable notification-only checks in `config.yml`:

```yaml
update-checker:
  enabled: true
  repository: addxiaoyi/EnchADD
  timeout-seconds: 8
```

The plugin only reads the latest GitHub release and logs a notification. It never downloads or replaces files automatically.

## Build

```bash
mvn -pl plugin -am test
mvn -pl plugin -am -Pprotected-release -DskipTests verify
```

The protected jar is written to `plugin/target/`.
