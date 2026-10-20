# EnchADD 2.1.15

发布日期：2026-10-20（源码静态 parity 更新）

## 本次更新

- 修正 `Tenderstep`：脚部护甲、脚部槽位、I 级上限。
- 修正 `Steadfast`：脚部护甲和脚部槽位。
- 修正 `Gravitation`：脚部护甲、脚部槽位、III 级上限。
- 修正 `Brittle`：六种腿甲、腿部槽位、III 级上限。
- 修正 `Airbag`：Elytra、胸甲槽位、IV 级上限。
- 修正 `Wingguard`：Elytra、胸甲槽位、II 级上限。
- 为六个平台增加装备保护、击退、踩踏、Elytra 救援和共享坠落伤害入口断言。

## 验证边界

- 六个平台的窄资源/适配器校验全部通过，记录在 `docs/validation-logs/v2.1.15-static-validation-20261020.txt`。
- 这次校验没有把源码静态通过升级为 Mod JAR 构建成功，也没有升级为服务器启动或玩家行为成功。
- 六个正式 Loader JAR 仍需要外部 Maven 依赖恢复后重新构建；当前没有生成新的 v2.1.15 JAR SHA-256 或 GitHub Release 资产。
- 玩家触发行为仍为 `NOT_VERIFIED`。
