# EnchADD Mod 模组

EnchADD Mod 为 Fabric、Forge 和 NeoForge 提供同一套附魔目录的覆盖 1.21.1-1.21.11 + 26.3 全版本的 Loader 专用构件。当前版本为 `2.1.17`。

2.1.17 修正 Bulwark、Purify、Lucidity、Dispel、Ricochet、Clairvoyance、Evasion、Shroud 和 Freshcatch 的 Mod 资源约束，并对齐 Bulwark 抗性等级、Clairvoyance 直接攻击条件和 Forge/NeoForge Ricochet 发射记录；静态校验与十五个 Loader 的正式 JAR 构建均已通过（Forge Bulwark 适配器随本次构建修复：新版 EventBus 7 无 `isCanceled()`，改为依赖 `ShieldBlockEvent` 仅在成功格挡时触发的语义），加载验证已通过；1.21.1 三平台的 Bulwark/Breakguard/盾挡边界玩家行为已通过隔离 bot 测试（mineflayer），其余附魔行为仍待验证。

## 支持构件

| 构件 | Minecraft | Java |
| --- | --- | --- |
| `enchadd-fabric-1.21.1-2.1.17.jar` | Fabric `1.21.1` | 21 |
| `enchadd-fabric-1.21.2-2.1.17.jar` | Fabric `1.21.2-1.21.4` | 21 |
| `enchadd-fabric-1.21.6-2.1.17.jar` | Fabric `1.21.5-1.21.10` | 21 |
| `enchadd-fabric-1.21.11-2.1.17.jar` | Fabric `1.21.11` | 21 |
| `enchadd-fabric-26.3-2.1.17.jar` | Fabric `26.3` | 25 |
| `enchadd-forge-1.21.1-2.1.17.jar` | Forge `1.21.1` | 21 |
| `enchadd-forge-1.21.3-2.1.17.jar` | Forge `1.21.3-1.21.5` | 21 |
| `enchadd-forge-1.21.6-2.1.17.jar` | Forge `1.21.6-1.21.10` | 21 |
| `enchadd-forge-1.21.11-2.1.17.jar` | Forge `1.21.11` | 21 |
| `enchadd-forge-26.3-2.1.17.jar` | Forge `26.3` | 25 |
| `enchadd-neoforge-1.21.1-2.1.17.jar` | NeoForge `1.21.1` | 21 |
| `enchadd-neoforge-1.21.3-2.1.17.jar` | NeoForge `1.21.3-1.21.5` | 21 |
| `enchadd-neoforge-1.21.8-2.1.17.jar` | NeoForge `1.21.8-1.21.10` | 21 |
| `enchadd-neoforge-1.21.11-2.1.17.jar` | NeoForge `1.21.11` | 21 |
| `enchadd-neoforge-26.3-2.1.17.jar` | NeoForge `26.3` | 25 |

各构件不能互换。请根据服务端 Loader 和 Minecraft 版本选择对应文件。

说明：v2.1.17 的资源与适配器静态校验已通过，全部 Loader 的正式 JAR 已于 2026-10-24 构建成功并生成 SHA-256（见 `release-assets/v2.1.17/` 与 `docs/validation-logs/v2.1.17-six-loader-build-20261024b.txt`）；服务器加载与玩家行为验证完成前不把本版本标记为可下载发布。

## 安装

1. 从 [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases) 下载匹配的 JAR。
2. 将 JAR 放入服务端的 `mods/` 目录。
3. 确认服务端使用正确的 Loader 和 Java 版本。
4. 启动服务端，并在日志中确认 EnchADD 已初始化。

Fabric、Forge 和 NeoForge 构件不要同时安装。Paper 插件是另一套独立实现。

## 内容

Mod 包含 111 个由统一目录生成的附魔定义，行为由共享规则和 Loader 适配器共同实现，覆盖战斗、防御、投射物、移动、工具、掉落、钓鱼、耐久和辅助能力。

附魔目录文件：

```text
common/src/main/resources/data/enchadd/enchantments.list
```

## 构建

`1.21.11` 模块使用 JDK 21，`26.3` 模块使用 JDK 25。当前构建要求 Gradle 9.7 或更高版本。

```powershell
gradle -p mod :fabric-1.21.11:build
gradle -p mod :fabric-26.3:build
gradle -p mod :forge-1.21.11:build
gradle -p mod :forge-26.3:build
gradle -p mod :neoforge-1.21.11:build
gradle -p mod :neoforge-26.3:build
```

每个构建任务都会执行生成资源校验和行为适配器校验。

## 验证状态

项目将证据分为三类：

- 构建和资源校验。
- Mod 被 Loader 发现、启动以及 Mixin/事件注册。
- 真实玩家触发附魔行为。

全部模块均有构建和 Loader 启动证据。正式的 `Block.getDrops` Smelting 行为已在 Fabric、Forge、NeoForge `1.21.11` 上验证。`26.3` 的玩家行为矩阵仍待支持 `26.3` 的客户端或协议实现后完成。

详细证据见 [构建与运行证据](../docs/build-evidence.md) 和 [111 个附魔矩阵](../docs/enchantment-evidence-matrix-20261009.csv)。

## 图标

![EnchADD 图标](common/src/main/resources/assets/enchadd/enchadd-icon.svg)

## 许可证

详见 [../LICENSE](../LICENSE)。
