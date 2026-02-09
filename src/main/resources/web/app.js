(function() {
    "use strict";

    let enchantments = null;
    let enhancedEnchantments = null;
    let allEnchantments = null;

    async function loadEnchantments() {
        try {
            const [response1, response2] = await Promise.all([
                fetch('enchantments.json'),
                fetch('enchantments_enhanced.json')
            ]);
            
            if (response1.ok) {
                enchantments = await response1.json();
            }
            
            if (response2.ok) {
                enhancedEnchantments = await response2.json();
            }
            
            mergeEnchantments();
        } catch (error) {
            console.warn('Error loading enchantments:', error);
            enchantments = getFallbackEnchantments();
        }
    }

    function getVanillaEnchantments() {
        return {
            // 武器类附魔
            "vanilla_weapon": {
                name: "原版武器附魔",
                color: "#e74c3c",
                icon: "⚔️",
                enchantments: [
                    {id: "vanilla_sharpness", name: "锋利", description: "增加伤害"},
                    {id: "vanilla_smite", name: "亡灵杀手", description: "对亡灵生物造成额外伤害"},
                    {id: "vanilla_bane_of_arthropods", name: "节肢杀手", description: "对节肢生物造成额外伤害"},
                    {id: "vanilla_knockback", name: "击退", description: "增加击退距离"},
                    {id: "vanilla_fire_aspect", name: "火焰附加", description: "点燃目标"},
                    {id: "vanilla_looting", name: "抢夺", description: "增加战利品掉落"},
                    {id: "vanilla_sweeping", name: "横扫之刃", description: "增加横扫攻击伤害"}
                ]
            },
            // 防具类附魔
            "vanilla_armor": {
                name: "原版防具附魔",
                color: "#3498db",
                icon: "🛡️",
                enchantments: [
                    {id: "vanilla_protection", name: "保护", description: "减少所有伤害"},
                    {id: "vanilla_fire_protection", name: "火焰保护", description: "减少火焰伤害"},
                    {id: "vanilla_feather_falling", name: "摔落保护", description: "减少摔落伤害"},
                    {id: "vanilla_blast_protection", name: "爆炸保护", description: "减少爆炸伤害"},
                    {id: "vanilla_projectile_protection", name: "弹射物保护", description: "减少弹射物伤害"},
                    {id: "vanilla_respiration", name: "水下呼吸", description: "延长水下呼吸时间"},
                    {id: "vanilla_aqua_affinity", name: "水下速掘", description: "提高水下挖掘速度"},
                    {id: "vanilla_thorns", name: "荆棘", description: "伤害攻击者"},
                    {id: "vanilla_depth_strider", name: "深海探索者", description: "提高水下移动速度"},
                    {id: "vanilla_frost_walker", name: "冰霜行者", description: "将水面变成冰块"},
                    {id: "vanilla_soul_speed", name: "灵魂疾行", description: "在灵魂土上移动更快"}
                ]
            },
            // 工具类附魔
            "vanilla_tool": {
                name: "原版工具附魔",
                color: "#f39c12",
                icon: "⛏️",
                enchantments: [
                    {id: "vanilla_efficiency", name: "效率", description: "提高挖掘速度"},
                    {id: "vanilla_silk_touch", name: "精准采集", description: "获得方块本身而不是掉落物"},
                    {id: "vanilla_unbreaking", name: "耐久", description: "延缓工具损耗"},
                    {id: "vanilla_mending", name: "经验修补", description: "消耗经验修补耐久"},
                    {id: "vanilla_fortune", name: "时运", description: "增加某些方块的掉落"},
                    {id: "vanilla_loot_bonus_blocks", name: "抢夺", description: "增加方块掉落（与武器抢夺不同）"}
                ]
            },
            // 实用类附魔
            "vanilla_utility": {
                name: "原版实用附魔",
                color: "#2ecc71",
                icon: "🔧",
                enchantments: [
                    {id: "vanilla_luck_of_the_sea", name: "海之眷顾", description: "改善钓鱼品质"},
                    {id: "vanilla_lure", name: "饵钓", description: "加快鱼儿咬钩速度"},
                    {id: "vanilla_loyalty", name: "忠诚", description: "三叉戟自动返回"},
                    {id: "vanilla_impaling", name: "穿刺", description: "对水生生物造成额外伤害"},
                    {id: "vanilla_riptide", name: "激流", description: "雨中使用三叉戟推进"},
                    {id: "vanilla_channeling", name: "引雷", description: "雷暴天气中使用三叉戟召唤闪电"}
                ]
            },
            // 诅咒类附魔
            "vanilla_curse": {
                name: "原版诅咒附魔",
                color: "#8e44ad",
                icon: "☠️",
                enchantments: [
                    {id: "vanilla_curse_binding", name: "绑定诅咒", description: "无法移除装备"},
                    {id: "vanilla_curse_vanishing", name: "消失诅咒", description: "死亡时物品消失"}
                ]
            },
            // 防御类附魔
            "vanilla_defense": {
                name: "原版特殊附魔",
                color: "#9b59b6",
                icon: "🔰",
                enchantments: [
                    {id: "vanilla_punch", name: "冲击", description: "增加弓箭击退距离"},
                    {id: "vanilla_power", name: "力量", description: "增加弓箭伤害"},
                    {id: "vanilla_flame", name: "火矢", description: "箭矢点燃目标"},
                    {id: "vanilla_infinity", name: "无限", description: "无限消耗箭矢"},
                    {id: "vanilla_multishot", name: "多重射击", description: "一次射出多支箭"},
                    {id: "vanilla_quick_charge", name: "快速装填", description: "加快弩的装填速度"}
                ]
            }
        };
    }

    function mergeEnchantments() {
        if (!enchantments) {
            enchantments = {};
        }
        
        if (enhancedEnchantments) {
            Object.keys(enhancedEnchantments).forEach(key => {
                const enhanced = enhancedEnchantments[key];
                if (!enchantments[key]) {
                    enchantments[key] = {
                        name: enhanced.name,
                        color: enhanced.color,
                        icon: enhanced.icon,
                        enchantments: []
                    };
                }
                
                enhanced.enchantments.forEach(enchant => {
                    const existing = enchantments[key].enchantments.find(e => e.id === enchant.id);
                    if (!existing) {
                        enchantments[key].enchantments.push(enchant);
                    }
                });
            });
        }
        
        // 添加原版附魔
        const vanillaEnchantments = getVanillaEnchantments();
        Object.keys(vanillaEnchantments).forEach(key => {
            if (!enchantments[key]) {
                enchantments[key] = vanillaEnchantments[key];
            } else {
                // 如果已有该类别，则合并附魔列表
                vanillaEnchantments[key].enchantments.forEach(vanillaEnchant => {
                    const existing = enchantments[key].enchantments.find(e => e.id === vanillaEnchant.id);
                    if (!existing) {
                        enchantments[key].enchantments.push(vanillaEnchant);
                    }
                });
            }
        });
        
        allEnchantments = enchantments;
    }

    function getFallbackEnchantments() {
        return {
            combat: { name: "战斗附魔", color: "#e74c3c", icon: "⚔️", enchantments: [] },
            armor: { name: "防具附魔", color: "#3498db", icon: "🛡️", enchantments: [] },
            tool: { name: "工具附魔", color: "#f39c12", icon: "⛏️", enchantments: [] },
            curse: { name: "诅咒附魔", color: "#8e44ad", icon: "💀", enchantments: [] },
            utility: { name: "实用附魔", color: "#2ecc71", icon: "🔧", enchantments: [] },
            defense: { name: "防御附魔", color: "#9b59b6", icon: "🛡️", enchantments: [] },
            special: { name: "特殊附魔", color: "#f1c40f", icon: "✨", enchantments: [] },
            cosmetic: { name: "装饰附魔", color: "#e91e63", icon: "💎", enchantments: [] }
        };
    }

    let currentTheme = "dark";
    let currentLang = "zh";
    let currentView = "grid";
    let selectedEquipment = "all";
    let selectedCategory = "all";
    let selectedDifficulty = "all";

    const translations = {
        zh: {
            search: "搜索附魔...",
            total: "共",
            home: "首页",
            enchantments: "附魔",
            conflicts: "冲突",
            commands: "命令",
            welcome: "欢迎使用 EnchAdd",
            subtitle: "Minecraft 附魔增强插件",
            about: "关于",
            all: "全部",
            weapon: "武器",
            armor: "防具",
            tool: "工具",
            easy: "简单",
            medium: "中等",
            hard: "困难",
            chest_menu: "附魔箱",
            grid_view: "网格",
            list_view: "列表",
            enhanced: "增强版"
        },
        en: {
            search: "Search enchantments...",
            total: "Total",
            home: "Home",
            enchantments: "Enchantments",
            conflicts: "Conflicts",
            commands: "Commands",
            welcome: "Welcome to EnchAdd",
            subtitle: "Minecraft Enchantment Expansion Plugin",
            about: "About",
            all: "All",
            weapon: "Weapon",
            armor: "Armor",
            tool: "Tool",
            easy: "Easy",
            medium: "Medium",
            hard: "Hard",
            chest_menu: "Enchant Chest",
            grid_view: "Grid",
            list_view: "List",
            enhanced: "Enhanced"
        }
    };

    const equipmentTypes = {
        "weapon": ["弓", "弩", "剑", "斧", "三叉戟"],
        "armor": ["头盔", "胸甲", "护腿", "靴子"],
        "tool": ["镐", "锹", "斧", "锄", "钓鱼竿"]
    };

    const difficultyLevels = {
        "common": { name: "普通", color: "#9e9e9e", weight: 1 },
        "uncommon": { name: "优秀", color: "#4caf50", weight: 2 },
        "rare": { name: "稀有", color: "#2196f3", weight: 3 },
        "very_rare": { name: "非常稀有", color: "#9c27b0", weight: 4 },
        "epic": { name: "史诗", color: "#ff9800", weight: 5 },
        "legendary": { name: "传说", color: "#f44336", weight: 6 },
        "curse": { name: "诅咒", color: "#607d8b", weight: 7 }
    };

    function init() {
        loadEnchantments().then(() => {
            renderStats();
            renderEnchantments();
            setupEventListeners();
        });
    }

    function renderStats() {
        if (!enchantments) return;
        const total = Object.values(enchantments).reduce((sum, cat) => sum + (cat.enchantments ? cat.enchantments.length : 0), 0);
        const el = document.getElementById("stat-total");
        if (el) el.textContent = total;
        
        Object.entries(enchantments).forEach(([key, cat]) => {
            const el = document.getElementById("stat-" + key);
            if (el) el.textContent = cat.enchantments ? cat.enchantments.length : 0;
        });
    }

    function renderEnchantments() {
        var container = document.getElementById("enchantment-list");
        if (!container || !enchantments) return;
        
        container.innerHTML = "";
        
        Object.entries(enchantments).forEach(([rarity, data]) => {
            var section = document.createElement("div");
            section.className = "enchantment-section";
            section.dataset.rarity = rarity;
            
            var header = document.createElement("div");
            header.className = "section-header";
            header.innerHTML = `<span class="rarity-name">${data.icon || ''} ${data.name}</span><span class="rarity-count">${data.enchantments.length}</span>`;
            header.style.borderLeftColor = data.color;
            
            var grid = document.createElement("div");
            grid.className = "enchantment-grid";
            
            data.enchantments.forEach((enchant) => {
                var card = createEnchantmentCard(enchant, rarity, data.color);
                grid.appendChild(card);
            });
            
            section.appendChild(header);
            section.appendChild(grid);
            container.appendChild(section);
        });
    }

    function createEnchantmentCard(enchant, rarity, color) {
        var card = document.createElement("div");
        card.className = "enchantment-card";
        card.dataset.id = enchant.id;
        card.dataset.rarity = rarity;
        card.dataset.name = enchant.name || '未知附魔';
        card.dataset.description = enchant.description || '暂无描述';
        card.dataset.level = enchant.level || "I";
        card.dataset.rarityName = rarity;
        
        var icon = getEnchantmentIcon(enchant.id, rarity);
        var rarityInfo = difficultyLevels[enchant.rarity] || difficultyLevels.common;
        
        card.innerHTML = `
            <div class="card-header" style="border-bottom-color: ${color}">
                <span class="enchant-icon">${icon}</span>
                <span class="enchant-name">${enchant.name || '未知附魔'}</span>
                <span class="enchant-level">${enchant.level || 'I'}</span>
            </div>
            <p class="enchant-desc">${enchant.description || '暂无描述'}</p>
            <div class="enchant-meta">
                <span class="enchant-rarity" style="background: ${rarityInfo.color}">${rarityInfo.name || '普通'}</span>
                <span class="enchant-obtain">${getShortObtain(enchant.obtain)}</span>
            </div>
        `;
        card.style.cursor = "pointer";
        
        return card;
    }

    function getEnchantmentIcon(id, rarity) {
        const iconMap = {
            "combat": "⚔️", "armor": "🛡️", "tool": "⛏️", "curse": "💀",
            "utility": "🔧", "defense": "🛡️", "special": "✨", "cosmetic": "💎",
            "combat_enhanced": "⚔️", "armor_enhanced": "🛡️", "tool_enhanced": "⛏️", "curse_enhanced": "💀"
        };
        return iconMap[rarity] || "✨";
    }

    function getShortObtain(obtain) {
        if (!obtain) return "多种途径";
        if (obtain.length > 15) return obtain.substring(0, 15) + "...";
        return obtain;
    }

    function setupEventListeners() {
        var searchInput = document.getElementById("search-input");
        if (searchInput) {
            searchInput.addEventListener("input", handleSearch);
        }
        
        document.querySelectorAll(".nav-link").forEach((link) => {
            link.addEventListener("click", handleNavigation);
        });
        
        var themeBtn = document.getElementById("theme-toggle");
        if (themeBtn) {
            themeBtn.addEventListener("click", toggleTheme);
        }
        
        document.querySelectorAll(".lang-btn").forEach((btn) => {
            btn.addEventListener("click", handleLanguageChange);
        });
        
        document.querySelectorAll(".tag").forEach((btn) => {
            btn.addEventListener("click", handleFilter);
        });
        
        document.addEventListener("click", function(e) {
            var card = e.target.closest(".enchantment-card");
            if (card) {
                showEnchantmentDetail(card.dataset.id);
            }
        });
    }

    function showEnchantmentDetail(enchantId) {
        let enchant = null;
        let category = '';
        let categoryData = null;
        
        for (const cat of Object.keys(enchantments || {})) {
            if (enchantments[cat]?.enchantments) {
                const found = enchantments[cat].enchantments.find(e => e.id === enchantId);
                if (found) {
                    enchant = found;
                    category = cat;
                    categoryData = enchantments[cat];
                    break;
                }
            }
        }
        
        if (!enchant) return;
        
        var detailPage = document.getElementById("page-detail");
        var modalName = document.getElementById("detail-name");
        var modalCategory = document.getElementById("detail-category");
        var modalDesc = document.getElementById("detail-description");
        var modalId = document.getElementById("detail-id");
        var modalSource = document.getElementById("detail-source");
        var modalConflicts = document.getElementById("detail-conflicts");
        var modalLevel = document.getElementById("detail-level");
        var modalMaterials = document.getElementById("detail-materials");
        var modalTrigger = document.getElementById("detail-trigger");
        var modalBalance = document.getElementById("detail-balance");
        var modalRarity = document.getElementById("detail-rarity");
        
        if (modalName) {
            modalName.textContent = enchant.name || '未知附魔';
            modalName.style.color = categoryData?.color || "var(--accent)";
        }
        if (modalCategory) modalCategory.textContent = categoryData?.name || category || '未知分类';
        if (modalDesc) modalDesc.textContent = enchant.description || '暂无描述';
        if (modalId) modalId.textContent = enchant.id || 'unknown';
        if (modalLevel) modalLevel.textContent = enchant.level || 'I';
        if (modalMaterials) modalMaterials.textContent = enchant.materials || '多种材料';
        if (modalTrigger) modalTrigger.textContent = enchant.trigger || '被动生效';
        if (modalBalance) modalBalance.textContent = enchant.balance || '该附魔经过平衡调整';
        if (modalRarity) modalRarity.textContent = difficultyLevels[enchant.rarity]?.name || '普通';
        
        if (modalSource) {
            modalSource.textContent = enchant.obtain || '多种途径';
        }
        
        var conflictList = [];
        Object.entries(conflictRules).forEach(function(entry) {
            var catId = entry[0];
            var rules = entry[1];
            if (catId === category) {
                (rules.conflicts || []).forEach(function(c) {
                    if (enchantments[c]) conflictList.push(enchantments[c].name);
                });
            }
        });
        if (conflictList.length === 0) conflictList = ["无"];
        if (modalConflicts) modalConflicts.textContent = conflictList.join("、");
        
        document.querySelectorAll(".page").forEach(function(p) { p.classList.remove("active"); });
        if (detailPage) {
            detailPage.classList.add("active");
        }
    }

    function handleSearch(e) {
        var query = e.target.value.toLowerCase();
        
        document.querySelectorAll(".enchantment-card").forEach((card) => {
            var name = card.querySelector(".enchant-name").textContent.toLowerCase();
            var desc = card.querySelector(".enchant-desc").textContent.toLowerCase();
            card.style.display = (name.includes(query) || desc.includes(query)) ? "" : "none";
        });
        
        document.querySelectorAll(".enchantment-section").forEach((section) => {
            var visibleCards = section.querySelectorAll(".enchantment-card:not([style*='display: none'])");
            section.style.display = visibleCards.length > 0 ? "" : "none";
        });
    }

    function handleFilter(e) {
        var filter = e.target.dataset.filter;
        
        document.querySelectorAll(".tag").forEach((btn) => {
            btn.classList.remove("active");
        });
        e.target.classList.add("active");
        
        document.querySelectorAll(".enchantment-section").forEach((section) => {
            var rarity = section.dataset.rarity;
            var cards = section.querySelectorAll(".enchantment-card");
            var visibleCount = 0;
            
            cards.forEach((card) => {
                if (filter === "all" || rarity === filter) {
                    card.style.display = "";
                    visibleCount++;
                } else {
                    card.style.display = "none";
                }
            });
            
            section.style.display = visibleCount > 0 ? "" : "none";
        });
    }

    function handleNavigation(e) {
        e.preventDefault();
        
        document.querySelectorAll(".nav-link").forEach((link) => {
            link.classList.remove("active");
        });
        
        e.target.classList.add("active");
        
        document.querySelectorAll(".page").forEach((page) => {
            page.classList.remove("active");
        });
        
        var navTarget = e.target.dataset.nav;
        var targetPage = document.getElementById("page-" + navTarget);
        if (targetPage) {
            targetPage.classList.add("active");
        }
        
        if (navTarget === "conflicts") {
            setTimeout(renderEnhancedConflictGraph, 100);
        }
    }

    function handlePageNavigation(pageName) {
        document.querySelectorAll(".page").forEach((page) => {
            page.classList.remove("active");
        });
        
        var targetPage = document.getElementById("page-" + pageName);
        if (targetPage) {
            targetPage.classList.add("active");
        }
        
        if (pageName === "enchantments") {
            renderEnchantmentCards();
        }
    }

    function toggleTheme() {
        currentTheme = currentTheme === "dark" ? "light" : "dark";
        document.body.setAttribute("data-theme", currentTheme);
        document.getElementById("theme-icon").textContent = currentTheme === "dark" ? "Light" : "Dark";
    }

    function handleLanguageChange(e) {
        currentLang = e.target.dataset.lang;
        document.querySelectorAll(".lang-btn").forEach((btn) => btn.classList.remove("active"));
        e.target.classList.add("active");
        updateTranslations();
    }
    
    function updateTranslations() {
        var t = translations[currentLang] || translations.zh;
        
        document.getElementById("search-input").placeholder = t.search;
        
        var navLinks = document.querySelectorAll(".nav-link");
        if (navLinks[0]) navLinks[0].textContent = t.home;
        if (navLinks[1]) navLinks[1].textContent = t.enchantments;
        if (navLinks[2]) navLinks[2].textContent = t.conflicts;
        if (navLinks[3]) navLinks[3].textContent = t.commands;
        
        var homeTitle = document.querySelector("#page-home h2");
        if (homeTitle) homeTitle.textContent = t.welcome;
        var homeSubtitle = document.querySelector("#page-home .subtitle");
        if (homeSubtitle) homeSubtitle.textContent = t.subtitle;
        
        var commandsTitle = document.querySelector("#page-commands h2");
        if (commandsTitle) commandsTitle.textContent = t.commands;
        var commandsSubtitle = document.querySelector("#page-commands .subtitle");
        if (commandsSubtitle) commandsSubtitle.textContent = t.subtitle;
        
        var tags = document.querySelectorAll(".tag");
        if (tags[0]) tags[0].textContent = currentLang === "zh" ? "全部" : "All";
        if (tags[1]) tags[1].textContent = currentLang === "zh" ? "普通" : "Common";
        if (tags[2]) tags[2].textContent = currentLang === "zh" ? "优秀" : "Uncommon";
        if (tags[3]) tags[3].textContent = currentLang === "zh" ? "稀有" : "Rare";
        if (tags[4]) tags[4].textContent = currentLang === "zh" ? "非常稀有" : "Very Rare";
        if (tags[5]) tags[5].textContent = currentLang === "zh" ? "史诗" : "Epic";
        if (tags[6]) tags[6].textContent = currentLang === "zh" ? "传说" : "Legendary";
        if (tags[7]) tags[7].textContent = currentLang === "zh" ? "诅咒" : "Curse";
        
        var statLabels = document.querySelectorAll(".stat-label");
        if (statLabels[0]) statLabels[0].textContent = t.total;
        
        var introTitle = document.querySelector("#page-home .intro h3");
        if (introTitle) introTitle.textContent = t.about;
        
        if (currentLang === "zh") {
            document.querySelector(".intro ul").innerHTML = 
                '<li>战斗 - 打怪更有成就感</li>' +
                '<li>防具 - 生存更加轻松</li>' +
                '<li>工具 - 挖矿砍树更快</li>' +
                '<li>远程 - 射手的最爱</li>' +
                '<li>特殊 - 各种有趣能力</li>' +
                '<li>诅咒 - 挑战高难度</li>';
        } else {
            document.querySelector(".intro ul").innerHTML = 
                '<li>Combat - Fight better</li>' +
                '<li>Armor - Survive longer</li>' +
                '<li>Tools - Mine faster</li>' +
                '<li>Ranged - Shoot further</li>' +
                '<li>Special - Fun abilities</li>' +
                '<li>Curse - Hard mode</li>';
        }
    }

    var conflictRules = {
        "combat": {
            conflicts: ["armor", "special"],
            compatible: ["tool", "curse", "utility", "defense", "cosmetic"],
            weak: ["defense"]
        },
        "armor": {
            conflicts: ["combat", "special"],
            compatible: ["tool", "curse", "utility", "defense", "cosmetic"],
            weak: []
        },
        "tool": {
            conflicts: ["special"],
            compatible: ["combat", "armor", "curse", "utility", "defense", "cosmetic"],
            weak: ["curse"]
        },
        "curse": {
            conflicts: [],
            compatible: ["combat", "armor", "tool", "utility", "defense", "special", "cosmetic"],
            weak: []
        },
        "utility": {
            conflicts: ["special"],
            compatible: ["combat", "armor", "tool", "curse", "defense", "cosmetic"],
            weak: []
        },
        "defense": {
            conflicts: ["special"],
            compatible: ["combat", "armor", "tool", "curse", "utility", "cosmetic"],
            weak: ["tool"]
        },
        "special": {
            conflicts: ["combat", "armor", "tool", "curse", "utility", "defense", "cosmetic"],
            compatible: [],
            weak: []
        },
        "cosmetic": {
            conflicts: ["cosmetic"],
            compatible: ["combat", "armor", "tool", "curse", "utility", "defense", "special"],
            weak: []
        },
        "combat_enhanced": {
            conflicts: ["armor_enhanced", "special"],
            compatible: ["tool_enhanced", "curse_enhanced", "utility", "defense"],
            weak: ["defense"]
        },
        "armor_enhanced": {
            conflicts: ["combat_enhanced", "special"],
            compatible: ["tool_enhanced", "curse_enhanced", "utility", "defense"],
            weak: []
        },
        "tool_enhanced": {
            conflicts: ["special"],
            compatible: ["combat_enhanced", "armor_enhanced", "curse_enhanced", "utility", "defense"],
            weak: ["curse_enhanced"]
        },
        "curse_enhanced": {
            conflicts: [],
            compatible: ["combat_enhanced", "armor_enhanced", "tool_enhanced", "utility", "defense", "special"],
            weak: []
        },
        // Minecraft原版附魔类别及其冲突规则
        "vanilla_weapon": {
            conflicts: ["vanilla_armor", "special"],
            compatible: ["vanilla_tool", "curse", "utility", "defense", "cosmetic"],
            weak: ["defense"]
        },
        "vanilla_armor": {
            conflicts: ["vanilla_weapon", "special"],
            compatible: ["vanilla_tool", "curse", "utility", "defense", "cosmetic"],
            weak: []
        },
        "vanilla_tool": {
            conflicts: ["special"],
            compatible: ["vanilla_weapon", "vanilla_armor", "curse", "utility", "defense", "cosmetic"],
            weak: ["curse"]
        },
        "vanilla_utility": {
            conflicts: ["special"],
            compatible: ["vanilla_weapon", "vanilla_armor", "vanilla_tool", "curse", "defense", "cosmetic"],
            weak: []
        },
        "vanilla_curse": {
            conflicts: [],
            compatible: ["vanilla_weapon", "vanilla_armor", "vanilla_tool", "vanilla_utility", "defense", "special", "cosmetic"],
            weak: []
        },
        "vanilla_defense": {
            conflicts: ["special"],
            compatible: ["vanilla_weapon", "vanilla_armor", "vanilla_tool", "vanilla_utility", "curse", "cosmetic"],
            weak: ["vanilla_tool"]
        }
    };
    
    var tooltipEl = null;
    var zoomLevel = 1;
    var currentTransform = { x: 0, y: 0 };
    var isDragging = false;
    var dragStart = { x: 0, y: 0 };
    var selectedGraphCategory = null;
    var physicsNodes = [];
    var velocityHistory = [];
    
    function createTooltip() {
        if (tooltipEl) return;
        tooltipEl = document.createElement("div");
        tooltipEl.className = "tooltip";
        tooltipEl.innerHTML = '<div class="tooltip-title"></div><div class="tooltip-desc"></div><div class="tooltip-conflicts"></div>';
        document.getElementById("conflict-graph-container").appendChild(tooltipEl);
    }
    
    function showTooltip(event, nodeData) {
        if (!tooltipEl) createTooltip();
        var container = document.getElementById("conflict-graph-container");
        var containerRect = container.getBoundingClientRect();
        var x = event.clientX - containerRect.left + 15;
        var y = event.clientY - containerRect.top - 10;
        
        var titleHtml = "";
        var descHtml = "";
        var conflictHtml = "";
        
        if (nodeData.isCategory) {
            titleHtml = '<span style="color:' + nodeData.color + ';">' + nodeData.icon + ' ' + nodeData.name + '</span>';
            descHtml = '<span style="font-size:13px; color: var(--text-primary);">' + nodeData.count + ' 个附魔</span>';
            
            var rules = conflictRules[nodeData.id] || { conflicts: [], compatible: [], weak: [] };
            
            if (rules.conflicts && rules.conflicts.length > 0) {
                conflictHtml += '<div class="conflict-title"></div>';
                rules.conflicts.forEach(function(cat) {
                    var color = enchantments[cat] ? enchantments[cat].color : "#f85149";
                    conflictHtml += '<div class="conflict-item" style="background:' + color + '; width:12px; height:12px; border-radius:50%;"></div>';
                });
            }
            if (rules.compatible && rules.compatible.length > 0) {
                conflictHtml += '<div class="compatible-title"></div>';
                rules.compatible.forEach(function(cat) {
                    var color = enchantments[cat] ? enchantments[cat].color : "#3fb950";
                    conflictHtml += '<div class="compatible-item" style="background:' + color + '; width:12px; height:12px; border-radius:50%;"></div>';
                });
            }
            if (rules.weak && rules.weak.length > 0) {
                conflictHtml += '<div class="weak-title"></div>';
                rules.weak.forEach(function(cat) {
                    var color = enchantments[cat] ? enchantments[cat].color : "#f0c14b";
                    conflictHtml += '<div class="weak-item" style="background:' + color + '; width:10px; height:10px; border-radius:50%;"></div>';
                });
            }
        } else {
            var parentCat = nodeData.parentId ? (enchantments[nodeData.parentId] || {}) : {};
            titleHtml = '<span style="color:' + nodeData.parentColor + ';">' + nodeData.name + '</span>';
            descHtml = '<div style="margin-top:6px; padding:8px; background: rgba(255,255,255,0.05); border-radius:4px; font-size:12px; color: var(--text-secondary); line-height:1.5;">' + (nodeData.description || '暂无介绍') + '</div>';
            descHtml += '<div style="margin-top:8px; font-size:11px; color: var(--text-muted);"><span style="color:' + nodeData.parentColor + ';">分类: ' + (parentCat.name || (nodeData.parentId ? nodeData.parentId.replace(/_/g, ' ') : '未知')) + '</span></div>';
            descHtml += '<div style="margin-top:4px; font-size:11px; color: var(--text-muted);">ID: ' + nodeData.id + '</div>';
        }
        
        tooltipEl.querySelector(".tooltip-title").innerHTML = titleHtml;
        tooltipEl.querySelector(".tooltip-desc").innerHTML = descHtml;
        tooltipEl.querySelector(".tooltip-conflicts").innerHTML = conflictHtml;
        
        tooltipEl.style.left = Math.min(x, containerRect.width - 320) + "px";
        tooltipEl.style.top = Math.min(y, containerRect.height - tooltipEl.offsetHeight - 20) + "px";
        tooltipEl.classList.add("visible");
    }
    
    function hideTooltip() {
        if (tooltipEl) {
            tooltipEl.classList.remove("visible");
        }
    }
    
    function createZoomControls(container) {
        var controls = document.createElement("div");
        controls.className = "zoom-controls";
        controls.innerHTML = '<button class="zoom-btn" data-action="zoom-in">+</button>' +
                           '<button class="zoom-btn" data-action="zoom-out">-</button>' +
                           '<button class="zoom-btn" data-action="zoom-reset">O</button>';
        container.appendChild(controls);
        
        controls.querySelector('[data-action="zoom-in"]').addEventListener("click", function() {
            zoomLevel = Math.min(zoomLevel * 1.2, 3);
            applyZoom();
        });
        controls.querySelector('[data-action="zoom-out"]').addEventListener("click", function() {
            zoomLevel = Math.max(zoomLevel / 1.2, 0.3);
            applyZoom();
        });
        controls.querySelector('[data-action="zoom-reset"]').addEventListener("click", function() {
            zoomLevel = 1;
            currentTransform = { x: 0, y: 0 };
            applyZoom();
        });
    }
    
    function createLegend() {
        var legend = document.createElement("div");
        legend.className = "legend";
        legend.innerHTML = '<div class="legend-item"><div class="legend-line conflict"></div></div>' +
                          '<div class="legend-item"><div class="legend-line compatible"></div></div>' +
                          '<div class="legend-item"><div class="legend-line neutral"></div></div>';
        return legend;
    }
    
    function createStatsBar() {
        var stats = document.createElement("div");
        stats.className = "stats-bar";
        stats.innerHTML = '<div class="stat-item"><span class="stat-value" id="stat-conflicts">0</span></div>' +
                         '<div class="stat-item"><span class="stat-value" id="stat-zoom">100%</span></div>';
        return stats;
    }
    
    function createInstruction() {
        var instruction = document.createElement("div");
        instruction.className = "instruction";
        instruction.innerHTML = '<div class="hint-hint"></div>';
        return instruction;
    }
    
    function applyZoom() {
        var svg = d3.select("#conflict-graph-container svg");
        svg.attr("transform", "translate(" + currentTransform.x + "," + currentTransform.y + ") scale(" + zoomLevel + ")");
    }
    
    function countRelationships() {
        var categories = Object.keys(enchantments);
        var totalConflicts = 0;
        var combatConflicts = 0;
        var armorConflicts = 0;
        var toolConflicts = 0;
        var cosmeticConflicts = 0;
        
        for (var i = 0; i < categories.length; i++) {
            for (var j = i + 1; j < categories.length; j++) {
                var cat1 = categories[i], cat2 = categories[j];
                var rules1 = conflictRules[cat1] || { conflicts: [], compatible: [], weak: [] };
                if (rules1.conflicts.indexOf(cat2) >= 0) {
                    totalConflicts++;
                    if (cat1 === "combat") combatConflicts++;
                    if (cat1 === "armor") armorConflicts++;
                    if (cat1 === "tool") toolConflicts++;
                    if (cat1 === "cosmetic") cosmeticConflicts++;
                }
            }
        }
        
        var elTotal = document.getElementById("stat-total-conflicts");
        var elCombat = document.getElementById("stat-combat-conflicts");
        var elArmor = document.getElementById("stat-armor-conflicts");
        var elTool = document.getElementById("stat-tool-conflicts");
        var elCosmetic = document.getElementById("stat-cosmetic-conflicts");
        
        if (elTotal) elTotal.textContent = totalConflicts;
        if (elCombat) elCombat.textContent = combatConflicts;
        if (elArmor) elArmor.textContent = armorConflicts;
        if (elTool) elTool.textContent = toolConflicts;
        if (elCosmetic) elCosmetic.textContent = cosmeticConflicts;
    }
    
    function showCategoryDetails(categoryData) {
        var container = document.getElementById("conflict-graph-container");
        var existingDetails = container.querySelector(".category-details");
        if (existingDetails) existingDetails.remove();
        
        var details = document.createElement("div");
        details.className = "category-details";
        
        var rules = conflictRules[categoryData.id] || { conflicts: [], compatible: [] };
        var conflictNames = rules.conflicts.map(function(cat) {
            return enchantments[cat] ? enchantments[cat].name : cat;
        }).join("、");
        
        details.innerHTML = '<button class="close-btn" onclick="this.parentElement.classList.remove(\'visible\')">X</button>' +
                           '<h3 style="color: ' + categoryData.color + ';">' + categoryData.icon + ' ' + categoryData.name + '</h3>' +
                           '<p style="color: var(--text-secondary); margin-bottom: 16px;">有 <strong style="color: var(--accent);">' + categoryData.count + '</strong> 个附魔</p>' +
                           '<div style="margin-bottom: 16px;"><strong style="color: #f85149;">会冲突:</strong> ' + (conflictNames || "无") + '</div>' +
                           '<h4 style="font-size: 14px; margin-bottom: 12px;">有哪些附魔:</h4>' +
                           '<div class="enchant-list">';
        
        categoryData.enchantments.forEach(function(enchant) {
            details.querySelector(".enchant-list").innerHTML += 
                '<div class="enchant-item"><span class="enchant-name">' + enchant.name + '</span></div>';
        });
        
        details.innerHTML += '</div>';
        container.appendChild(details);
        setTimeout(function() { details.classList.add("visible"); }, 10);
    }
    
    function renderEnhancedConflictGraph() {
        var container = document.getElementById("conflict-graph-container");
        if (!container) {
            console.warn("Conflict graph container not found");
            return;
        }
        
        if (!enchantments) {
            console.warn("Enchantments data not loaded yet");
            setTimeout(() => renderEnhancedConflictGraph(), 500);
            return;
        }
        
        if (typeof d3 === "undefined") {
            console.warn("D3.js not loaded, using simple HTML fallback");
            renderSimpleConflictGraph(container);
            return;
        }
        
        try {
            container.innerHTML = "";
            createTooltip();
            container.appendChild(createLegend());
            container.appendChild(createStatsBar());
            container.appendChild(createInstruction());
            createZoomControls(container);
            
            var width = container.clientWidth || 800;
            var height = 600;
            var svg = d3.select(container).select("svg");
            if (svg.empty()) {
                svg = d3.select("#conflict-graph-container")
                    .append("svg")
                    .attr("width", width)
                    .attr("height", height)
                    .attr("style", "width: 100%; height: 100%;");
            }
            
            svg.attr("viewBox", "0 0 " + width + " " + height);
            
            var centerX = width / 2;
            var centerY = height / 2;
            var orbitRadius = Math.min(width, height) * 0.22;
            
            physicsNodes = [];
            var categoryNodes = [];
            var allEnchantmentNodes = [];
            
            Object.keys(enchantments).forEach(function(catKey, catIndex) {
                var catData = enchantments[catKey];
                var baseAngle = (catIndex / Object.keys(enchantments).length) * Math.PI * 2 - Math.PI / 2;
                var baseX = centerX + Math.cos(baseAngle) * orbitRadius;
                var baseY = centerY + Math.sin(baseAngle) * orbitRadius;
                
                var enchantList = catData.enchantments || [];
                var nodeCount = enchantList.length;
                
                var categoryNode = {
                    id: catKey,
                    name: catData.name,
                    icon: catData.icon,
                    color: catData.color,
                    count: nodeCount,
                    x: baseX,
                    y: baseY,
                    vx: 0,
                    vy: 0,
                    radius: 5, // 缩小2倍，原来是10
                    isCategory: true
                };
                categoryNodes.push(categoryNode);
                physicsNodes.push(categoryNode);
                
                var catEnchantments = [];
                enchantList.forEach(function(enchant, enchantIndex) {
                    var angle = baseAngle + ((enchantIndex - nodeCount / 2) / nodeCount) * Math.PI * 1.2;
                    var dist = 55 + (enchantIndex % 3) * 20;
                    var x = centerX + Math.cos(angle) * dist;
                    var y = centerY + Math.sin(angle) * dist;
                    
                    var enchantNode = {
                        id: enchant.id,
                        name: enchant.name,
                        description: enchant.description,
                        parentId: catKey,
                        parentColor: catData.color,
                        x: x,
                        y: y,
                        vx: 0,
                        vy: 0,
                        radius: 3.5,
                        isCategory: false
                    };
                    catEnchantments.push(enchantNode);
                    allEnchantmentNodes.push(enchantNode);
                });
                
                categoryNode.enchantments = catEnchantments;
            });
            
            physicsNodes = categoryNodes.concat(allEnchantmentNodes);
            
            var defs = svg.append("defs");
            
            physicsNodes.forEach(function(node) {
                var gradientId = "glow-" + node.id + "-" + Math.random().toString(36).substr(2, 9);
                var gradient = defs.append("radialGradient")
                    .attr("id", gradientId)
                    .attr("cx", "50%")
                    .attr("cy", "50%")
                    .attr("r", "50%");
                
                gradient.append("stop")
                    .attr("offset", "0%")
                    .attr("stop-color", node.color || node.parentColor)
                    .attr("stop-opacity", 0.9);
                
                gradient.append("stop")
                    .attr("offset", "100%")
                    .attr("stop-color", node.color || node.parentColor)
                    .attr("stop-opacity", 0);
                
                node.gradientId = gradientId;
            });
            
            var linkGroup = svg.append("g").attr("class", "links");
            var nodeGroup = svg.append("g").attr("class", "nodes");
            var labelGroup = svg.append("g").attr("class", "labels");
            
            function getLinkType(cat1, cat2) {
                var rules1 = conflictRules[cat1] || { conflicts: [], compatible: [], weak: [] };
                if (rules1.conflicts.indexOf(cat2) >= 0) return "conflict";
                if (rules1.compatible.indexOf(cat2) >= 0) return "compatible";
                if (rules1.weak.indexOf(cat2) >= 0) return "weak";
                return "weak";
            }
            
            function updateLinks() {
                linkGroup.selectAll("*").remove();
                
                for (var i = 0; i < categoryNodes.length; i++) {
                    for (var j = i + 1; j < categoryNodes.length; j++) {
                        var linkType = getLinkType(categoryNodes[i].id, categoryNodes[j].id);
                        if (linkType !== "none") {
                            linkGroup.append("line")
                                .attr("class", "link " + linkType)
                                .attr("x1", categoryNodes[i].x)
                                .attr("y1", categoryNodes[i].y)
                                .attr("x2", categoryNodes[j].x)
                                .attr("y2", categoryNodes[j].y)
                                .attr("stroke", "#888") // 默认为灰色
                                .attr("stroke-width", linkType === "conflict" ? 2 : (linkType === "compatible" ? 1 : 0.5))
                                .attr("opacity", 0.15); // 降低透明度
                        }
                    }
                }
                
                allEnchantmentNodes.forEach(function(enchantNode) {
                    var categoryNode = categoryNodes.find(function(n) { return n.id === enchantNode.parentId; });
                    if (categoryNode) {
                        linkGroup.append("line")
                            .attr("class", "link enchant-link")
                            .attr("x1", enchantNode.x)
                            .attr("y1", enchantNode.y)
                            .attr("x2", categoryNode.x)
                            .attr("y2", categoryNode.y)
                            .attr("stroke", "#888") // 默认为灰色
                            .attr("stroke-width", 0.5)
                            .attr("opacity", 0.1); // 降低透明度
                    }
                });
            }
            
            updateLinks();
            countRelationships();
            
            function getMousePos(event) {
                var pt = svg.node().createSVGPoint();
                var sourceEvent = event.sourceEvent || event;
                pt.x = sourceEvent.clientX;
                pt.y = sourceEvent.clientY;
                return pt.matrixTransform(svg.node().getScreenCTM().inverse());
            }
            
            var draggedNode = null;
            var touchStartTime = 0;
            
            function nodeEnter(event, d) {
                d3.select(this).style("cursor", "grab");
                
                // 悬停时显示颜色和发光效果
                if (d3.select(this).select("circle.main")) {
                    d3.select(this).select("circle.main")
                        .transition().duration(150)
                        .attr("r", d.radius * 1.4)
                        .attr("fill", function() { return d.color || d.parentColor; })
                        .attr("stroke", function() { return d.color || d.parentColor; })
                        .attr("stroke-opacity", d.isCategory ? 0.7 : 0.5)
                        .style("filter", function() { return "drop-shadow(0 0 " + (d.radius * 0.4) + "px " + (d.color || d.parentColor) + ")"; });
                }
                if (d3.select(this).select(".glow")) {
                    d3.select(this).select(".glow")
                        .transition().duration(150)
                        .attr("r", d.radius * 2)
                        .attr("opacity", 0.6);
                }
                
                // 更新相关连线的颜色
                var selectedNodeId = d.id;
                
                // 更新与其他分类节点的连线
                linkGroup.selectAll('.link').each(function() {
                    var link = d3.select(this);
                    var x1 = link.attr("x1");
                    var y1 = link.attr("y1");
                    var x2 = link.attr("x2");
                    var y2 = link.attr("y2");
                    
                    // 检查连线是否连接到当前悬停的节点
                    var connectedToCurrent = false;
                    var connectedNodes = [];
                    
                    categoryNodes.forEach(function(catNode) {
                        if ((Math.abs(parseFloat(x1) - catNode.x) < 0.1 && Math.abs(parseFloat(y1) - catNode.y) < 0.1) ||
                            (Math.abs(parseFloat(x2) - catNode.x) < 0.1 && Math.abs(parseFloat(y2) - catNode.y) < 0.1)) {
                            connectedNodes.push(catNode);
                            if (catNode.id === selectedNodeId) {
                                connectedToCurrent = true;
                            }
                        }
                    });
                    
                    if (connectedToCurrent && connectedNodes.length >= 2) {
                        // 如果连线连接到当前悬停的节点，则显示原色
                        var linkType = getLinkType(connectedNodes[0].id, connectedNodes[1].id);
                        var strokeColor = "#ccc";
                        
                        if (linkType === "conflict") {
                            strokeColor = connectedNodes[0].color || connectedNodes[0].parentColor || "#f85149";
                        } else if (linkType === "compatible") {
                            strokeColor = connectedNodes[0].color || connectedNodes[0].parentColor || "#3fb950";
                        } else if (linkType === "weak") {
                            strokeColor = connectedNodes[0].color || connectedNodes[0].parentColor || "#f0c14b";
                        }
                        
                        link.transition().duration(150)
                            .attr("stroke", strokeColor)
                            .attr("opacity", 0.7);
                    }
                });
                
                // 更新与子节点的连线
                if (d.isCategory) {
                    linkGroup.selectAll('.link.enchant-link').each(function() {
                        var link = d3.select(this);
                        var x1 = link.attr("x1");
                        var y1 = link.attr("y1");
                        var x2 = link.attr("x2");
                        var y2 = link.attr("y2");
                        
                        // 检查连线是否连接到当前悬停的大节点
                        categoryNodes.forEach(function(catNode) {
                            if (catNode.id === selectedNodeId &&
                                ((Math.abs(parseFloat(x2) - catNode.x) < 0.1 && Math.abs(parseFloat(y2) - catNode.y) < 0.1))) {
                                link.transition().duration(150)
                                    .attr("stroke", d.color || d.parentColor)
                                    .attr("opacity", 0.6);
                            }
                        });
                    });
                } else {
                    // 对于小节点，更新与其父分类节点的连线
                    linkGroup.selectAll('.link.enchant-link').each(function() {
                        var link = d3.select(this);
                        var x1 = link.attr("x1");
                        var y1 = link.attr("y1");
                        var x2 = link.attr("x2");
                        var y2 = link.attr("y2");
                        
                        // 检查连线是否连接到当前悬停的小节点
                        allEnchantmentNodes.forEach(function(enchantNode) {
                            if (enchantNode.id === selectedNodeId &&
                                ((Math.abs(parseFloat(x1) - enchantNode.x) < 0.1 && Math.abs(parseFloat(y1) - enchantNode.y) < 0.1))) {
                                link.transition().duration(150)
                                    .attr("stroke", d.parentColor)
                                    .attr("opacity", 0.6);
                            }
                        });
                    });
                }
                
                // 如果悬停在大球上，高亮显示该分类下的所有小球
                if (d.isCategory) {
                    allEnchantmentNodes.forEach(function(enchantNode) {
                        if (enchantNode.parentId === d.id) {
                            // 查找对应的节点组
                            var nodesToHighlight = nodeGroup.selectAll('.node-group').filter(function(data) { 
                                return data.id === enchantNode.id; 
                            });
                            if (!nodesToHighlight.empty()) {
                                nodesToHighlight.select('circle.main')
                                    .transition().duration(150)
                                    .attr("fill", function() { return enchantNode.parentColor; })
                                    .attr("stroke", function() { return enchantNode.parentColor; })
                                    .attr("stroke-opacity", 0.7)
                                    .style("filter", function() { return "drop-shadow(0 0 " + (enchantNode.radius * 0.4) + "px " + enchantNode.parentColor + ")"; });
                                    
                                nodesToHighlight.select('.glow')
                                    .transition().duration(150)
                                    .attr("opacity", 0.4);
                                
                                // 同时高亮该小球与其父节点的连线
                                linkGroup.selectAll('.link.enchant-link').each(function() {
                                    var link = d3.select(this);
                                    var x1 = link.attr("x1");
                                    var y1 = link.attr("y1");
                                    var x2 = link.attr("x2");
                                    var y2 = link.attr("y2");
                                    
                                    if ((Math.abs(parseFloat(x1) - enchantNode.x) < 0.1 && Math.abs(parseFloat(y1) - enchantNode.y) < 0.1) &&
                                        (Math.abs(parseFloat(x2) - d.x) < 0.1 && Math.abs(parseFloat(y2) - d.y) < 0.1)) {
                                        link.transition().duration(150)
                                            .attr("stroke", enchantNode.parentColor)
                                            .attr("opacity", 0.6);
                                    }
                                });
                            }
                        }
                    });
                }
                
                showTooltip(event, d);
            }
            
            function nodeMove(event, d) {
                showTooltip(event, d);
            }
            
            function nodeLeave(event, d) {
                if (!d.isDragging) {
                    // 恢复默认的灰色状态
                    d3.select(this).select("circle.main")
                        .transition().duration(150)
                        .attr("r", d.radius)
                        .attr("fill", "#888888")
                        .attr("stroke", "#666666")
                        .attr("stroke-opacity", 0.5)
                        .style("filter", "none");
                    d3.select(this).select(".glow")
                        .transition().duration(150)
                        .attr("r", d.radius * 1.3)
                        .attr("opacity", 0);
                }
                
                // 恢复所有连线的默认灰色状态
                linkGroup.selectAll('.link').each(function() {
                    var link = d3.select(this);
                    link.transition().duration(150)
                        .attr("stroke", "#888")
                        .attr("opacity", function() {
                            if (link.classed("enchant-link")) {
                                return 0.1;
                            } else {
                                return 0.15;
                            }
                        });
                });
                
                // 如果离开大球，恢复该分类下所有小球的灰色状态
                if (d.isCategory) {
                    allEnchantmentNodes.forEach(function(enchantNode) {
                        if (enchantNode.parentId === d.id) {
                            var nodesToRestore = nodeGroup.selectAll('.node-group').filter(function(data) { 
                                return data.id === enchantNode.id; 
                            });
                            if (!nodesToRestore.empty()) {
                                nodesToRestore.select('circle.main')
                                    .transition().duration(150)
                                    .attr("fill", "#888888")
                                    .attr("stroke", "#666666")
                                    .attr("stroke-opacity", 0.5)
                                    .style("filter", "none");
                                    
                                nodesToRestore.select('.glow')
                                    .transition().duration(150)
                                    .attr("opacity", 0);
                                
                                // 恢复该小球与其父节点的连线
                                linkGroup.selectAll('.link.enchant-link').each(function() {
                                    var link = d3.select(this);
                                    var x1 = link.attr("x1");
                                    var y1 = link.attr("y1");
                                    var x2 = link.attr("x2");
                                    var y2 = link.attr("y2");
                                    
                                    if ((Math.abs(parseFloat(x1) - enchantNode.x) < 0.1 && Math.abs(parseFloat(y1) - enchantNode.y) < 0.1) &&
                                        (Math.abs(parseFloat(x2) - d.x) < 0.1 && Math.abs(parseFloat(y2) - d.y) < 0.1)) {
                                        link.transition().duration(150)
                                            .attr("stroke", "#888")
                                            .attr("opacity", 0.1);
                                    }
                                });
                            }
                        }
                    });
                }
                
                hideTooltip();
            }
            
            function nodeClick(event, d) {
                event.stopPropagation();
                if (Date.now() - touchStartTime < 300) {
                    if (d.isCategory) {
                        showCategoryDetails(d);
                    } else {
                        // 点击小球时，显示与它冲突的其他附魔的连接
                        showConflictConnections(d);
                    }
                }
            }
            
            function showConflictConnections(clickedNode) {
                // 存储原始颜色以便恢复
                var originalColors = new Map();
                
                // 高亮点击的节点
                var clickedNodeElement = nodeGroup.selectAll('.node-group').filter(function(data) { 
                    return data.id === clickedNode.id; 
                });
                
                if (!clickedNodeElement.empty()) {
                    // 保存原始颜色
                    var originalFill = clickedNodeElement.select('circle.main').attr("fill");
                    var originalStroke = clickedNodeElement.select('circle.main').attr("stroke");
                    originalColors.set(clickedNode.id, {fill: originalFill, stroke: originalStroke});
                    
                    // 高亮点击的节点
                    clickedNodeElement.select('circle.main')
                        .transition().duration(300)
                        .attr("fill", clickedNode.parentColor || clickedNode.color)
                        .attr("stroke", clickedNode.parentColor || clickedNode.color)
                        .attr("stroke-width", 2)
                        .style("filter", "drop-shadow(0 0 " + (clickedNode.radius * 0.8) + "px " + (clickedNode.parentColor || clickedNode.color) + ")");
                        
                    clickedNodeElement.select('.glow')
                        .transition().duration(300)
                        .attr("opacity", 0.8);
                }
                
                // 创建临时冲突连接
                var tempLinks = [];
                
                // 查找与点击节点冲突的类别
                var clickedParentNode = categoryNodes.find(function(cat) {
                    return cat.id === clickedNode.parentId;
                });
                
                var conflictingNodes = []; // 存储冲突节点以便稍后高亮
                
                if (clickedParentNode) {
                    var rules = conflictRules[clickedParentNode.id] || { conflicts: [], compatible: [], weak: [] };
                    
                    // 查找冲突类别中的节点
                    rules.conflicts.forEach(function(conflictCategoryId) {
                        var conflictCategory = categoryNodes.find(function(cat) {
                            return cat.id === conflictCategoryId;
                        });
                        
                        if (conflictCategory) {
                            // 查找冲突类别中的所有节点
                            var conflictNodes = allEnchantmentNodes.filter(function(node) {
                                return node.parentId === conflictCategoryId;
                            });
                            
                            // 为每个冲突节点创建临时连接
                            conflictNodes.forEach(function(conflictNode) {
                                var tempLink = {
                                    source: clickedNode,
                                    target: conflictNode,
                                    type: "conflict"
                                };
                                tempLinks.push(tempLink);
                                conflictingNodes.push(conflictNode); // 添加到冲突节点数组
                            });
                        }
                    });
                }
                
                // 高亮所有冲突节点
                conflictingNodes.forEach(function(conflictNode) {
                    var conflictNodeElement = nodeGroup.selectAll('.node-group').filter(function(data) { 
                        return data.id === conflictNode.id; 
                    });
                    
                    if (!conflictNodeElement.empty()) {
                        // 保存原始颜色
                        var originalFill = conflictNodeElement.select('circle.main').attr("fill");
                        var originalStroke = conflictNodeElement.select('circle.main').attr("stroke");
                        originalColors.set(conflictNode.id, {fill: originalFill, stroke: originalStroke});
                        
                        // 高亮冲突节点
                        conflictNodeElement.select('circle.main')
                            .transition().duration(300)
                            .attr("fill", conflictNode.parentColor || conflictNode.color)
                            .attr("stroke", conflictNode.parentColor || conflictNode.color)
                            .attr("stroke-width", 1.5)
                            .style("filter", "drop-shadow(0 0 " + (conflictNode.radius * 0.6) + "px " + (conflictNode.parentColor || conflictNode.color) + ")");
                            
                        conflictNodeElement.select('.glow')
                            .transition().duration(300)
                            .attr("opacity", 0.7);
                    }
                });
                
                // 绘制临时连接
                var tempLinkGroup = svg.append("g").attr("class", "temp-links");
                
                tempLinks.forEach(function(link) {
                    tempLinkGroup.append("line")
                        .attr("x1", link.source.x)
                        .attr("y1", link.source.y)
                        .attr("x2", link.target.x)
                        .attr("y2", link.target.y)
                        .attr("stroke", "#ff4d4d")
                        .attr("stroke-width", 2)
                        .attr("stroke-dasharray", "5,5")
                        .attr("opacity", 0.8)
                        .attr("class", "temp-conflict-link");
                });
                
                // 3秒后移除临时连接并恢复节点颜色
                setTimeout(function() {
                    // 恢复所有节点的颜色
                    originalColors.forEach(function(original, nodeId) {
                        var nodeElement = nodeGroup.selectAll('.node-group').filter(function(data) { 
                            return data.id === nodeId; 
                        });
                        
                        if (!nodeElement.empty()) {
                            nodeElement.select('circle.main')
                                .transition().duration(500)
                                .attr("fill", original.fill || "#888888")
                                .attr("stroke", original.stroke || "#666666")
                                .attr("stroke-width", function(d) { return d.isCategory ? 1 : 0.5; })
                                .style("filter", "none");
                                
                            nodeElement.select('.glow')
                                .transition().duration(500)
                                .attr("opacity", 0);
                        }
                    });
                    
                    // 移除临时连接
                    tempLinkGroup.transition()
                        .duration(500)
                        .attr("opacity", 0)
                        .remove();
                }, 3000);
            }
            
            function nodeDragStart(event, d) {
                isNearlyStationary = false;
                lastMovementTime = Date.now();
                d.isDragging = true;
                draggedNode = d;
                d3.select(this).style("cursor", "grabbing");
                velocityHistory = [];
                event.sourceEvent.stopPropagation();
            }
            
            function nodeDrag(event, d) {
                if (d === draggedNode) {
                    var pos = getMousePos(event);
                    d.x = Math.max(d.radius, Math.min(width - d.radius, pos.x));
                    d.y = Math.max(d.radius, Math.min(height - d.radius, pos.y));
                    d.vx = event.deltaX || 0;
                    d.vy = event.deltaY || 0;
                    
                    velocityHistory.push({ vx: d.vx, vy: d.vy, time: Date.now() });
                    if (velocityHistory.length > 10) velocityHistory.shift();
                    
                    updatePositions();
                    updateLinks();
                }
            }
            
            function nodeDragEnd(event, d) {
                d.isDragging = false;
                draggedNode = null;
                d3.select(this).style("cursor", "grab");
                
                if (velocityHistory.length > 0) {
                    var avgVel = velocityHistory.reduce(function(acc, v) {
                        return { vx: acc.vx + v.vx * 0.3, vy: acc.vy + v.vy * 0.3 };
                    }, { vx: 0, vy: 0 });
                    d.vx = avgVel.vx * 2;
                    d.vy = avgVel.vy * 2;
                }
            }
            
            function updatePositions() {
                nodeGroup.selectAll(".node-group").attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
                
                labelGroup.selectAll("[class^='label-']").each(function() {
                    var className = this.className.baseVal.replace("label-group ", "");
                    var id = className.replace("label-", "");
                    var nodeData = physicsNodes.find(function(n) { return n.id === id && !n.parentId; });
                    if (nodeData) {
                        d3.select(this).attr("transform", "translate(" + nodeData.x + "," + (nodeData.y + nodeData.radius + 20) + ")");
                    }
                });
            }
            
            var lastMovementTime = Date.now();
            var isNearlyStationary = false;
            
            function applyPhysics() {
                if (isNearlyStationary) return;
                
                var friction = 0.85; // 增加摩擦力，让系统更容易停止
                var springStrength = 0.005; // 减少弹簧强度，让节点分布更分散
                var repulsionStrength = 0.4; // 增强斥力
                var boundaryForce = 0.2;
                var velocityThreshold = 0.02; // 更低的速度阈值
                
                var totalVelocity = 0;
                var maxVelocity = 0;
                
                categoryNodes.forEach(function(node) {
                    if (node.isDragging) return;
                    
                    node.vx *= friction;
                    node.vy *= friction;
                    
                    var distFromCenter = Math.sqrt(Math.pow(node.x - centerX, 2) + Math.pow(node.y - centerY, 2));
                    var targetDist = orbitRadius * 1.2; // 增加目标距离，让节点分布更分散
                    var distError = distFromCenter - targetDist;
                    
                    if (Math.abs(distError) > 3) {
                        var force = distError * springStrength;
                        var angle = Math.atan2(node.y - centerY, node.x - centerX);
                        node.vx -= Math.cos(angle) * force;
                        node.vy -= Math.sin(angle) * force;
                    }
                    
                    // 分类节点之间的斥力（根据冲突程度调整）
                    categoryNodes.forEach(function(other) {
                        if (node === other) return;
                        
                        var dx = other.x - node.x;
                        var dy = other.y - node.y;
                        var dist = Math.sqrt(dx * dx + dy * dy);
                        
                        // 根据冲突规则调整斥力
                        var conflictMultiplier = 1.0;
                        var rules = conflictRules[node.id] || { conflicts: [], compatible: [], weak: [] };
                        
                        if (rules.conflicts && rules.conflicts.indexOf(other.id) !== -1) {
                            conflictMultiplier = 2.0; // 冲突类别斥力更大
                        } else if (rules.compatible && rules.compatible.indexOf(other.id) !== -1) {
                            conflictMultiplier = 0.7; // 兼容类别斥力较小
                        } else if (rules.weak && rules.weak.indexOf(other.id) !== -1) {
                            conflictMultiplier = 1.2; // 弱冲突类别斥力适中
                        }
                        
                        var safeDist = (node.radius + other.radius) * 3.0 * conflictMultiplier; // 根据冲突程度调整距离
                        
                        if (dist < safeDist && dist > 0) {
                            var force = (safeDist - dist) * repulsionStrength * conflictMultiplier;
                            var nx = dx / dist;
                            var ny = dy / dist;
                            node.vx -= nx * force;
                            node.vy -= ny * force;
                        }
                    });
                    
                    if (!node.isDragging) {
                        node.x += node.vx;
                        node.y += node.vy;
                    }
                    
                    var margin = node.radius + 15;
                    if (node.x < margin) {
                        node.x = margin;
                        node.vx *= -boundaryForce;
                    }
                    if (node.x > width - margin) {
                        node.x = width - margin;
                        node.vx *= -boundaryForce;
                    }
                    if (node.y < margin) {
                        node.y = margin;
                        node.vy *= -boundaryForce;
                    }
                    if (node.y > height - margin) {
                        node.y = height - margin;
                        node.vy *= -boundaryForce;
                    }
                });
                
                allEnchantmentNodes.forEach(function(node) {
                    if (node.isDragging) return;
                    
                    node.vx *= friction; // 对附魔节点也使用相同的摩擦力
                    node.vy *= friction;
                    
                    var categoryNode = categoryNodes.find(function(n) { return n.id === node.parentId; });
                    if (categoryNode) {
                        var dx = categoryNode.x - node.x;
                        var dy = categoryNode.y - node.y;
                        var dist = Math.sqrt(dx * dx + dy * dy);
                        var idealDist = 40 + (node.radius * 8); // 调整理想距离
                        var distError = dist - idealDist;
                        
                        // 弹簧力，让附魔节点围绕其分类节点
                        var springForce = distError * springStrength * 1.2;
                        node.vx += (dx / (dist || 1)) * springForce;
                        node.vy += (dy / (dist || 1)) * springForce;
                        
                        // 与父分类节点的距离约束
                        var safeDist = node.radius + categoryNode.radius + 5;
                        if (dist < safeDist && dist > 0) {
                            var repulsion = (safeDist - dist) * repulsionStrength * 0.3;
                            var nx = dx / dist;
                            var ny = dy / dist;
                            node.vx -= nx * repulsion;
                            node.vy -= ny * repulsion;
                        }
                    }
                    
                    // 不同分类的附魔节点之间的斥力
                    allEnchantmentNodes.forEach(function(other) {
                        if (node === other) return;
                        
                        // 获取两个节点所属分类的冲突规则
                        var nodeRules = conflictRules[node.parentId] || { conflicts: [], compatible: [], weak: [] };
                        var otherRules = conflictRules[other.parentId] || { conflicts: [], compatible: [], weak: [] };
                        
                        var conflictMultiplier = 1.0;
                        if (nodeRules.conflicts && nodeRules.conflicts.indexOf(other.parentId) !== -1) {
                            conflictMultiplier = 1.8; // 冲突类别斥力更大
                        } else if (nodeRules.compatible && nodeRules.compatible.indexOf(other.parentId) !== -1) {
                            conflictMultiplier = 0.6; // 兼容类别斥力较小
                        } else if (nodeRules.weak && nodeRules.weak.indexOf(other.parentId) !== -1) {
                            conflictMultiplier = 1.1; // 弱冲突类别斥力适中
                        }
                        
                        var odx = other.x - node.x;
                        var ody = other.y - node.y;
                        var odist = Math.sqrt(odx * odx + ody * ody);
                        var osafeDist = (node.radius + other.radius) * 2.0 * conflictMultiplier; // 跨分类节点间更大的斥力范围
                        
                        if (odist < osafeDist && odist > 0) {
                            var ofrepulsion = (osafeDist - odist) * repulsionStrength * 0.6 * conflictMultiplier;
                            var onx = odx / odist;
                            var ony = ody / odist;
                            node.vx -= onx * ofrepulsion;
                            node.vy -= ony * ofrepulsion;
                        }
                    });
                    
                    // 同分类节点之间的斥力
                    allEnchantmentNodes.forEach(function(other) {
                        if (node === other || node.parentId !== other.parentId) return;
                        
                        var odx = other.x - node.x;
                        var ody = other.y - node.y;
                        var odist = Math.sqrt(odx * odx + ody * ody);
                        var osafeDist = (node.radius + other.radius) * 1.8;
                        
                        if (odist < osafeDist && odist > 0) {
                            var ofrepulsion = (osafeDist - odist) * repulsionStrength * 0.3;
                            var onx = odx / odist;
                            var ony = ody / odist;
                            node.vx -= onx * ofrepulsion;
                            node.vy -= ony * ofrepulsion;
                        }
                    });
                    
                    // 与分类节点之间的斥力（跨分类）
                    categoryNodes.forEach(function(otherCat) {
                        if (categoryNode && otherCat.id !== categoryNode.id) {
                            // 检查冲突规则
                            var nodeRules = conflictRules[categoryNode.id] || { conflicts: [], compatible: [], weak: [] };
                            var conflictMultiplier = 1.0;
                            
                            if (nodeRules.conflicts && nodeRules.conflicts.indexOf(otherCat.id) !== -1) {
                                conflictMultiplier = 1.5; // 冲突类别斥力更大
                            } else if (nodeRules.compatible && nodeRules.compatible.indexOf(otherCat.id) !== -1) {
                                conflictMultiplier = 0.7; // 兼容类别斥力较小
                            } else if (nodeRules.weak && nodeRules.weak.indexOf(otherCat.id) !== -1) {
                                conflictMultiplier = 1.2; // 弱冲突类别斥力适中
                            }
                            
                            var cdx = otherCat.x - node.x;
                            var cdy = otherCat.y - node.y;
                            var cdist = Math.sqrt(cdx * cdx + cdy * cdy);
                            var csafeDist = (node.radius + otherCat.radius + 15) * conflictMultiplier;
                            
                            if (cdist < csafeDist && cdist > 0) {
                                var crepulsion = (csafeDist - cdist) * repulsionStrength * 0.3 * conflictMultiplier;
                                var cnx = cdx / cdist;
                                var cny = cdy / cdist;
                                node.vx -= cnx * crepulsion;
                                node.vy -= cny * crepulsion;
                            }
                        }
                    });
                    
                    if (!node.isDragging) {
                        node.x += node.vx;
                        node.y += node.vy;
                    }
                    
                    var margin = node.radius + 10;
                    if (node.x < margin) {
                        node.x = margin;
                        node.vx *= -boundaryForce;
                    }
                    if (node.x > width - margin) {
                        node.x = width - margin;
                        node.vx *= -boundaryForce;
                    }
                    if (node.y < margin) {
                        node.y = margin;
                        node.vy *= -boundaryForce;
                    }
                    if (node.y > height - margin) {
                        node.y = height - margin;
                        node.vy *= -boundaryForce;
                    }
                });
                
                physicsNodes.forEach(function(node) {
                    if (node.isDragging) return;
                    var speed = Math.sqrt(node.vx * node.vx + node.vy * node.vy);
                    totalVelocity += speed;
                    if (speed > maxVelocity) maxVelocity = speed;
                });
                
                if (maxVelocity < velocityThreshold) {
                    if (Date.now() - lastMovementTime > 2500) { // 增加稳定时间
                        isNearlyStationary = true;
                        physicsNodes.forEach(function(n) {
                            n.vx = 0;
                            n.vy = 0;
                        });
                    }
                } else {
                    lastMovementTime = Date.now();
                }
            }
            
            function animate() {
                applyPhysics();
                updatePositions();
                updateLinks();
                requestAnimationFrame(animate);
            }
            
            physicsNodes.forEach(function(d) {
                var nodeG = nodeGroup.append("g")
                    .attr("class", "node-group")
                    .attr("data-id", d.id) // 添加ID属性以便选择
                    .datum(d)
                    .style("cursor", d.isCategory ? "grab" : "pointer")
                    .on("mouseenter", nodeEnter)
                    .on("mousemove", nodeMove)
                    .on("mouseleave", nodeLeave)
                    .on("click", nodeClick)
                    .call(
                        d3.drag()
                            .on("start", nodeDragStart)
                            .on("drag", nodeDrag)
                            .on("end", nodeDragEnd)
                    );
                
                nodeG.append("circle")
                    .attr("class", "glow")
                    .attr("r", function(d) { return d.radius * 1.3; })
                    .attr("fill", function(d) { return "url(#" + d.gradientId + ")"; })
                    .attr("opacity", 0); // 默认不显示发光效果
                
                nodeG.append("circle")
                    .attr("class", "main")
                    .attr("r", function(d) { return d.radius; })
                    .attr("fill", "#888888") // 默认为灰色
                    .attr("stroke", "#666666") // 默认为深灰色
                    .attr("stroke-width", function(d) { return d.isCategory ? 1 : 0.5; })
                    .attr("stroke-opacity", 0.5)
                    .style("filter", "none"); // 默认无滤镜
                
                // 不再在节点上显示图标
            });
            
            // 不再显示外部标签
            
            svg.on("wheel", function(event) {
                event.preventDefault();
                var delta = event.deltaY > 0 ? 0.9 : 1.1;
                zoomLevel = Math.max(0.3, Math.min(3, zoomLevel * delta));
                applyZoom();
            });
            
            svg.on("click", function() {
                var details = container.querySelector(".category-details");
                if (details) details.classList.remove("visible");
            });
            
            svg.call(d3.drag()
                .on("start", function() {
                    dragStart.x = currentTransform.x;
                    dragStart.y = currentTransform.y;
                    touchStartTime = Date.now();
                })
                .on("drag", function(event) {
                    currentTransform.x = dragStart.x + event.dx;
                    currentTransform.y = dragStart.y + event.dy;
                    applyZoom();
                }));
            
            svg.append("text")
                .attr("x", width / 2)
                .attr("y", 30)
                .attr("text-anchor", "middle")
                .attr("fill", "rgba(255,255,255,0.25)")
                .attr("font-weight", "300")
                .attr("pointer-events", "none")
                .text("附魔冲突星图");
            
            animate();
            
        } catch (error) {
            console.error("Error rendering enhanced conflict graph:", error);
            renderSimpleConflictGraph(container);
        }
    }
    
    function renderSimpleConflictGraph(container) {
        container.innerHTML = '<div style="padding: 40px; text-align: center; color: var(--text-secondary);">' +
                             '<h3 style="margin-bottom: 20px;">附魔冲突关系图</h3>' +
                             '<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 20px; max-width: 800px; margin: 0 auto;">';
        
        Object.entries(enchantments || {}).forEach(([key, cat]) => {
            var rules = conflictRules[key] || { conflicts: [], compatible: [] };
            var conflictNames = rules.conflicts.map(c => enchantments[c]?.name || c).join("、");
            
            container.innerHTML += `
                <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px; border-left: 4px solid ${cat.color};">
                    <h4 style="color: ${cat.color}; margin-bottom: 10px;">${cat.icon || ''} ${cat.name}</h4>
                    <p style="font-size: 12px; margin-bottom: 8px;">${cat.enchantments?.length || 0} 个附魔</p>
                    ${conflictNames ? `<p style="font-size: 11px; color: #f85149;">冲突: ${conflictNames}</p>` : ''}
                </div>
            `;
        });
        
        container.innerHTML += '</div></div>';
    }

    window.showEnchantmentDetail = showEnchantmentDetail;
    
    window.switchToChinese = function() {
        handleLanguageChange({target: {dataset: {lang: 'zh'}}});
    };
    
    window.switchToEnglish = function() {
        handleLanguageChange({target: {dataset: {lang: 'en'}}});
    };
    
    window.copyEnchantmentInfo = function(enchantId) {
        let enchant = null;
        for (const cat of Object.keys(enchantments || {})) {
            if (enchantments[cat]?.enchantments) {
                const found = enchantments[cat].enchantments.find(e => e.id === enchantId);
                if (found) {
                    enchant = found;
                    break;
                }
            }
        }
        
        if (enchant) {
            const text = `【${enchant.name}】\n等级: ${enchant.level || 'I'}\n描述: ${enchant.description}\n材料: ${enchant.materials || '多种'}\n获取: ${enchant.obtain || '多种途径'}\nID: ${enchant.id}`;
            navigator.clipboard.writeText(text).then(() => {
                alert('已复制到剪贴板');
            }).catch(() => {
                prompt('复制失败，请手动复制:', text);
            });
        }
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
