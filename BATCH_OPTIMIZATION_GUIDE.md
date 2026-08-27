# 批量优化指南

## 已完成的优化

### 第一批 (2个文件) ✅

1. BindListener.java ✅
2. BulwarkListener.java ✅

### 优化模式

每个监听器需要应用以下优化模式:

```java
// 1. 添加配置缓存字段
private final XxxEnchant config;

// 2. 在构造函数中初始化配置
public XxxListener() {
    Object enchantObj = EnchADDConfig.ENCHANTS.get(XxxEnchant.KEY);
    this.config = (enchantObj instanceof XxxEnchant) ? (XxxEnchant) enchantObj : null;
}

// 3. 在事件处理方法开始检查
if (enchant == null || config == null) return;

// 4. 替换 getEnchantmentLevel
// 旧代码:
int level = item.getEnchantmentLevel(enchant);
// 新代码:
int level = PerformanceUtils.getEnchantLevel(item, enchant);

// 5. 替换冷却检查
// 旧代码:
Long last = pdc.get(key, PersistentDataType.LONG);
if (last != null && System.currentTimeMillis() - last < 50L * config.getCooldownTicks()) return;
// 新代码:
if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

// 6. 替换冷却设置
// 旧代码:
pdc.set(key, PersistentDataType.LONG, System.currentTimeMillis());
// 新代码:
PerformanceUtils.setCooldown(pdc, key);

// 7. 替换概率检查
// 旧代码:
if (ThreadLocalRandom.current().nextDouble() >= chance) return;
// 新代码:
if (!PerformanceUtils.rollChance(chance)) return;

// 8. 替换持续时间计算
// 旧代码:
int durationTicks = Math.max(1, level * config.getSecondsPerLevel()) * 20;
// 新代码:
int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSecondsPerLevel(), level);

// 9. 使用安全的工具方法
// 旧代码:
EntityEquipment equipment = entity.getEquipment();
if (equipment == null) return;
// 新代码:
EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
if (equipment == null) return;

// 10. 使用安全的 PDC 获取
// 旧代码:
PersistentDataContainer pdc = entity.getPersistentDataContainer();
// 新代码:
PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
if (pdc == null) return;
```

## 待优化文件列表 (按优先级)

### P0 - 高频事件监听器 (立即优化)

1. **ExecutionerListener.java** - 伤害事件
2. **VampirismListener.java** - 定时任务
3. **TelepathyListener.java** - 方块掉落
4. **SoulboundListener.java** - 死亡事件
5. **EvasionListener.java** - 伤害事件
6. **LastStandListener.java** - 伤害事件
7. **PanicListener.java** - 伤害事件 + 特殊优化
8. **ShadowstrikeListener.java** - 伤害事件

### P1 - 中频事件监听器 (本周优化)

1. **FlareListener.java**
2. **HemorrhageListener.java**
3. **HuntersMarkListener.java**
4. **TremorListener.java**
5. **DispelListener.java**
6. **ClairvoyanceListener.java**
7. **FrostbrandListener.java**
8. **ImmolateListener.java**
9. **ObscureListener.java**
10. **PurifyListener.java**
11. **RicochetListener.java**
12. **ShroudListener.java**
13. **SidestepListener.java**
14. **SteadfastListener.java**
15. **SunderListener.java**
16. **UndertowListener.java**

### P2 - 低频事件监听器 (本月优化)

1. **AirbagListener.java**
2. **ArrowRefundListener.java**
3. **BackfireListener.java**
4. **BarrierListener.java**
5. **BeheadingListener.java**
6. **BlastGuardListener.java**
7. **BrittleListener.java**
8. **DecapitateListener.java**
9. **DebilitateListener.java**
10. **DrainListener.java**
11. **FarshotListener.java**
12. **FleetfootListener.java**
13. **FortitudeListener.java**
14. **FragilityListener.java**
15. **GravitationListener.java**
16. **GreedListener.java**
17. **HomecomingListener.java**
18. **InsightListener.java**
19. **InsomniaListener.java**
20. **LethargyListener.java**
21. **MisfortuneListener.java**
22. **MomentumListener.java**
23. **MortalWoundListener.java**
24. **NourishListener.java**
25. **QuellListener.java**
26. **ReboundListener.java**
27. **RefineListener.java**
28. **ReplantingListener.java**
29. **RetaliateListener.java**
30. **RiposteListener.java**
31. **RocketBurstListener.java**
32. **ShatterListener.java**
33. **SmeltingListener.java**
34. **SonarListener.java**
35. **SteadyAimListener.java**
36. **StillnessListener.java**
37. **ThirstListener.java**
38. **TideRunnerListener.java**
39. **TrawlerListener.java**
40. **WardListener.java**
41. **WingguardListener.java**

## 特殊优化案例

### PanicListener 特殊优化

```java
// 旧代码 (性能差):
List<ItemStack> hotbarItems = new ArrayList<>(
    Arrays.stream(inventory.getContents())
        .toList()
        .subList(0, 9)
);
Collections.shuffle(hotbarItems, ThreadLocalRandom.current());

// 新代码 (性能优化):
ItemStack[] contents = inventory.getContents();
if (contents == null || contents.length < 9) return;

// 直接操作数组，避免创建中间集合
ItemStack[] hotbar = new ItemStack[9];
System.arraycopy(contents, 0, hotbar, 0, 9);

// 使用 Fisher-Yates 洗牌算法
for (int i = hotbar.length - 1; i > 0; i--) {
    int j = PerformanceUtils.getRandom().nextInt(i + 1);
    ItemStack temp = hotbar[i];
    hotbar[i] = hotbar[j];
    hotbar[j] = temp;
}
```

### Stream API 优化

```java
// 旧代码 (BarrierListener):
List<Entity> nearby = player.getWorld()
    .getNearbyEntities(loc, radius, radius, radius, 
        e -> e instanceof LivingEntity && e != player)
    .stream()
    .toList();

// 新代码:
Collection<Entity> nearbyCollection = player.getWorld()
    .getNearbyEntities(loc, radius, radius, radius, 
        e -> e instanceof LivingEntity && e != player);

// 使用预分配的 ArrayList
List<Entity> nearby = PerformanceUtils.newArrayListWithCapacity(nearbyCollection.size());
for (Entity e : nearbyCollection) {
    nearby.add(e);
}
```

### Math.random() 替换

```java
// 旧代码 (SteadyAimListener):
if (Math.random() >= chance) return;

// 新代码:
if (!PerformanceUtils.rollChance(chance)) return;
```

## 验证清单

每个文件优化后需要检查:

- 添加了配置缓存字段
- 构造函数初始化配置
- 替换了所有 getEnchantmentLevel
- 替换了所有冷却检查
- 替换了所有概率检查
- 添加了必要的 null 检查
- 使用了安全的工具方法
- 代码格式正确
- 没有编译错误
- 逻辑保持一致

## 测试清单

每批优化完成后需要测试:

- 编译通过
- 插件加载成功
- 附魔功能正常
- 冷却系统正常
- 概率触发正常
- 无空指针异常
- 无内存泄漏
- 性能有提升

## 进度跟踪

### 已完成: 2/65 (3%)

- BindListener.java
- BulwarkListener.java

### 进行中: 0/65 (0%)

### 待开始: 63/65 (97%)

## 预计完成时间

- P0 (8个文件): 2-3小时
- P1 (16个文件): 4-5小时
- P2 (41个文件): 10-12小时
- 总计: 16-20小时

## 建议

1. 每次优化3-5个文件后进行测试
2. 优先处理高频事件监听器
3. 保持代码风格一致
4. 添加必要的注释
5. 定期提交代码
6. 记录遇到的问题
7. 更新进度跟踪

## 自动化工具

可以考虑编写脚本来自动化部分替换工作:

```bash
# 示例: 批量替换 getEnchantmentLevel
find src/main/java/net/enchadd/listeners -name "*.java" -exec sed -i 's/\.getEnchantmentLevel(enchant)/PerformanceUtils.getEnchantLevel(&, enchant)/g' {} \;
```

注意: 自动化替换后仍需人工检查和调整。