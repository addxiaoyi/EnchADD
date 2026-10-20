# EnchADD 2.1.12

发布日期：2026-10-20

## 更新内容

- Initiative、Wingclip、Tracer 和 Pursuit 的 Mod 物品范围与 Paper 对齐，限定为剑、斧和三叉戟。
- 六个 Loader 的资源校验现在覆盖目标生命值、浮空、发光、冲刺玩家条件及伤害上限。
- 六个平台的共享近战伤害链和注册入口校验通过。

## 构建验证

- Fabric、Forge、NeoForge 的 `1.21.11` 和 `26.3` 六个 Mod 模块均以 JDK 25.0.3、Gradle 9.7.1 构建成功，资源和适配器校验通过。
- Paper protected-release 使用 JDK 21 执行 `clean verify`，322 项测试通过，0 失败、0 错误、0 跳过。
- 七个发布 JAR 的 SHA-256 收录于 `SHA256SUMS.txt` 和 `docs/build-evidence.md`。

## 行为验证范围

本次构建与资源、注册链路校验不等同于实际玩家行为验证。四项附魔的真实游戏触发结果仍为 `NOT_VERIFIED`。

安装时请按 Loader 和 Minecraft 版本选择对应 JAR。`1.21.11` 使用 Java 21，`26.3` 使用 Java 25。Paper 插件与 Mod 模组是不同服务端实现，请勿同时安装。
