# EnchAdd 用户使用指南

## 目录

1. [简介](#简介)
2. [安装](#安装)
3. [配置](#配置)
4. [附魔列表](#附魔列表)
5. [使用方法](#使用方法)
6. [命令](#命令)
7. [权限](#权限)
8. [常见问题](#常见问题)

---

## 简介

EnchAdd 是一个强大的 Minecraft 附魔扩展插件，为游戏添加了超过 200 个全新的附魔效果。这些附魔分为多个类别，包括战斗、护甲、工具、防御、特殊和实用附魔。

### 主要特性

- **200+ 独特附魔**：涵盖战斗、护甲、工具等多个领域
- **智能冲突系统**：自动检测和管理附魔冲突
- **GUI 界面**：直观的图形界面浏览和选择附魔
- **高度可配置**：通过配置文件自定义附魔行为
- **性能优化**：两级缓存和高效的事件处理
- **安全可靠**：完善的输入验证和权限管理

---

## 安装

### 系统要求

- Minecraft 版本：1.16.5+
- Java 版本：Java 11+
- 服务器类型：Spigot、Paper、Bukkit

### 安装步骤

1. 下载最新版本的 EnchAdd 插件 jar 文件
2. 将 jar 文件放入服务器的 `plugins` 文件夹
3. 重启服务器或使用 `/reload` 命令
4. 插件将自动生成配置文件

### 首次启动

首次启动时，插件将创建以下文件结构：

```
plugins/EnchAdd/
├── config.yml              # 主配置文件
├── enchantments.yml        # 附魔配置
├── conflicts.yml           # 冲突规则
└── logs/                  # 日志文件夹
```

---

## 配置

### 主配置文件 (config.yml)

```yaml
# 插件设置
settings:
  # 启用/禁用插件
  enabled: true
  
  # 调试模式
  debug: false
  
  # 语言设置
  language: zh_CN

# 性能设置
performance:
  # 启用缓存
  enable-cache: true
  
  # 缓存大小
  cache-size: 1000
  
  # 异步加载
  async-loading: true

# GUI 设置
gui:
  # 启用 GUI
  enabled: true
  
  # GUI 标题
  title: "附魔选择器"
  
  # 每页显示数量
  items-per-page: 45
```

### 附魔配置 (enchantments.yml)

```yaml
# 附魔启用/禁用设置
enchantments:
  # 战斗附魔
  combat:
    critical_strike:
      enabled: true
      max-level: 3
      rarity: epic
    
    vampirism:
      enabled: true
      max-level: 3
      rarity: epic
  
  # 护甲附魔
  armor:
    dodge:
      enabled: true
      max-level: 3
      rarity: uncommon
    
    stone_skin:
      enabled: true
      max-level: 3
      rarity: rare
```

---

## 附魔列表

### 战斗附魔

#### 暴击 (Critical Strike)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：增加暴击率和暴击伤害
- **冲突**：precision_strike, execution

#### 吸血鬼 (Vampirism)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：攻击回复生命值并增加力量
- **冲突**：life_drain, leech

#### 生命偷取 (Life Steal)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：攻击偷取敌人生命值
- **冲突**：vampirism, life_drain

#### 撕裂 (Bleeding)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：攻击使敌人持续流血
- **冲突**：hemorrhage, wound

#### 穿甲 (Armor Pierce)
- **稀有度**：稀有
- **最大等级**：5
- **效果**：无视敌人部分护甲
- **冲突**：无

### 护甲附魔

#### 闪避 (Dodge)
- **稀有度**：普通
- **最大等级**：3
- **效果**：有几率完全躲避攻击
- **冲突**：evasive

#### 石肤 (Stone Skin)
- **稀有度**：稀有
- **最大等级**：3
- **效果**：增加物理抗性
- **冲突**：reinforced_thorns

#### 屏障 (Barrier)
- **稀有度**：稀有
- **最大等级**：3
- **效果**：减少所有伤害
- **冲突**：aegis_shield, bastion

### 工具附魔

#### 效率 (Efficiency)
- **稀有度**：普通
- **最大等级**：5
- **效果**：提高挖掘/砍伐速度
- **冲突**：efficiency_plus, miner

#### 时运 (Fortune)
- **稀有度**：稀有
- **最大等级**：3
- **效果**：增加掉落物数量
- **冲突**：fortune_plus, fortunes_grace

#### 区域挖掘 (Area Mining)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：同时挖掘多个方块
- **冲突**：vein_miner, excavation, timber

### 特殊附魔

#### 陨石打击 (Meteor Strike)
- **稀有度**：传说
- **最大等级**：2
- **效果**：召唤陨石造成范围伤害
- **冲突**：storm_caller, dragon_breath

#### 传送 (Teleport)
- **稀有度**：史诗
- **最大等级**：3
- **效果**：瞬移到目标位置
- **冲突**：phase, void_reach

---

## 使用方法

### 通过 GUI 使用

1. 打开附魔 GUI：
   ```
   /enchadd gui
   ```
2. 浏览附魔列表
3. 点击附魔查看详情
4. 选择附魔并应用到物品

### 通过命令使用

#### 添加附魔
```
/enchadd add <附魔ID> [等级]
```

#### 移除附魔
```
/enchadd remove <附魔ID>
```

#### 查看附魔信息
```
/enchadd info <附魔ID>
```

#### 列出所有附魔
```
/enchadd list [类别]
```

### 在铁砧中使用

1. 打开铁砧
2. 放入要附魔的物品
3. 放入附魔书或经验瓶
4. 附魔将自动应用

---

## 命令

### 玩家命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/enchadd gui` | 打开附魔 GUI | `enchadd.gui` |
| `/enchadd add <附魔> [等级]` | 添加附魔 | `enchadd.add` |
| `/enchadd remove <附魔>` | 移除附魔 | `enchadd.remove` |
| `/enchadd info <附魔>` | 查看附魔信息 | `enchadd.info` |
| `/enchadd list [类别]` | 列出附魔 | `enchadd.list` |
| `/enchadd conflicts` | 查看冲突规则 | `enchadd.conflicts` |

### 管理员命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/enchadd reload` | 重载配置 | `enchadd.admin.reload` |
| `/enchadd enable <附魔>` | 启用附魔 | `enchadd.admin.enable` |
| `/enchadd disable <附魔>` | 禁用附魔 | `enchadd.admin.disable` |
| `/enchadd export` | 导出附魔数据 | `enchadd.admin.export` |
| `/enchadd stats` | 查看统计信息 | `enchadd.admin.stats` |

---

## 权限

### 玩家权限

- `enchadd.use` - 使用插件基本功能
- `enchadd.gui` - 打开附魔 GUI
- `enchadd.add` - 添加附魔
- `enchadd.remove` - 移除附魔
- `enchadd.info` - 查看附魔信息
- `enchadd.list` - 列出附魔
- `enchadd.conflicts` - 查看冲突规则

### 管理员权限

- `enchadd.admin.*` - 所有管理员权限
- `enchadd.admin.reload` - 重载配置
- `enchadd.admin.enable` - 启用附魔
- `enchadd.admin.disable` - 禁用附魔
- `enchadd.admin.export` - 导出数据
- `enchadd.admin.stats` - 查看统计

### 权限配置示例 (permissions.yml)

```yaml
# 默认玩家权限
default:
  - enchadd.use
  - enchadd.gui
  - enchadd.info
  - enchadd.list

# VIP 玩家权限
vip:
  - default
  - enchadd.add
  - enchadd.remove

# 管理员权限
admin:
  - '*'
```

---

## 常见问题

### Q: 附魔不生效？

A: 请检查以下几点：
1. 确认插件已正确安装和启用
2. 检查附魔是否在配置中启用
3. 确认附魔与物品类型兼容
4. 查看控制台是否有错误信息

### Q: 如何查看附魔冲突？

A: 使用命令 `/enchadd conflicts` 或在 GUI 中查看附魔详情。

### Q: 可以自定义附魔效果吗？

A: 可以，通过修改 `enchantments.yml` 配置文件来自定义附魔的最大等级和稀有度。

### Q: 插件会影响服务器性能吗？

A: 插件经过性能优化，包括两级缓存和高效的事件处理，对服务器性能影响极小。

### Q: 如何获取更多附魔？

A: 插件已包含 200+ 附魔，可以通过 `/enchadd list` 查看所有可用附魔。

### Q: 支持哪些 Minecraft 版本？

A: 支持 Minecraft 1.16.5 及以上版本。

### Q: 如何报告 Bug？

A: 请在 GitHub Issues 页面提交问题，并提供详细的错误日志和复现步骤。

---

## 技术支持

- **GitHub**: https://github.com/your-repo/enchadd
- **Discord**: https://discord.gg/your-server
- **文档**: https://docs.enchadd.com

---

**版本**: 1.0.0  
**最后更新**: 2026-02-08
