# 1.21.2 – 1.21.10 版本能力矩阵（2026-10-24 采集）

数据来源：Mojang piston-meta version_manifest_v2（Java、发布日期、asset index）、maven.fabricmc.net（fabric-api/fabric-loader 最新构件）、maven.neoforged.net（NeoForge 21.x 元数据）。采集时 Fabric、Forge、NeoForge 三个 Maven 均 HTTP 200 可达。

## 逐版本矩阵

| 版本 | 发布日期 | Mojang 要求 Java | fabric-api 最新 | NeoForge 线 | 数据包格式（pack_format） | asset index | EnchADD 相关新增物品能力 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1.21.2 | 2024-10-22 | 21 | 0.106.1+1.21.2 | 21.3.x | 57（20.45+ 扩展） | 18 | 试点附魔精度：enchantment `definition` 数据包格式（slots/supported_items/effect 列表）已可用；`enchantable/*` 物品标签 |
| 1.21.3 | 2024-10-23 | 21 | 0.114.1+1.21.3 | 21.3.x | 57 | 18 | 无重大物品面变化（breeze rod 生态修补） |
| 1.21.4 | 2024-12-03 | 21 | 0.119.4+1.21.4 | 21.4.x | 61 | 19 | `minecraft:enchantable/*` 标签面持续；Piglin 头盔/盔甲纹饰无关；对 EnchADD 无新物品钩子 |
| 1.21.5 | 2025-03-25 | 21 | 0.128.2+1.21.5 | 21.5.x | 71 | 24 | datafixerupper 9 世代：物品组件序列化调整；`condition` → 结构稳定；leaf litter、bush、firefly bush、wildflowers、cactus flower（对附魔支持面无影响） |
| 1.21.6 | 2025-06-17 | 21 | 0.128.2+1.21.6 | 21.6.x | 80 | 26 | dry grass、dried ghast；happy ghast/harness（可附魔面未扩展）；Paper 插件端最低支持线 |
| 1.21.7 | 2025-06-30 | 21 | 0.129.0+1.21.7 | 21.7.x | 81 | 26 | 无新增可附魔物品 |
| 1.21.8 | 2025-07-17 | 21 | 0.136.1+1.21.8 | 21.8.x | 81 | 26 | copper golem/golem statue、shelf、copper chest 等（不可附魔） |
| 1.21.9 | 2025-09-30 | 21 | 0.134.1+1.21.9 | 21.10.x（21.10.63/64） | 88 | 27 | copper tools/armor + copper nugget 首次进入 `enchantable/*` 标签面（copper_sword、copper_pickaxe、copper_axe、copper_shovel、copper_hoe、copper_helmet/chestplate/leggings/boots） |
| 1.21.10 | 2025-10-07 | 21 | 0.138.4+1.21.10 | 21.10.x | 88 | 27 | 同 1.21.9 物品面，无新增可附魔物品 |

对照：仓库当前支持的 1.21.11（pack min_format 94 / asset 29，NeoForge 21.11.x，fabric-api 0.141.6+1.21.11）与 26.3（min_format 121，Java 25，fabric-api 0.161.0+26.3）。

## 关键结论

1. **Java 全线为 21**：1.21.2–1.21.10 均 Mojang 要求 Java 21，与现有 `fabric-1.21.11`/`forge-1.21.11`/`neoforge-1.21.11` 的 `java_release=21` 一致；如补建历史版本模块可复用同一 JDK 21 工具链。
2. **数据包格式断层**：`pack_format` 从 57（1.21.2/3）→ 61（1.21.4）→ 71（1.21.5）→ 80/81（1.21.6–8）→ 88（1.21.9/10）→ 94（1.21.11）。`generate-enchant-resources.gradle` 目前按 `is26_3` 二分（94 vs 121），若要覆盖 1.21.2–1.21.10 需要 per-version `min_format` 表，而不是现有二元分支。
3. **`condition` 字段命名**：1.21.2–1.21.10 的附魔 JSON 使用 `condition` 键（26.3 改为 `type`），现有 `requirementType` 二分逻辑兼容 1.21.x 全线，可直接覆盖到 1.21.10。
4. **附魔支持面差异**：1.21.9 起 copper 工具/盔甲进入 `minecraft:enchantable/*` 标签。若目录附魔按标签引用（`#minecraft:enchantable/durability` 等），1.21.9+ 自动扩展；若按显式物品列表（如 `supportedItems()` 中硬编码的剑/盔甲清单），需要额外把 copper 变体加入 1.21.9/10 的支持物品集合。spear 物品（`wooden_spear`…`netherite_spear`、`copper_spear`）为 EnchADD 自定义资源，不属于原版物品面，跨版本不受影响。
5. **Loader 依赖可获取**：每个版本的 fabric-api 均有发布版（上表），NeoForge 按 MC 版本有 21.3.x–21.11.x 对应线；Forge 对 1.21.2+ 提供有限版本（Forge 官方线在 1.21.x 后期更新缓慢），新建 Forge 历史模块前需先核对 maven.minecraftforge.net 对应构建号。

## 与 1.21.1 待办的关系

- 仓库中不存在任何 `fabric-1.21.1`/`forge-1.21.1`/`neoforge-1.21.1` 模块源码（`mod/settings.gradle` 仅 include 六个 1.21.11/26.3 模块；`mod/.gradle/loom-cache/projects/fabric-1.21.1` 只有空目录残留）。因此“重试 1.21.1 Fabric 校验与构建”和“隔离 Forge 1.21.1 配置”两个待办的前置条件不成立——不是 Maven 或 Gradle 阻塞，而是模块本身从未进入当前源码树。1.21.1 需要独立模块（pack_format 48/65 世代、fabric-api ≤0.116.x+1.21.1、NeoForge 21.1.x ≤21.1.264、Forge ≤52.x）。
- 三个 Maven 均已恢复（HTTP 200）：fabric-api `0.116.17+1.21.1` 可解析，NeoForge `21.1.x` 元数据可解析。若后续决定重建 1.21.1 模块，网络条件已具备；在重建之前，不恢复任何 1.21.1 支持声明（遵守约定）。
