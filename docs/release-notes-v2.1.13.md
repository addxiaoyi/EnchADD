# EnchADD 2.1.13

发布日期：2026-10-20

## 更新内容

- Executioner 使用 Paper 默认的 5 级上限、25% 目标生命阈值和逐级伤害倍率。
- Beheading 限定为斧类并保持 Paper 的 1 级上限和掉头概率。
- Decapitate 限定为剑与斧，玩家头物品保留被击杀者 profile。
- Beheading 成功为玩家生成头颅后，不再为同一次击杀重复生成 Decapitate 玩家头。
- 六个平台的资源、共享行为与 Loader 注册入口校验通过。

## Compatibility Rules

- `Executioner` conflicts with `Decapitate`; `Beheading` conflicts with vanilla `Looting`, matching the Paper defaults.

## 构建验证

- 六个 Fabric、Forge、NeoForge 模块（Minecraft `1.21.11` 和 `26.3`）均以 JDK 25.0.3 和 Gradle 9.7.1 构建成功。
- Paper protected-release 使用 JDK 21 执行 `clean verify`，322 项测试通过，0 失败、0 错误、0 跳过。
- 七个发布 JAR 的 SHA-256 收录在发布资产 `SHA256SUMS.txt`。

实际玩家触发行为仍标记为 `NOT_VERIFIED`；构建通过不代表游戏内行为验证通过。
