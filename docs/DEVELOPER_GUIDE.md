# EnchAdd 开发者指南

## 目录

1. [简介](#简介)
2. [项目结构](#项目结构)
3. [开发环境设置](#开发环境设置)
4. [核心架构](#核心架构)
5. [创建新附魔](#创建新附魔)
6. [冲突规则](#冲突规则)
7. [事件系统](#事件系统)
8. [性能优化](#性能优化)
9. [测试](#测试)
10. [贡献指南](#贡献指南)

---

## 简介

EnchAdd 是一个模块化的 Minecraft 附魔扩展插件，采用现代 Java 开发实践，提供高度可扩展的架构。本指南面向希望为项目贡献代码或创建自定义附魔的开发者。

### 技术栈

- **语言**: Java 11+
- **构建工具**: Gradle 8.5
- **框架**: Bukkit/Spigot API
- **测试框架**: JUnit 5
- **依赖管理**: Google Guava

---

## 项目结构

```
enchadd/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/enadd/
│   │   │       ├── core/                    # 核心系统
│   │   │       │   ├── api/               # API 接口
│   │   │       │   ├── cache/             # 缓存系统
│   │   │       │   ├── conflict/          # 冲突管理
│   │   │       │   ├── dataexport/         # 数据导出
│   │   │       │   ├── enchantment/       # 附魔效果
│   │   │       │   ├── event/             # 事件处理
│   │   │       │   ├── monitor/           # 性能监控
│   │   │       │   ├── optimize/          # 优化工具
│   │   │       │   ├── registry/          # 注册表
│   │   │       │   └── visualization/     # 可视化
│   │   │       ├── enchantments/           # 附魔实现
│   │   │       │   ├── armor/            # 护甲附魔
│   │   │       │   ├── combat/           # 战斗附魔
│   │   │       │   ├── defense/          # 防御附魔
│   │   │       │   ├── special/          # 特殊附魔
│   │   │       │   ├── tool/             # 工具附魔
│   │   │       │   └── utility/          # 实用附魔
│   │   │       ├── gui/                  # GUI 系统
│   │   │       ├── util/                 # 工具类
│   │   │       ├── config/               # 配置管理
│   │   │       └── EnchantmentExpansionPlugin.java
│   │   └── resources/
│   │       ├── plugin.yml
│   │       ├── config.yml
│   │       └── enchantments.yml
│   └── test/
│       └── java/
│           └── com/enadd/
│               └── core/
│                   └── conflict/
│                       └── EnchantmentConflictManagerTest.java
├── docs/                               # 文档
├── build.gradle                         # 构建配置
└── gradle.properties                    # Gradle 属性
```

---

## 开发环境设置

### 前置要求

- JDK 11 或更高版本
- IDE (IntelliJ IDEA 或 Eclipse)
- Git
- Gradle 8.5

### 克隆项目

```bash
git clone https://github.com/your-repo/enchadd.git
cd enchadd
```

### 导入到 IDE

#### IntelliJ IDEA

1. 打开 IntelliJ IDEA
2. 选择 "File" → "Open"
3. 选择项目根目录
4. 等待 Gradle 同步完成

#### Eclipse

1. 运行 `./gradlew eclipse`
2. 打开 Eclipse
3. 选择 "File" → "Import"
4. 选择 "Existing Projects into Workspace"
5. 选择项目目录

### 构建项目

```bash
# 清理构建
./gradlew clean

# 构建项目
./gradlew build

# 跳过测试构建
./gradlew build -x test

# 运行测试
./gradlew test
```

---

## 核心架构

### 1. EnchantmentRegistry（附魔注册表）

**位置**: `com.enadd.core.registry.EnchantmentRegistry`

**职责**: 管理所有附魔的注册、查询和冲突检测

**关键方法**:

```java
// 注册附魔
public static void register(String id, Enchantment enchantment)

// 获取附魔
public static Enchantment getEnchantment(String id)

// 获取所有附魔
public static Map<String, EnchantmentInfo> getAllEnchantments()

// 检查冲突
public static boolean areEnchantmentsCompatible(String id1, String id2)
```

**使用示例**:

```java
// 注册新附魔
EnchantmentRegistry.register("my_enchantment", new MyEnchantment());

// 获取附魔信息
EnchantmentInfo info = EnchantmentRegistry.getEnchantmentInfo("my_enchantment");
```

### 2. EnchantmentConflictManager（冲突管理器）

**位置**: `com.enadd.core.conflict.EnchantmentConflictManager`

**职责**: 管理附魔之间的冲突规则

**关键方法**:

```java
// 检查是否冲突
public boolean areConflicting(String enchant1, String enchant2)

// 获取冲突列表
public Set<String> getConflicts(String enchantmentId)

// 获取所有冲突规则
public Map<String, Set<String>> getConflictRules()
```

### 3. EnchantmentEffect（附魔效果系统）

**位置**: `com.enadd.core.enchantment.*`

**职责**: 实现附魔的具体效果

**核心接口**:

```java
public interface EnchantmentEffect {
    void apply(EffectContext context);
    
    String getName();
    
    String getDescription();
}
```

**EffectContext**:

```java
public class EffectContext {
    private final EffectTrigger trigger;
    private final Event event;
    private final int level;
    private final Entity target;
    
    // 触发类型
    public enum EffectTrigger {
        ATTACK, DEFEND, INTERACT, PASSIVE
    }
}
```

---

## 创建新附魔

### 步骤 1: 创建附魔类

```java
package com.enadd.enchantments.combat;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MyEnchantment extends Enchantment {
    
    public MyEnchantment() {
        super(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{
            EquipmentSlot.MAINHAND
        });
    }
    
    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }
    
    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }
    
    @Override
    public int getMaxLevel() {
        return 3;
    }
    
    @Override
    public boolean isTreasureOnly() {
        return false;
    }
    
    @Override
    public boolean isCurse() {
        return false;
    }
}
```

### 步骤 2: 创建效果类

```java
package com.enadd.core.enchantment;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MyEffect implements EnchantmentEffect {
    
    private static final double DAMAGE_MULTIPLIER = 0.15;
    
    @Override
    public void apply(EffectContext context) {
        // 只在攻击时触发
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return;
        }
        
        // 检查事件类型
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return;
        }
        
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        
        // 检查攻击者是否为玩家
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        int level = context.getLevel();
        
        // 计算额外伤害
        double extraDamage = event.getDamage() * DAMAGE_MULTIPLIER * level;
        event.setDamage(event.getDamage() + extraDamage);
        
        // 播放粒子效果
        spawnParticles(player);
    }
    
    @Override
    public String getName() {
        return "我的附魔";
    }
    
    @Override
    public String getDescription() {
        return "每次攻击造成额外伤害";
    }
    
    private void spawnParticles(Player player) {
        player.getWorld().spawnParticle(
            org.bukkit.Particle.CRIT,
            player.getLocation(),
            10,
            0.5, 0.5, 0.5,
            0.1
        );
    }
}
```

### 步骤 3: 注册附魔

在 `EnchantmentRegistry.java` 中添加注册代码：

```java
private void registerCombatEnchantments() throws Exception {
    // 现有附魔...
    
    // 添加新附魔
    safeRegister("my_enchantment", () -> new com.enadd.enchantments.combat.MyEnchantment());
}
```

### 步骤 4: 添加冲突规则（如需要）

在 `EnchantmentConflictManager.java` 中添加冲突规则：

```java
private void initializeConflictRules() {
    // 现有规则...
    
    // 添加新冲突组
    addConflictGroup("combat", Arrays.asList(
        "my_enchantment", "similar_enchantment"
    ));
}
```

### 步骤 5: 添加配置

在 `enchantments.yml` 中添加配置：

```yaml
enchantments:
  combat:
    my_enchantment:
      enabled: true
      max-level: 3
      rarity: rare
```

---

## 冲突规则

### 添加冲突规则

冲突规则定义了哪些附魔不能同时应用。

```java
// 在 EnchantmentConflictManager 中
addConflictGroup("combat", Arrays.asList(
    "critical_strike", "precision_strike", "execution"
));
```

### 冲突规则分类

1. **战斗附魔冲突**: 相似效果的战斗附魔
2. **护甲附魔冲突**: 相似效果的护甲附魔
3. **工具附魔冲突**: 相似效果的工具附魔
4. **装饰附魔冲突**: 视觉效果冲突
5. **特殊附魔冲突**: 特殊效果冲突

### 检查冲突

```java
// 检查两个附魔是否冲突
boolean conflicts = EnchantmentConflictManager.getInstance()
    .areConflicting("enchant1", "enchant2");

// 获取附魔的所有冲突
Set<String> conflicts = EnchantmentConflictManager.getInstance()
    .getConflicts("enchant1");
```

---

## 事件系统

### 监听附魔事件

```java
package com.enadd.core.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EnchantmentEventHandler implements Listener {
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // 获取攻击者
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        
        // 检查玩家装备的附魔
        player.getInventory().getItemInMainHand().getEnchantments()
            .forEach((enchantment, level) -> {
                // 触发附魔效果
                EnchantmentRegistry.triggerEnchantment(
                    enchantment.getKey().getKey(),
                    level,
                    EffectTrigger.ATTACK,
                    event
                );
            });
    }
}
```

### 注册事件监听器

在主插件类中：

```java
@Override
public void onEnable() {
    // 注册事件监听器
    getServer().getPluginManager().registerEvents(
        new EnchantmentEventHandler(),
        this
    );
}
```

---

## 性能优化

### 1. 使用缓存

```java
// 使用两级缓存
TwoLevelCache<String, Boolean> cache = new TwoLevelCache<>(100, 1000);

// 检查缓存
Boolean cached = cache.get("key");
if (cached != null) {
    return cached;
}

// 计算并缓存
Boolean result = expensiveOperation();
cache.put("key", result);
return result;
```

### 2. 异步处理

```java
// 异步加载附魔
Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
    // 执行耗时操作
    loadEnchantments();
});

// 异步保存数据
Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
    saveData();
});
```

### 3. 批量处理

```java
// 批量注册附魔
List<Runnable> tasks = Arrays.asList(
    () -> registerCombatEnchantments(),
    () -> registerArmorEnchantments(),
    () -> registerToolEnchantments()
);

tasks.parallelStream().forEach(Runnable::run);
```

---

## 测试

### 单元测试

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyEnchantmentTest {
    
    @Test
    void testEnchantmentEffect() {
        MyEffect effect = new MyEffect();
        EffectContext context = createMockContext();
        
        effect.apply(context);
        
        // 验证效果
        assertTrue(effectApplied(context));
    }
    
    @Test
    void testConflictDetection() {
        boolean conflicts = EnchantmentConflictManager.getInstance()
            .areConflicting("enchant1", "enchant2");
        
        assertTrue(conflicts);
    }
}
```

### 集成测试

```java
import org.junit.jupiter.api.Test;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class IntegrationTest {
    
    @Test
    void testEnchantmentApplication() {
        Player player = Bukkit.getPlayer("TestPlayer");
        
        // 应用附魔
        EnchantmentRegistry.applyEnchantment(
            player,
            "my_enchantment",
            1
        );
        
        // 验证附魔已应用
        assertTrue(player.getInventory().getItemInMainHand()
            .containsEnchantment("my_enchantment"));
    }
}
```

### 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests MyEnchantmentTest

# 生成测试报告
./gradlew test jacocoTestReport
```

---

## 贡献指南

### 代码规范

1. **命名约定**:
   - 类名：PascalCase (e.g., `MyEnchantment`)
   - 方法名：camelCase (e.g., `applyEffect`)
   - 常量：UPPER_SNAKE_CASE (e.g., `MAX_LEVEL`)

2. **注释**:
   - 所有公共方法必须有 JavaDoc
   - 复杂逻辑需要行内注释
   - 不要注释显而易见的代码

3. **格式化**:
   - 使用 4 空格缩进
   - 每行最大 120 字符
   - 使用 IDE 自动格式化

### 提交 Pull Request

1. Fork 项目仓库
2. 创建功能分支：`git checkout -b feature/my-feature`
3. 提交更改：`git commit -m "Add my feature"`
4. 推送到分支：`git push origin feature/my-feature`
5. 创建 Pull Request

### PR 模板

```markdown
## 描述
简要描述此 PR 的目的和内容。

## 更改类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 性能优化

## 测试
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试完成

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 已添加必要的文档
- [ ] 已更新测试用例
- [ ] 无编译警告
```

---

## 常见问题

### Q: 如何调试附魔效果？

A: 使用调试日志：

```java
plugin.getLogger().fine("附魔效果触发: " + enchantmentId);
```

### Q: 如何添加自定义配置？

A: 在 `config.yml` 中添加配置项，然后通过 `ConfigManager` 读取：

```java
int maxLevel = ConfigManager.getInt("my_enchantment.max-level", 3);
```

### Q: 如何处理版本兼容性？

A: 使用版本检查：

```java
if (Bukkit.getVersion().contains("1.16")) {
    // 1.16 特定代码
} else {
    // 其他版本代码
}
```

---

## 资源

- **Bukkit API 文档**: https://hub.spigotmc.org/javadocs/spigot/
- **NMS 文档**: https://wiki.vg/Mapping
- **Gradle 文档**: https://docs.gradle.org/

---

**版本**: 1.0.0  
**最后更新**: 2026-02-08
