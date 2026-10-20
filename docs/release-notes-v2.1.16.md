# EnchADD 2.1.16

发布日期：2026-10-20（源码静态 parity 更新）

## 本次更新

- 修正 `Bulwark`：盾牌、offhand 槽位和共享格挡入口。
- 修正 `Purify`、`Lucidity`、`Clairvoyance`：头盔支持物品和 head 槽位。
- 修正 `Dispel`：剑/斧支持物品和 mainhand 槽位。
- 修正 `Ricochet`：弓/弩支持物品和 mainhand 槽位。
- 修正 `Evasion`：脚部护甲标签和 feet 槽位。
- 修正 `Shroud`：护腿支持物品和 legs 槽位。
- 修正 `Freshcatch`：钓鱼竿支持物品、mainhand 槽位和 II 级上限。
- 补齐 Forge/NeoForge Ricochet 的发射记录：实体加入世界时调用 `RicochetBehavior.recordLaunch`，命中时再调用 `onHit`。
- 为六个平台增加本批 Common/Loader 入口断言。

## 验证边界

- 六个平台的窄资源/适配器校验全部通过，记录在 `docs/validation-logs/v2.1.16-static-validation-20261020.txt`。
- 这次校验没有把源码静态通过升级为 Mod JAR 构建成功，也没有升级为服务器启动或玩家行为成功。
- 六个正式 Loader JAR 仍需要外部 Maven 依赖恢复后重新构建；当前没有生成新的 v2.1.16 JAR SHA-256 或 GitHub Release 资产。
- 玩家触发行为仍为 `NOT_VERIFIED`。