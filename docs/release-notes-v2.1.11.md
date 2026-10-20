# EnchADD 2.1.11

发布日期：2026-10-20

## 更新内容

- Cadence 与 Underdog 的 Mod 附魔物品范围现在与 Paper 一致，仅支持剑、斧和三叉戟。
- Rally 的 Mod 附魔范围与槽位现在限定为胸甲和胸部装备槽。
- 为 Rebound、Insight、Debilitate、Cadence、Underdog 和 Rally 增加资源结构及六 Loader 适配器校验。
- 保留本版本实际游戏触发状态为未验证；构建通过不代表玩家行为验证通过。

## 构建验证

- Fabric、Forge、NeoForge 的 `1.21.11` 与 `26.3` 六个模块均使用 JDK 25.0.3、Gradle 9.7.1 构建成功，资源与适配器校验通过。
- Paper protected-release 使用 JDK 21 执行 `clean verify`，322 项测试通过，0 失败、0 错误、0 跳过。
- Mod JAR 的 SHA-256 收录在本次发布的 `SHA256SUMS.txt` 和 `docs/build-evidence.md`。

## 下载

请在 GitHub Releases 中按 Loader 和 Minecraft 版本选择对应 JAR。`1.21.11` 使用 Java 21，`26.3` 使用 Java 25。Paper 插件与 Mod 模组是不同服务端实现，请勿同时安装。
