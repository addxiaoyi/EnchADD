# EnchADD - 高性能自定义附魔插件

EnchADD 是一款专为 Paper 1.21.10–1.21.11 服务器设计的极速自定义附魔插件。它不仅提升了服务器的游戏性，还通过深度优化确保了在高并发环境下的极致性能。

## 🌟 核心特性

- **极致性能**：采用附魔结果缓存（Enchantment Caching）机制，大幅降低高频事件中的 CPU 损耗。
- **全方位汉化**：插件消息、附魔描述及配置文件均已完美中文化。
- **高度可配置**：支持自定义附魔冲突组、权重、基础花费及等级属性。
- **现代架构**：基于 Abstract Base Class 重构，代码结构清晰，易于维护与扩展。
- **异步处理**：所有文件 IO 和导出操作均在独立线程执行，确保主线程永不掉帧。

## 🛠️ 构建与发布

- **本地构建**：运行 `mvn clean package` 即可生成插件。
- **发布版本**：当前版本 `2.0.5`。

## 🧪 开发与测试

- **单元测试**：集成了 JUnit 5，支持对核心逻辑进行自动化验证。
- **调试模式**：在 `config.yml` 中开启 `debug: true` 即可获取详细的实时运行日志。

## 🚀 灰度上线与回滚

- **灰度发布（Canary）**：`./scripts/rollout-canary.ps1 -PluginJar <jar> -CanaryPluginsDir <server/plugins>`
- **灰度后正式发布**：`./scripts/rollout-canary.ps1 -PluginJar <jar> -CanaryPluginsDir <canary/plugins> -ProductionPluginsDir <prod/plugins> -Promote`
- **一键回滚**：`./scripts/rollback-enchadd.ps1 -PluginsDir <server/plugins>`
- 脚本会自动备份到 `plugins/.enchadd-backups/` 并写入部署元数据 `EnchADD-rollout.json`。

## 📈 运行时监控告警

`config.yml` 支持 `runtime-monitoring`（自动生成默认值）：

- `enabled`
- `sample-interval-seconds`
- `min-tps`
- `max-errors-per-minute`
- `max-trigger-rate-per-minute`
- `max-particle-window-drop-rate`
- `max-stats-pending`

插件会输出 `[ENCHADD-ALERT]` 与扩展后的 `[ENCHADD-CI]` 指标，用于线上告警与 CI 门禁。

## GitHub 更新检查

在 `config.yml` 启用后，插件会在启动时异步查询 GitHub 最新稳定 Release，并在控制台提示可用的新版本：

```yaml
update-checker:
  enabled: true
  repository: EnchADD/EnchADD
  timeout-seconds: 8
```

检查器不会自动下载、覆盖或热加载 JAR。管理员应从日志中的 GitHub Release 页面获取受保护发布包，核验 SHA-256 后按正常流程重启部署。

---

*EnchADD - 为你的服务器增添无限可能。*
