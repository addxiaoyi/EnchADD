# EnchADD 兼容性

## 支持范围

- 服务端：Paper `1.21.6` 至 `1.21.11`，以及 Paper `26.2`。
- Java：Paper `1.21.6` 至 `1.21.11` 使用 Java 21；Paper `26.1+` 使用 Java 25。
- Spigot/CraftBukkit：不支持。插件使用 Paper 的 bootstrap、lifecycle 和数据驱动附魔注册 API。
- `1.21.5` 及更低版本：不支持。这些版本缺少当前附魔注册所需的 API。

发布包使用最低支持的 Paper API 编译。这避免在高版本 API 上编译后，将不存在的新方法带到较低版本服务端。

## 验证新版本

`paper.version` 可从命令行覆盖，不需要修改 POM：

```powershell
mvn -pl plugin -am -Dpaper.version=1.21.11-R0.1-SNAPSHOT clean verify
```

使用仓库中记录的最新稳定 Paper API：

```powershell
mvn -pl plugin -am -Platest-paper-api clean verify
```

每个新 Minecraft/Paper 小版本发布后，至少执行：

1. 使用默认的最低 API 版本运行 `clean verify`。
2. 覆盖 `paper.version` 为待支持的最新版本，再运行 `clean verify`。
3. 使用对应 Paper 版本要求的 Java 运行时。
4. 在最低和最新版本的真实 Paper 服务端上启动，检查附魔注册、监听器和命令。

Paper `26.2` 的官方 API 已升级到 Adventure 5。EnchADD 的翻译注册使用 Adventure 4/5 共有的 `TranslationStore` API，同一个按 `1.21.6` 基线编译的 jar 可在两代运行。

## 旧客户端连接新服务端

EnchADD 不处理 Minecraft 网络协议。需要让旧客户端连接高版本服务端时，应在服务端使用 ViaVersion/ViaBackwards 等协议适配插件，并单独测试自定义附魔的物品显示和交互。

未经过构建和真实服务端启动验证的未来版本，不应标记为已支持。
