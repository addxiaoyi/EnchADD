# EnchADD 2.1.17

发布日期：2026-10-20（源码静态 parity 更新）

## 本次更新

- 修正 `Bulwark`：资源约束保持盾牌/offhand，并将 Common 行为限制为玩家成功持盾状态，Resistance 改为 Paper 的 I 级。
- 修正 `Clairvoyance`：只允许直接的隐身 LivingEntity 攻击触发，不接受投射物间接来源。
- 修正 `Purify`、`Lucidity`、`Dispel`、`Ricochet`、`Evasion`、`Shroud`、`Freshcatch` 的 Mod 支持物品、槽位和等级资源约束。
- 保持 Forge/NeoForge Ricochet 发射记录修复，并为六平台入口增加回归断言。

## 验证边界

- 六个平台的窄资源/适配器校验全部通过，记录在 `docs/validation-logs/v2.1.16-static-validation-20261020.txt`，当前本批复用同一完整校验命令和环境。
- 本批不把源码静态通过升级为 Mod JAR 构建成功、服务器启动成功或玩家行为成功。
- 六个正式 Loader JAR 仍需要外部 Maven 依赖恢复后重新构建；当前没有生成新的 v2.1.17 JAR SHA-256 或 GitHub Release 资产。
- 玩家触发行为仍为 `NOT_VERIFIED`。
## 2026-10-24 构建补充

- 外部 Maven（Fabric/Forge/NeoForge）已恢复，六个 Loader 的 v2.1.17 正式 JAR 构建成功，日志见 `docs/validation-logs/v2.1.17-six-loader-build-20261024b.txt`。
- 构建中发现并修复 Forge `EnchADDForgeBulwark` 编译错误：新版 Forge EventBus 7 的 `ShieldBlockEvent` 不再提供 `isCanceled()`，移除该检查（事件本身仅在成功格挡时触发，`getBlockedDamage() > 0.0f` 条件保持）。
- JAR 与 SHA-256 位于 `release-assets/v2.1.17/`；服务器加载与玩家行为仍为 `NOT_VERIFIED`，GitHub Release 资产暂不发布。

## 2026-10-24 Fabric 1.21.1 重建补充

- 前置条件（外部 Maven 恢复）达成后，重建 `fabric-1.21.1` 模块并完成正式构建（此前该模块源码从未进入当前源码树）。
- 1.21.1 mojmap 与 1.21.11 的 API 差异通过模块本地源码副本（`mod/fabric-1.21.1/common-1.21.1/`）处理：`Identifier→ResourceLocation`、`projectile.arrow.*→projectile.*`、`advancements.criterion→critereon`、`MobEffects` 旧常量名、`hurtServer→hurt`、`TeleportTransition→DimensionTransition`、`ItemCooldowns` 以 `Item` 为键、`getLastHurtByPlayer→getKillCredit`、`withScaledDuration` 手动展开、`recipeAccess→getRecipeManager`。
- 行为边界：Holdfast 的盾牌禁用抑制依赖 1.21.2+ 的 `BlocksAttacks` 组件，1.21.1 上不可用（仅保留冷却/触发判定）；Nourish 改用 `FOOD` 组件判定；Purify/Retching 挂接 `LivingEntity.eat` 而非 `Consumable.onConsume`。
- 数据包格式 48（1.21.1），pack.mcmeta 已核对；JAR 与 SHA-256 见 `release-assets/v2.1.17/`。
- 服务器加载与玩家行为仍为 `NOT_VERIFIED`。

## 2026-10-24 Forge/NeoForge 1.21.1 重建补充

- `neoforge-1.21.1`（NeoForge 21.1.264）：共享源码副本复用 Fabric 1.21.1 的移植集，注册改走 `NeoForge.EVENT_BUS`（21.1.x 事件模型与 21.11.x 相同），`mods.toml` 版本范围 `[21.1.264,)`/`[1.21.1,1.21.2)`。构建含资源与适配器校验一次通过。
- `forge-1.21.1`（Forge 52.1.20 + ForgeGradle 7.0.40）：57 个适配器从 EventBus 7（`.BUS.addListener` + `eventbus.api.listener.SubscribeEvent`）移植到经典总线（`MinecraftForge.EVENT_BUS.addListener` + 经典 `SubscribeEvent`）；`TickEvent.PlayerTickEvent` 回退 phase 字段；`Pivot` 的 `getBlockedWith()` 在 52.1.20 不可用，改用 `getUseItem()`；移除 eventbus-validator 7.0.5（与新总线版本不匹配）。mavenizer 需 `_JAVA_OPTIONS` 代理环境完成 1.21.1 首次构建。
- 两者行为边界与 Fabric 1.21.1 相同：Holdfast 盾牌禁用抑制不可用；Nourish 用 `FOOD` 组件；Purify/Retching 挂接 `LivingEntity.eat`。
- 九个 JAR 全部位于 `release-assets/v2.1.17/`（SHA256SUMS.txt 已更新）；服务器加载与玩家行为仍为 `NOT_VERIFIED`。

## 2026-10-25 1.21.1 玩家行为验证补充（mineflayer 隔离盾挡测试）

测试方法：生产式服务器（Forge/NeoForge installer 安装，Fabric 为 runServer）+ mineflayer 双 bot（EnchaddDefBot 持盾防守、EnchaddAtkBot 自然近战攻击），RCON 驱动装备/附魔/位置。脚本归档 `tests/modbot/`。

### Fabric 1.21.1（`docs/validation-logs/v2.1.17-fabric-1.21.1-runserver-20261024.log` + `...-shield-behavior-20261025.log`）
- 启动：`Done (9.352s)`，EnchADD 初始化，无 Mixin/注册错误。
- Bulwark：持盾被近战格挡成功 → defender 获得 Resistance ✓
- Breakguard：格挡成立但保留加成穿透（20→18.2）✓
- 低伤害边界：空手 vs 盾 → 完全格挡无伤 ✓
- 盾禁用绕过：斧击禁盾后第二击全额伤害 ✓

### Forge 1.21.1（生产服务器 52.1.20，`...-forge-1.21.1-shield-behavior-20261025.log`）
- 启动：`Done (2.803s)`，无 Mixin 错误。
- 四项行为测试全部 PASS（Bulwark Resistance / Breakguard 穿透 20→18.2 / 低伤害全挡 / 禁盾绕过 20→12.8）。
- **适配器修复**：`EnchADDForgeBreakguard` 从 `LivingHurtEvent.adjustIncoming`（加伤被盾完全吸收，穿透永不生效）改为 `ShieldBlockEvent.setBlockedDamage(reduced)`；`ForgeDurabilityCurseMixin` 目标从 `processDurabilityChange` 修正为 `hurtAndBreak(II...)`。修复后 JAR SHA-256 已更新。

### NeoForge 1.21.1（生产服务器 21.1.264，`...-neoforge-1.21.1-shield-behavior-20261025.log`）
- 启动：`Done (0.733s)`，无 Mixin 错误。
- Bulwark：Resistance 触发 ✓（双通道：LivingDamageEvent.Post + LivingShieldBlockEvent）
- Breakguard：穿透 ✓（适配器从 `LivingIncomingDamageEvent` 改为 `LivingShieldBlockEvent.setBlockedDamage`）
- 低伤害全挡 ✓；禁盾绕过路径由"无盾斧击全额 10.8 伤"基线佐证（mineflayer 连续第二击在该服务器存在同步限制，改用逐场景独立会话验证）。
- `NeoForgeDurabilityCurseMixin` 目标同步修正为 `hurtAndBreak(II...)`。修复后 JAR SHA-256 已更新。

### 版本矩阵补充
- `docs/version-matrix-1.21.2-1.21.10.md` 记录 1.21.2–1.21.10 的 Loader/Java/pack_format/物品面差异。

### v2.1.17 发布条件状态
- 九个 Loader JAR 构建完成，SHA-256 齐全（1.21.1 三个 JAR 因行为测试中的适配器修复而重新构建，SHA 已刷新）。
- Fabric/Forge/NeoForge 1.21.1 的服务器加载与 Bulwark/Breakguard/盾挡边界玩家行为验证通过（隔离 bot 环境）。
- 仍未验证：1.21.1 其余 109 个附魔的玩家行为、1.21.11/26.3 六平台的本批（2.1.17）玩家行为回归、客户端（非 bot）交互。
- 结论：GitHub Release 仍不标记为"完全验证发布"；可将 1.21.1 构件标注为"启动+盾挡行为已验证"发布候选。

## 2026-10-25 附魔抽样回归（fabric-1.21.1，bot 环境补充）
- Immolate：PASS——8/8 次清除冷却后的近战命中均点燃目标（RCON Fire NBT 权威验证；bot 客户端 fire 元数据同步不可信，vanilla fire_aspect 对照确认）。
- Sunder：行为符合设计——无甲目标加成为 0（bonus 随目标护甲点数缩放）；带甲目标的加成数值需属性精确的测试对象，bot 属性同步不可用，标 PARTIAL。
- Thirst / Volley：bot 元数据（food/箭实体）不可测，1.21.1 保持 NOT_VERIFIED。

## 2026-10-26：forge-1.21.6 构建完成——1.21.x 全版本覆盖达成

- mavenizer 1.21.6-56.0.9 管线完成（vineflower-1.11.1 与 mcp_config zip 的损坏缓存用 curl 完整下载修复后通过）。
- forge-1.21.6 JAR 已构建并上传（SHA-256 见 SHA256SUMS.txt）；PlayerTick 适配器采用经典事件 + END-phase 过滤。
- **最终覆盖：14 个 Loader 组合，1.21.1-1.21.11 + 26.3 全版本**：

| MC 版本 | Fabric | Forge | NeoForge |
| --- | --- | --- | --- |
| 1.21.1 | ✓ | ✓ 52.1.20 | ✓ 21.1.264 |
| 1.21.2-1.21.4 | ✓ fabric-1.21.2 | ✓ forge-1.21.3 (53.1.16) | ✓ neoforge-1.21.3 (21.3.100) |
| 1.21.5-1.21.10 | ✓ fabric-1.21.6 | ✓ forge-1.21.6 (56.0.9) | ✓ neoforge-1.21.8 (21.8.57，覆盖 21.8/21.10) |
| 1.21.11 | ✓ | ✓ 61.1.1 | ✓ 21.11.45 |
| 26.3 | ✓ | ✓ 66.0.9 | ✓ |

- 未发布 loader 版本（1.21.2 无 Forge/NeoForge，1.21.6/1.21.7/1.21.9 无 NeoForge）为上游官方跳版，非本项目缺口。
