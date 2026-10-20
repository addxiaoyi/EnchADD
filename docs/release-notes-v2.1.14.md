# EnchADD 2.1.14

发布日期：2026-10-20（源码静态 parity 更新）

## 本次更新

- 修正 `Afterglide` 和 `Skim` 的 Mod 资源约束：仅支持 Elytra，使用胸甲槽位。
- 修正 `Tideshell` 的 Mod 资源约束：仅支持鹦鹉螺壳，使用主手槽位，等级上限为 I。
- 修正 `Fleetfoot`、`TideRunner` 和 `Lethargy` 的 Mod 资源约束：使用脚部护甲标签、脚部槽位和 Paper 对应的 III 级上限。
- 为六个平台增加 Common 行为和 Loader 入口静态断言，防止共享玩家状态、水下使用和近战入口回退。

## 验证边界

- 六个平台的窄资源/适配器校验全部通过，记录在 `docs/validation-logs/v2.1.14-static-validation-20261020.txt`。
- 这次校验没有把源码静态通过升级为 Mod JAR 构建成功，也没有升级为服务器启动或玩家行为成功。
- 六个正式 Loader JAR 需要外部 Maven 依赖恢复后重新构建；当前没有生成新的 v2.1.14 JAR SHA-256 或 GitHub Release 资产。
- 玩家触发行为仍为 `NOT_VERIFIED`。
