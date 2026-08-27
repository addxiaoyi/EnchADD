# EnchADD 受保护发布流程

本文档记录插件防破解、防篡改和反编译成本提升的当前实现。它不是绝对防破解方案；Java 插件在客户机器上运行，攻击者始终能拿到字节码。这里的目标是提高篡改、二开盗卖、直接反编译复制的成本，并让正式服能快速发现不可信 jar。

## 当前保护层

1. 默认构建不再附带 `*-sources.jar`，避免把源码直接随发布物输出。
2. `PluginProtection` 在 Paper bootstrap 和 Bukkit enable 两个阶段执行。
3. 启动时检查 jar 形态，确认 `paper-plugin.yml`、`plugin.yml`、主类和 bootstrapper 存在。
4. 严格模式会拒绝明显不适合生产的来源，例如展开的 classes 目录、源文件或 Maven 坐标元数据被打进发布 jar。
5. 可通过 SHA-256 绑定发布包，检测被替换或篡改的 jar。
6. 可检测常见调试和字节码注入参数，例如 `-javaagent`、`-agentlib:jdwp`、`-Xdebug`。
7. `protected-release` Maven profile 会额外产出混淆 jar 和对应 SHA-256 文件。

## 构建命令

普通开发构建：

```powershell
mvn -pl plugin -am clean package
```

受保护发布构建：

```powershell
mvn -pl plugin -am -Pprotected-release clean verify
```

受保护 jar 输出位置：

```text
plugin/target/enchadd-plugin-<version>-protected.jar
plugin/target/enchadd-plugin-<version>-protected.jar.sha256
```

## 生产哈希绑定

发布前读取 `.sha256` 文件中的 64 位哈希，并在服务器启动参数或环境变量中配置其一：

```powershell
$env:ENCHADD_EXPECTED_SHA256 = "<protected-jar-sha256>"
```

或 JVM 参数：

```text
-Denchadd.expectedSha256=<protected-jar-sha256>
```

哈希不匹配时，插件会阻止启动。

## 调试和灰度开关

默认严格模式开启：

```text
-Denchadd.protection.strict=true
```

本地 IDE 或测试环境需要从 classes 目录启动时可临时允许：

```text
-Denchadd.protection.allowExploded=true
```

需要远程调试或使用 Java agent 时可临时允许：

```text
-Denchadd.protection.allowInstrumentation=true
```

这些开关只应在开发、排障或内测环境使用。

## 保留入口

混淆构建保留以下入口，避免破坏 Paper/Bukkit/PlaceholderAPI 运行：

- `net.enchadd.EnchADD`
- `net.enchadd.EnchADDBootstrap`
- `net.enchadd.security.PluginProtection`
- 继承 `PlaceholderExpansion` 的扩展类公开方法
- 带 `@EventHandler` 的事件处理方法
- enum 的 `values` 和 `valueOf`

## 后续增强方向

- 商业发布可接入服务器端授权签名，用设备指纹、购买凭证和短期 token 做二次校验。
- 可引入私有构建流水线，把期望哈希自动写入部署系统，不手动复制。
- 重要算法可拆到远端服务或原生库，但会增加运维复杂度和兼容性风险。
