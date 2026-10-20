# EnchADD Mod 模组

EnchADD Mod 为 Fabric、Forge 和 NeoForge 提供同一套附魔目录的六个 Loader 专用构件。当前版本为 `2.1.12`。

2.1.12 对齐 Initiative、Wingclip、Tracer 和 Pursuit 的剑、斧、三叉戟范围，并强化六 Loader 条件校验；真实游戏触发仍未验证。

## 支持构件

| 构件 | Minecraft | Java |
| --- | --- | --- |
| `enchadd-fabric-1.21.11-2.1.12.jar` | Fabric `1.21.11` | 21 |
| `enchadd-fabric-26.3-2.1.12.jar` | Fabric `26.3` | 25 |
| `enchadd-forge-1.21.11-2.1.12.jar` | Forge `1.21.11` | 21 |
| `enchadd-forge-26.3-2.1.12.jar` | Forge `26.3` | 25 |
| `enchadd-neoforge-1.21.11-2.1.12.jar` | NeoForge `1.21.11` | 21 |
| `enchadd-neoforge-26.3-2.1.12.jar` | NeoForge `26.3` | 25 |

六个构件不能互换。请根据服务端 Loader 和 Minecraft 版本选择对应文件。

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

六个模块均有构建和 Loader 启动证据。正式的 `Block.getDrops` Smelting 行为已在 Fabric、Forge、NeoForge `1.21.11` 上验证。`26.3` 的玩家行为矩阵仍待支持 `26.3` 的客户端或协议实现后完成。

详细证据见 [构建与运行证据](../docs/build-evidence.md) 和 [111 个附魔矩阵](../docs/enchantment-evidence-matrix-20261009.csv)。

## 图标

![EnchADD 图标](common/src/main/resources/assets/enchadd/enchadd-icon.svg)

## 许可证

详见 [../LICENSE](../LICENSE)。
