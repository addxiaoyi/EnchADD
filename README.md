# EnchADD

EnchADD 是一个 Minecraft 附魔项目，提供两套独立实现：

- Paper 插件：面向 Paper 服务端。
- Mod 模组：支持 Fabric、Forge 和 NeoForge。

当前版本：`2.1.14`

## 选择发行版

| 目标平台 | 文档 | 发布文件 |
| --- | --- | --- |
| Paper | [Paper 插件说明](#paper-插件) | `enchadd-plugin-2.1.14-protected.jar` |
| Fabric / Forge / NeoForge | [Mod 模组说明](mod/README.md) | 六个 Loader 专用 JAR |

2.1.14 修正 Afterglide、Skim、Tideshell、Fleetfoot、TideRunner 和 Lethargy 的 Mod 资源槽位、支持物品和等级约束；六个平台静态资源与适配器校验通过，正式 JAR 构建和玩家行为仍待验证。

Paper 插件和 Mod 是两套独立的运行时实现。请根据你的服务端类型选择其中一种，不要无目的地同时安装。

说明：v2.1.14 已完成六个平台的资源与适配器静态校验；六个新的 Mod JAR 仍需等待外部 Maven 依赖恢复后重新构建，GitHub Release 资产尚未发布。

## Releases 下载

前往 [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases) 下载对应文件。

下载时请根据文件名确认：

- Paper 服务端：下载 `enchadd-plugin-2.1.14-protected.jar`。
- Fabric 服务端：下载 `enchadd-fabric-1.21.11-2.1.14.jar`（或 `enchadd-fabric-26.3-2.1.14.jar`）。
- Forge 服务端：下载 `enchadd-forge-1.21.11-2.1.14.jar`（或 `enchadd-forge-26.3-2.1.14.jar`）。
- NeoForge 服务端：下载 `enchadd-neoforge-1.21.11-2.1.14.jar`（或 `enchadd-neoforge-26.3-2.1.14.jar`）。
- `1.21.11` 和 `26.3` 使用不同的 Loader 与 Java 版本，请勿混用。

## Paper 插件

插件面向 Paper `1.21.11` 及更新的兼容版本。Paper 1.21.11 使用 Java 21；Paper 26.3+ 使用该版本服务端要求的 Java 版本。

### 安装

1. 从 [GitHub Releases](https://github.com/addxiaoyi/EnchADD/releases) 下载 `enchadd-plugin-2.1.14-protected.jar`。
2. 将文件放入服务端的 `plugins/` 目录。
3. 启动服务端并编辑 `plugins/EnchADD/config.yml`。

受保护构件会在启动时校验发布哈希，自动更新时不会覆盖当前正在运行的 JAR。

### 构建 Paper 插件

```bash
mvn -pl plugin -am test
mvn -pl plugin -am -Pprotected-release -DskipTests verify
```

## Mod 模组

Mod 的六个构件并不通用，必须选择与服务端 Loader 和 Minecraft 版本完全匹配的文件：

| Loader | Minecraft | Java |
| --- | --- | --- |
| Fabric | `1.21.11` | 21 |
| Fabric | `26.3` | 25 |
| Forge | `1.21.11` | 21 |
| Forge | `26.3` | 25 |
| NeoForge | `1.21.11` | 21 |
| NeoForge | `26.3` | 25 |

详细安装、构建和验证说明见 [Mod README](mod/README.md)。

## 项目图标

![EnchADD 图标](mod/common/src/main/resources/assets/enchadd/enchadd-icon.svg)

## 验证说明

项目将以下证据分开记录：

- 构建和资源校验。
- Mod 被 Loader 发现、启动和注册 Mixin/事件。
- 真实玩家触发附魔行为。

完整证据见 [docs/build-evidence.md](docs/build-evidence.md)。仓库不会仅凭静态源码或启动成功宣称完整的 111 个附魔行为一致性。

目前 Minecraft `26.3` 的真实玩家行为验证仍需要支持 `26.3` 的客户端或协议实现；常用的 `minecraft-data` 和 `minecraft-protocol` 包目前只提供到 `26.1`。

## 许可证

详见 [LICENSE](LICENSE)。
