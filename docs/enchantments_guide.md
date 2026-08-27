<!--
TITLE: 附魔功能指南
CATEGORY: 玩家教程
LAST_UPDATED: 2026-04-28
PARENT: 
ICON: ✨
-->

# EnchADD 附魔功能指南

本文档依据当前代码与注册逻辑整理，用于说明每个附魔的**作用、触发方式、获取方式、最高等级、适用物品、概率/冷却逻辑**，并同步记录当前已确认的冲突与边界。

配套文档：

- 分档修复与长时间压测汇总：`docs/安全与压测报告.md`
- 自动生成全量附魔矩阵：`tests/papermc/target/perf-gate/enchant-catalog.md`

## 说明

- 文档中的“获取方式”以当前代码注册/配置读取逻辑为准；如果某附魔在配置中被关闭，它就不会注册对应监听器。
- “概率/冷却”列表示代码中实际采用的触发逻辑；若某附魔没有概率或冷却，则记为“固定触发”或“无”。
- “最高等级”指当前代码 `create()` 中配置的 `maxLevel` 默认值。
- “适用物品”与“触发条件”均按监听器与附魔定义类共同整理。
- 当前文档只保留与代码一致的附魔；已确认不存在的遗留条目（如 `destroying`）已移除。

## 附魔总览

| 中文名 | 英文/展示名 | Key | 类别 | 最高等级 | 获取方式 | 适用物品 | 触发条件 | 作用说明 | 概率 / 冷却 | 代码状态 |
|---|---|---|---|---:|---|---|---|---|---|---|
| 灵魂绑定 | Soulbound | `enchadd:soulbound` | 功能/保留 | I | 战利品 / 指令（默认不进附魔台） | 所有可附魔物品 | `PlayerDeathEvent` | 死亡时保留带此附魔的物品，避免重复掉落 | 极低权重 / 无冷却 | 已注册 |
| 传心 | Telepathy | `enchadd:telepathy` | 采集 / 掉落 | I | 同上 | 工具 | `BlockDropItemEvent` | 掉落物归属玩家，并在下一 tick 传送到玩家附近 | 固定触发 / 下一 tick 延迟 | 已注册 |
| 回耕 | Replanting | `enchadd:replanting` | 采集 | I | 同上 | 锄 / 斧 | 方块破坏相关事件 | 自动补种农作物或作物方块 | 固定触发 / 无冷却 | 已注册 |
| 润垄 | Irrigation | `enchadd:irrigation` | 农业 / 工具 | I | 附魔台 / 战利品 / 交易 | 锄 | `PlayerInteractEvent` | 右键农田时为周围 3x3 干燥农田补满湿度，潜行时仅作用单格 | 固定触发 / 无冷却 | 已注册 |
| 树艺 | Arborist | `enchadd:arborist` | 建造 / 工具 | I | 附魔台 / 战利品 / 交易 | 斧 | `PlayerInteractEvent` | 斧头给原木去皮时，额外向上连剥同材质树干，潜行时关闭扩展 | 固定触发 / 无冷却 | 已注册 |
| 拓径 | Trailblazer | `enchadd:trailblazer` | 地形 / 工具 | I | 附魔台 / 战利品 / 交易 | 铲 | `PlayerInteractEvent` | 铲子拍平道路时额外拓展周围 3x3 可拍平地块，潜行时关闭扩展 | 固定触发 / 无冷却 | 已注册 |
| 垄收 | Furrow | `enchadd:furrow` | 采集 | I | 附魔台 / 战利品 / 交易 | 锄 | `BlockBreakEvent` | 成熟作物触发 3x3 横向收割，潜行时关闭群收，可与回耕联动补种 | 固定触发 / 无冷却 | 已注册 |
| 处刑者 | Executioner | `enchadd:executioner` | 进攻 | III | 同上 | 剑 / 斧 | 近战命中 | 对低生命目标增伤 | 概率型 / 冷却型 | 已注册 |
| 斩首 | Beheading | `enchadd:beheading` | 战利品 | III | 同上 | 剑 / 斧 | 击杀相关事件 | 提高头颅掉落相关行为 | 概率型 / 无冷却 | 已注册 |
| 熔炼 | Smelting | `enchadd:smelting` | 采集 | I | 同上 | 镐 | 挖掘相关事件 | 挖矿时自动熔炼 | 固定触发 / 无冷却 | 已注册 |
| 矿醒 | Stonewake | `enchadd:stonewake` | 采集 / 节奏 | III | 附魔台 / 战利品 / 交易 | 镐 | `BlockBreakEvent` | 挖到矿石后获得短暂急迫，加快下一轮采矿节奏 | 固定触发 / 无冷却 | 已注册 |
| 精炼 | Refine | `enchadd:refine` | 采集 | III | 同上 | 工具 | 挖掘相关事件 | 提升资源产出 | 概率 / 收益型 | 已注册 |
| 反弹 | Rebound | `enchadd:rebound` | 耐久 | III | 同上 | 需耐久消耗的工具 / 装备 | `PlayerItemDamageEvent` | 耐久损耗有概率返还 1 点 | 概率型 / 无冷却 | 已注册 |
| 洞察 | Insight | `enchadd:insight` | 采集 / 经验 | III | 同上 | 工具 | 挖掘 / 击杀相关事件 | 提供额外经验或提示 | 固定 / 概率混合 | 已注册 |
| 安全气囊 | Airbag | `enchadd:airbag` | 防护 | III | 同上 | 全套护甲 | `FALL` / `FLY_INTO_WALL` | 摔落和撞墙减伤 | 固定触发 / 无冷却 | 已注册 |
| 归家 | Homecoming | `enchadd:homecoming` | 生存 | I | 同上 | `totem_of_undying`（配置默认） | `EntityResurrectEvent` | 保命后传送回重生位置或世界出生点 | 固定触发 / 无冷却 | 已注册 |
| 齐射 | Volley | `enchadd:volley` | 远程 | III | 同上 | 弓 | `ProjectileLaunchEvent` | 射出额外箭矢，形成弹幕效果 | 固定触发 / 无冷却 | 已注册 |
| 守护 | Ward | `enchadd:ward` | 防御 | I | 同上 | 盾牌（副手） | `EntityDamageByEntityEvent` | 副手盾牌格挡时直接取消本次伤害 | 概率 / 冷却型 | 已注册 |
| 动量 | Momentum | `enchadd:momentum` | 机动 | III | 同上 | 靴子 | 移动相关事件 | 连续移动提升机动性 | 固定 / 持续型 | 已注册 |
| 坚毅 | Fortitude | `enchadd:fortitude` | 防御 | III | 同上 | 防具 | 受击 / 状态相关事件 | 提升抗性或生存能力 | 固定 / 概率型 | 已注册 |
| 迟缓 | Debilitate | `enchadd:debilitate` | 进攻 | III | 同上 | 近战武器 | 命中事件 | 降低目标能力 | 固定 / 概率型 | 已注册 |
| 营养 | Nourish | `enchadd:nourish` | 生存 | III | 同上 | 工具 | 使用 / 移动事件 | 回复饥饿或降低饥饿损耗 | 概率型 / 无冷却 | 已注册 |
| 轻步 | Fleetfoot | `enchadd:fleetfoot` | 机动 | III | 同上 | 靴子 | 移动相关事件 | 提升基础移动速度 | 固定触发 / 无冷却 | 已注册 |
| 轻踏 | Tenderstep | `enchadd:tenderstep` | 机动 / 农业 | I | 附魔台 / 战利品 / 交易 | 靴子 | `EntityChangeBlockEvent` | 穿着时不会踩坏农田，并可选保护海龟蛋 | 固定触发 / 无冷却 | 已注册 |
| 影袭 | Shadowstrike | `enchadd:shadowstrike` | 进攻 | III | 同上 | 近战武器 | 条件命中事件 | 特定条件下提升伤害 | 固定 / 概率型 | 已注册 |
| 壁垒 | Bulwark | `enchadd:bulwark` | 防御 | III | 同上 | 胸甲 | 受击相关事件 | 提供护盾或抗击退类效果 | 固定 / 概率型 | 已注册 |
| 净化 | Purify | `enchadd:purify` | 净化 | III | 同上 | 头盔 | 负面效果事件 | 清除负面效果 | 固定 / 概率型 | 已注册 |
| 惊惧 | Panic | `enchadd:panic` | 诅咒 | III | 同上 | 护甲 / 武器（按配置） | 受击相关事件 | 施加恐慌或干扰效果 | 概率型 / 无冷却 | 已注册 |
| 霜刃 | Frostbrand | `enchadd:frostbrand` | 元素 | III | 同上 | 近战武器 | 命中事件 | 造成减速或冰冻类效果 | 概率型 / 无冷却 | 已注册 |
| 吸血 | Vampirism | `enchadd:vampirism` | 吸血 | III | 同上 | 近战武器 | 命中事件 | 按伤害比例回复生命值 | 概率 / 比例型 | 已注册 |
| 失眠 | Insomnia | `enchadd:insomnia` | 诅咒 | III | 同上 | 防具 | `PlayerDeepSleepEvent` | 阻止深睡 / 跳夜 | 固定触发 / 无冷却 | 已注册 |
| 贪婪 | Greed | `enchadd:greed` | 诅咒 / 收益 | III | 同上 | 按配置 | 收益相关事件 | 倾向资源收益或经验变化 | 概率型 / 无冷却 | 已注册 |
| 饥渴 | Thirst | `enchadd:thirst` | 诅咒 | III | 同上 | 按配置 | 饥饿 / 状态事件 | 加快饥饿消耗 | 固定 / 持续型 | 已注册 |
| 脆弱 | Fragility | `enchadd:fragility` | 诅咒 | III | 同上 | 按配置 | 耐久损耗相关事件 | 提高耐久损耗速度 | 固定 / 概率型 | 已注册 |
| 厄运 | Misfortune | `enchadd:misfortune` | 诅咒 | III | 同上 | 按配置 | 掉落 / 采集事件 | 降低收益或增加负面结果 | 概率型 / 无冷却 | 已注册 |
| 惰性 | Lethargy | `enchadd:lethargy` | 诅咒 | III | 同上 | 按配置 | 移动 / 攻击事件 | 降低速度和攻击节奏 | 固定 / 持续型 | 已注册 |
| 沉坠 | Gravitation | `enchadd:gravitation` | 诅咒 | III | 同上 | 靴子 | `EntityDamageEvent`（仅 `FALL`） | 只提高摔落伤害 | 固定触发 / 无冷却 | 已注册 |
| 脆骨 | Brittle | `enchadd:brittle` | 诅咒 | III | 同上 | 按配置 | 受击 / 耐久事件 | 更易受损或骨折减速类效果 | 概率型 / 无冷却 | 已注册 |
| 反噬 | Backfire | `enchadd:backfire` | 诅咒 | III | 同上 | 按配置 | 命中 / 攻击事件 | 攻击时反伤自身 | 概率型 / 无冷却 | 已注册 |
| 崩裂 | Shatter | `enchadd:shatter` | 诅咒 | III | 同上 | 按配置 | 物品损坏 / 死亡事件 | 装备或工具更容易损坏 | 概率型 / 无冷却 | 已注册 |
| 血痕 | Hemorrhage | `enchadd:hemorrhage` | 诅咒 | III | 同上 | 近战武器 | 命中事件 | 造成流血持续伤害 | 固定 / 概率型 | 已注册 |
| 致命创伤 | Mortal Wound | `enchadd:mortal_wound` | 诅咒 | III | 同上 | 近战武器 | 命中 / 治疗事件 | 降低治疗效果 | 概率型 / 无冷却 | 已注册 |
| 点燃 | Immolate | `enchadd:immolate` | 攻击 / 诅咒 | III | 同上 | 近战武器 | 命中事件 | 点燃目标 | 固定 / 概率型 | 已注册 |
| 坚守 | Steadfast | `enchadd:steadfast` | 防御 | III | 同上 | 靴子 | 位移 / 受击事件 | 降低击退 | 固定触发 / 无冷却 | 已注册 |
| 压制 | Quell | `enchadd:quell` | 防御 | III | 同上 | 盾 / 防具 | 受击事件 | 压制特定攻击类型 | 固定 / 概率型 | 已注册 |
| 闪避 | Evasion | `enchadd:evasion` | 防御 | III | 同上 | 靴子 | `EntityDamageByEntityEvent`（箭类） | 闪避箭矢类远程攻击 | 冷却 + 概率 | 已注册 |
| 处决 | Decapitate | `enchadd:decapitate` | 进攻 | III | 同上 | 近战武器 | 击杀 / 低血事件 | 触发处决效果 | 概率型 / 无冷却 | 已注册 |
| 潜影 | Shroud | `enchadd:shroud` | 防御 | III | 同上 | 胸甲 | 隐匿 / 侦测事件 | 降低被发现范围 | 固定 / 持续型 | 已注册 |
| 绑定 | Bind | `enchadd:bind` | 控制 | III | 同上 | 近战武器 | 命中事件 | 绑定或限制目标移动 | 概率型 / 无冷却 | 已注册 |
| 绝境 | LastStand | `enchadd:last_stand` | 生存 | III | 同上 | 胸甲 | `EntityDamageEvent` | 致死时保命并设置冷却 | 冷却 + 概率 | 已注册 |
| 稳准 | Steady Aim | `enchadd:steady_aim` | 远程 | III | 同上 | 弓 | 射击相关事件 | 提升瞄准 / 命中稳定性 | 固定 / 持续型 | 已注册 |
| 静止 | Stillness | `enchadd:stillness` | 远程 / 机动 | III | 同上 | 弓 | 射击相关事件 | 静止状态下增强射击 | 固定 / 持续型 | 已注册 |
| 远袭 | Farshot | `enchadd:farshot` | 远程 | III | 同上 | 弓 | `ProjectileLaunchEvent` / 命中事件 | 距离越远伤害越高 | 冷却 + 概率 | 已注册 |
| 标焰 | Flare | `enchadd:flare` | 远程 / 辅助 | III | 同上 | 弓 | 命中事件 | 命中后发光 | 固定 / 概率型 | 已注册 |
| 捕鱼者 | Trawler | `enchadd:trawler` | 水下 | III | 同上 | 水下工具 / 武器 | 水下相关事件 | 水下采集 / 牵引类效果 | 固定 / 持续型 | 已注册 |
| 鲜获 | Freshcatch | `enchadd:freshcatch` | 生存 / 钓鱼 | II | 附魔台 / 战利品 / 交易 | 钓鱼竿 | `PlayerFishEvent` | 钓起可食用渔获时回复少量饥饿与饱和度 | 固定触发 / 无冷却 | 已注册 |
| 星愿 | Starwish | `enchadd:starwish` | 彩蛋 / 探索 | I | 战利品 / 稀有交易（不进附魔台） | 望远镜 | `PlayerInteractEvent` | 夜晚仰望天空时触发，给予短暂夜视与幸运，并播放流星彩蛋特效 | 固定触发 / 60 秒冷却 | 已注册 |
| 旅歌 | Waysong | `enchadd:waysong` | 彩蛋 / 旅行 | I | 战利品 / 稀有交易（不进附魔台） | 山羊角 | `PlayerInteractEvent` | 吹响附魔山羊角时，为自己和附近玩家提供短暂赶路加速，适合队伍集合和长途转场 | 固定触发 / 45 秒冷却 | 已注册 |
| 探幽 | Delvesense | `enchadd:delvesense` | 彩蛋 / 探洞 | I | 战利品 / 稀有交易（不进附魔台） | 指南针 | `PlayerInteractEvent` | 地下使用附魔指南针时，短暂获得夜视并高亮附近敌对生物，适合探洞、清点黑暗角落和遗迹推进 | 固定触发 / 30 秒冷却 | 已注册 |
| 潮息 | Tideshell | `enchadd:tideshell` | 彩蛋 / 海洋 | I | 战利品 / 稀有交易（不进附魔台） | 鹦鹉螺壳 | `PlayerInteractEvent` | 接触水体时使用附魔鹦鹉螺壳，短暂获得水下呼吸与海豚恩惠，适合潜水找船、落水脱困和早期海洋探索 | 固定触发 / 35 秒冷却 | 已注册 |
| 羽护 | Wingguard | `enchadd:wingguard` | 机动 / 翅膀 | III | 同上 | 鞘翅 | 鞘翅飞行 / 受击事件 | 提升鞘翅生存性 | 固定 / 持续型 | 已注册 |
| 屏障 | Barrier | `enchadd:barrier` | 防御 | III | 同上 | 盾牌 | 防御事件 | 产生屏障或阻挡效果 | 固定 / 持续型 | 已注册 |
| 潮行者 | TideRunner | `enchadd:tide_runner` | 水下 / 机动 | III | 同上 | 靴子 | 水中 / 雨天移动事件 | 水下加速 | 固定 / 持续型 | 已注册 |
| 侧闪 | Sidestep | `enchadd:sidestep` | 机动 | III | 同上 | 靴子 | 闪避后事件 | 闪避后短暂加速 | 固定 / 持续型 | 已注册 |
| 裂甲 | Sunder | `enchadd:sunder` | 进攻 | III | 同上 | 近战武器 | `EntityDamageByEntityEvent` | 目标护甲越高，伤害放大越明显 | 冷却 + 概率 | 已注册 |

## 互斥与冲突

### 当前代码默认冲突

| 附魔 A | 附魔 B | 说明 |
|---|---|---|
| `enchadd:hemorrhage` | `minecraft:sharpness` | 流血体系与通用直伤互斥，避免武器伤害线过于万能 |
| `enchadd:hemorrhage` | `minecraft:smite` | 流血与亡灵特化互斥，保留目标特攻分工 |
| `enchadd:hemorrhage` | `minecraft:bane_of_arthropods` | 流血与节肢特化互斥，保留目标特攻分工 |
| `enchadd:frostbrand` | `minecraft:fire_aspect` | 冰霜与火焰互斥，避免元素状态叠穿 |
| `enchadd:immolate` | `minecraft:fire_aspect` | 焦灼与火焰附加互斥，避免重复点燃收益 |
| `enchadd:frostbrand` | `enchadd:immolate` | 冰火双属性互斥，维持武器流派选择 |
| `enchadd:executioner` | `enchadd:decapitate` | 双终结技互斥，避免低血斩杀链过强 |
| `enchadd:initiative` | `enchadd:shadowstrike` | 两种先手爆发互斥，避免开战一击过高 |
| `enchadd:beheading` | `minecraft:looting` | 头颅/额外掉落与掠夺互斥，防止刷取收益失衡 |
| `enchadd:undertow` | `minecraft:riptide` | 拉拽命中与自我冲刺互斥，维持三叉戟定位清晰 |
| `enchadd:volley` | `enchadd:steady_aim` | 弹幕散射与稳准单发互斥 |
| `enchadd:volley` | `enchadd:stillness` | 高机动弹幕与站桩狙击互斥 |
| `enchadd:volley` | `enchadd:farshot` | 近中程压制与远距狙击互斥 |
| `enchadd:volley` | `enchadd:bind` | 多箭齐发与定身控制互斥，防止群体硬控过强 |
| `enchadd:farshot` | `minecraft:multishot` | 远距精射与多发散射互斥，保留弩系分工 |
| `enchadd:steady_aim` | `enchadd:stillness` | 稳准与站定增益互斥，避免炮台流双重叠加 |
| `enchadd:bind` | `minecraft:punch` | 定身与击退互斥，防止控制逻辑相互打架 |
| `enchadd:mortal_wound` | `minecraft:power` | 治疗压制与纯伤害强化互斥，避免远程压制过猛 |
| `enchadd:ward` | `enchadd:barrier` | 完全格挡与护盾减伤互斥 |
| `enchadd:ward` | `enchadd:brace` | 完全格挡与抗破盾韧性互斥 |
| `enchadd:ward` | `enchadd:bulwark` | 完全格挡与受击抗性互斥 |
| `enchadd:ward` | `enchadd:holdfast` | 完全格挡与持续稳守互斥 |
| `enchadd:parry` | `enchadd:barrier` | 反击流与被动护盾流互斥 |
| `enchadd:pivot` | `enchadd:holdfast` | 转守拉开与原地稳守互斥 |
| `enchadd:airbag` | `enchadd:afterglide` | 坠落保护与长续航滑翔互斥，防止鞘翅全能化 |
| `enchadd:airbag` | `enchadd:wingguard` | 坠落保护与空中减伤互斥，保留鞘翅防护分支 |
| `enchadd:fleetfoot` | `enchadd:steadfast` | 高速机动与抗击退稳步互斥 |
| `enchadd:tide_runner` | `minecraft:depth_strider` | 水域加速与原版水下推进互斥，避免移速堆叠过高 |
| `enchadd:evasion` | `minecraft:projectile_protection` | 概率闪避与投射物减伤互斥，防止远程克制失效 |
| `enchadd:sidestep` | `enchadd:homeward` | 近战闪转与紧急撤离互斥，保留腿甲定位差异 |

### 冲突配置迁移说明

- `conflictPolicyVersion` 用于标记默认冲突策略版本，当前默认策略版本为 `2`。
- 旧 `config.yml` 首次加载新版本时，会自动补齐缺失的默认冲突。
- 已存在的自定义冲突条目会保留，不会被默认值覆盖。
- 无效键名、重复项和“自己与自己互斥”的脏配置会在加载时自动清理。
- 附魔台一次摇出多个互斥附魔时，会优先保留更高等级；平级时按稳定 key 顺序裁决，避免结果漂移。

### 现有文档建议互斥（以代码 / 配置为准）

- `Airbag`、`LastStand`、`Ward` 等防御类应避免过度叠加。
- `Volley` 与高散射 / 高数量配置应谨慎。
- `Farshot`、`Sunder` 属于数值敏感项，需和其他伤害增幅类一并验收。

## 高优先级实测建议

### P0
- `Homecoming`
- `Rebound`
- `Gravitation`
- `Insomnia`
- `Drain`
- `LastStand`
- `Ward`
- `Airbag`
- `Telepathy`
- `Soulbound`

### P1
- `Volley`
- `Evasion`
- `Sunder`
- `Farshot`

## 备注

- 本文档会随着代码变更继续更新。

---
*最后更新于: 2026-04-28*
