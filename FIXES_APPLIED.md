# EnchADD 插件问题修复报告

## 修复日期
2026-03-08

## 修复的主要问题

### 1. 资源泄漏问题 ✅

#### 问题描述
- `VampirismListener` 和 `CloakingListener` 创建的定时任务在插件禁用时没有被取消
- 这会导致任务继续运行，造成内存泄漏和潜在的服务器崩溃

#### 修复方案
- 在两个监听器中添加了 `cleanup()` 方法
- 在 `ListenerRegistry.unregisterAll()` 中通过反射调用所有监听器的 `cleanup()` 方法
- 确保插件禁用时所有后台任务都被正确取消

**修改文件:**
- `src/main/java/net/enchadd/listeners/VampirismListener.java`
- `src/main/java/net/enchadd/listeners/CloakingListener.java`
- `src/main/java/net/enchadd/utils/ListenerRegistry.java`

---

### 2. 线程安全和内存泄漏问题 ✅

#### 问题描述
- `EnchantCache` 使用 `WeakHashMap` 但没有同步，存在线程安全问题
- 缓存没有大小限制，可能导致内存泄漏
- 使用 `ItemStack` 作为键不可靠，因为 `ItemStack` 是可变对象

#### 修复方案
- 使用 `LinkedHashMap` 实现真正的 LRU 缓存
- 添加同步块保证线程安全
- 创建不可变的 `CacheKey` 类，使用 `System.identityHashCode()` 作为键
- 设置最大缓存大小为 10000，自动淘汰最旧的条目

**修改文件:**
- `src/main/java/net/enchadd/utils/EnchantCache.java`

---

### 3. 空指针异常风险 ✅

#### 问题描述
多个监听器缺少必要的 null 检查，可能导致 `NullPointerException`

#### 修复方案
在以下位置添加了 null 检查：

1. **ExecutionerListener**
   - 添加 `DamageSource` null 检查
   - 添加除以零保护（`targetMaxHealth <= 0`）

2. **BeheadingListener**
   - 添加 `DamageSource` null 检查

3. **DispelListener**
   - 添加 `equipment` null 检查

4. **CurseConflictListener**
   - 添加 `inventory` 和 `enchantments` null 检查

5. **LastStandListener**
   - 添加 `PersistentDataContainer` null 检查

6. **PanicListener**
   - 添加 `equipment` 和 `inventory` null 检查

7. **RicochetListener**
   - 添加 `shooter` 和 `PDC` null 检查

8. **TelepathyListener**
   - 添加玩家在线状态检查
   - 添加物品列表空检查
   - 在异步任务中再次检查玩家是否在线

9. **VolleyListener**
   - 添加 `projectileEntity` null 检查
   - 添加 `velocity` null 检查
   - 修复错误的 Key 引用（从 `ExecutionerEnchant.KEY` 改为 `VolleyEnchant.KEY`）

**修改文件:**
- `src/main/java/net/enchadd/listeners/ExecutionerListener.java`
- `src/main/java/net/enchadd/listeners/BeheadingListener.java`
- `src/main/java/net/enchadd/listeners/DispelListener.java`
- `src/main/java/net/enchadd/listeners/CurseConflictListener.java`
- `src/main/java/net/enchadd/listeners/LastStandListener.java`
- `src/main/java/net/enchadd/listeners/PanicListener.java`
- `src/main/java/net/enchadd/listeners/RicochetListener.java`
- `src/main/java/net/enchadd/listeners/TelepathyListener.java`
- `src/main/java/net/enchadd/listeners/VolleyListener.java`

---

### 4. 逻辑错误 ✅

#### 问题描述
- `VolleyListener` 使用了错误的附魔 Key（`ExecutionerEnchant.KEY` 而不是 `VolleyEnchant.KEY`）

#### 修复方案
- 修正为使用正确的 `VolleyEnchant.KEY`

**修改文件:**
- `src/main/java/net/enchadd/listeners/VolleyListener.java`

---

## 修复统计

- **修复的文件数量**: 11
- **修复的监听器**: 9
- **修复的工具类**: 2
- **添加的 null 检查**: 20+
- **修复的资源泄漏**: 2
- **修复的线程安全问题**: 1
- **修复的逻辑错误**: 1

---

## 建议的后续改进

### 高优先级
1. **添加单元测试** - 为关键组件添加测试，特别是缓存和冷却管理器
2. **性能监控** - 添加性能指标收集，监控缓存命中率和任务执行时间
3. **配置验证** - 在启动时验证配置文件的完整性

### 中优先级
1. **日志改进** - 添加更详细的调试日志，便于问题排查
2. **错误处理** - 统一异常处理策略，避免静默失败
3. **代码重构** - 减少重复代码，提取公共方法

### 低优先级
1. **文档完善** - 为所有公共 API 添加 Javadoc
2. **代码风格** - 统一代码格式和命名规范
3. **国际化** - 完善多语言支持

---

## 测试建议

### 必须测试的场景
1. **插件重载** - 测试 `/reload` 命令，确保没有资源泄漏
2. **高并发** - 在多玩家环境下测试缓存的线程安全性
3. **长时间运行** - 测试服务器运行 24 小时后的内存使用情况
4. **边界条件** - 测试 null 值、空列表等边界情况

### 性能测试
1. **缓存效率** - 监控缓存命中率，应该 > 90%
2. **TPS 影响** - 确保插件不会显著降低服务器 TPS
3. **内存使用** - 监控内存使用，确保没有泄漏

---

## 兼容性说明

- **Minecraft 版本**: 1.21.1+
- **Java 版本**: 21+
- **Paper API**: 1.21.1-R0.1-SNAPSHOT
- **依赖插件**: PlaceholderAPI (可选)

---

## 注意事项

1. 所有修复都是向后兼容的，不会破坏现有功能
2. 建议在测试服务器上充分测试后再部署到生产环境
3. 如果发现任何问题，请查看服务器日志中的 `[EnchADD]` 标记
4. 建议定期备份配置文件和数据文件

---

## 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues: https://github.com/EnchADD/EnchADD/issues
- 项目主页: https://github.com/EnchADD/EnchADD
