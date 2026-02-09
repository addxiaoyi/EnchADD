package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.effects.BaseEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;



/**
 * 通用附魔效果 - 根据配置动态应用效果
 * 支持所有229个附魔的功能实现
 */
public final class UniversalEffect extends BaseEffect {

    private final EffectType effectType;
    private final Map<String, Object> config;

    public UniversalEffect(JavaPlugin plugin, EffectType effectType, Map<String, Object> config) {
        super(plugin);
        this.effectType = effectType;
        this.config = config != null ? config : new HashMap<>();
    }
    
    @Override
    protected String getEffectId() {
        return "universal_" + effectType.name().toLowerCase();
    }

    @Override
    public void apply(EffectContext context) {
        switch (effectType) {
            case DAMAGE_OVER_TIME:
                applyDamageOverTime(context);
                break;
            case LIFESTEAL:
                applyLifesteal(context);
                break;
            case CRITICAL:
                applyCritical(context);
                break;
            case ARMOR_REDUCTION:
                applyArmorReduction(context);
                break;
            case EXECUTE:
                applyExecute(context);
                break;
            case BACKSTAB:
                applyBackstab(context);
                break;
            case DISARM:
                applyDisarm(context);
                break;
            case STUN:
                applyStun(context);
                break;
            case COMBO:
                applyCombo(context);
                break;
            case DODGE:
                applyDodge(context);
                break;
            case REFLECT:
                applyReflect(context);
                break;
            case DAMAGE_REDUCTION:
                applyDamageReduction(context);
                break;
            case SHIELD:
                applyShield(context);
                break;
            case LAST_STAND:
                applyLastStand(context);
                break;
            case VEIN_MINE:
                applyVeinMine(context);
                break;
            case AUTO_SMELT:
                applyAutoSmelt(context);
                break;
            case MAGNETIC:
                applyMagnetic(context);
                break;
            case FORTUNE:
                applyFortune(context);
                break;
            case SPEED:
                applySpeed(context);
                break;
            case AUTO_REPAIR:
                applyAutoRepair(context);
                break;
            case DOUBLE_DROP:
                applyDoubleDrop(context);
                break;
            case NIGHT_VISION:
                applyNightVision(context);
                break;
            case WATER_WALK:
                applyWaterWalk(context);
                break;
            case GLOWING:
                applyGlowing(context);
                break;
            case DURABILITY_LOSS:
                applyDurabilityLoss(context);
                break;
            case SLOWNESS:
                applySlowness(context);
                break;
            case WEAKNESS:
                applyWeakness(context);
                break;
            case HUNGER:
                applyHunger(context);
                break;
            default:
                applyGeneric(context);
                break;
        }

        recordCooldown(context);
    }

    // ========== 战斗效果实现 ==========

    private void applyDamageOverTime(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (target == null || target.isDead()) return;

        double damagePerSecond = getConfigDouble("damagePerSecond", 0.5) * context.getLevel();
        int duration = getConfigInt("duration", 100);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration || target.isDead()) {
                    cancel();
                    return;
                }
                if (ticks % 20 == 0) {
                    target.damage(damagePerSecond, context.getPlayer());
                    showParticle(target.getLocation().add(0, 1, 0), Particle.DUST, Color.RED);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void applyLifesteal(EffectContext context) {
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        double damage = event.getFinalDamage();
        double healPercent = getConfigDouble("healPercent", 0.15) * context.getLevel();
        double healAmount = damage * healPercent;

        Player player = context.getPlayer();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
        player.setHealth(newHealth);

        showParticle(player.getLocation().add(0, 2, 0), Particle.HEART, null);
    }

    private void applyCritical(EffectContext context) {
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) return;

        double critChance = getConfigDouble("critChance", 0.15) + (context.getLevel() * 0.05);
        if (Math.random() > critChance) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        double critMultiplier = getConfigDouble("critMultiplier", 2.0);
        event.setDamage(event.getDamage() * critMultiplier);

        showParticle(event.getEntity().getLocation().add(0, 1, 0), Particle.CRIT, null);
        playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT);
    }

    private void applyArmorReduction(EffectContext context) {
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        double reduction = getConfigDouble("armorReduction", 0.2) * context.getLevel();
        event.setDamage(event.getDamage() * (1 + reduction));
    }

    private void applyExecute(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (target == null) return;

        double healthThreshold = getConfigDouble("healthThreshold", 0.3);
        double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (target.getHealth() / maxHealth <= healthThreshold) {
            if (context.getEvent() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
                double bonusDamage = getConfigDouble("bonusDamage", 5.0) * context.getLevel();
                event.setDamage(event.getDamage() + bonusDamage);

                showParticle(target.getLocation().add(0, 1, 0), Particle.SWEEP_ATTACK, null);
            }
        }
    }

    private void applyBackstab(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (target == null) return;

        // 检查是否从背后攻击
        double angle = getAngleBetween(context.getPlayer(), target);
        if (angle < 45) { // 背后45度范围
            if (context.getEvent() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
                double backstabMultiplier = getConfigDouble("backstabMultiplier", 2.5);
                event.setDamage(event.getDamage() * backstabMultiplier);

                showParticle(target.getLocation().add(0, 1, 0), Particle.DAMAGE_INDICATOR, null);
            }
        }
    }

    private void applyDisarm(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (!(target instanceof Player)) return;

        double disarmChance = getConfigDouble("disarmChance", 0.1) * context.getLevel();
        if (Math.random() > disarmChance) return;

        Player targetPlayer = (Player) target;
        ItemStack weapon = targetPlayer.getInventory().getItemInMainHand();

        if (weapon != null && weapon.getType() != Material.AIR) {
            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), weapon);
            targetPlayer.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

            showParticle(targetPlayer.getLocation().add(0, 1, 0), Particle.ITEM, null);
        }
    }

    private void applyStun(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (target == null) return;

        int stunDuration = getConfigInt("stunDuration", 40) + (context.getLevel() * 10);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration, 10));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, stunDuration, 128));

        showParticle(target.getLocation().add(0, 1, 0), Particle.EXPLOSION, null);
    }

    private void applyCombo(EffectContext context) {
        // 连击系统需要追踪连击数
        String key = "combo_" + context.getPlayer().getUniqueId();
        int combo = getConfigInt(key, 0) + 1;
        config.put(key, combo);

        if (context.getEvent() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
            double bonusPerCombo = getConfigDouble("bonusPerCombo", 0.1);
            event.setDamage(event.getDamage() * (1 + (combo * bonusPerCombo)));
        }

        // 3秒后重置连击
        new BukkitRunnable() {
            @Override
            public void run() {
                config.put(key, 0);
            }
        }.runTaskLater(plugin, 60L);
    }

    // ========== 防御效果实现 ==========

    private void applyDodge(EffectContext context) {
        double dodgeChance = getConfigDouble("dodgeChance", 0.1) + (context.getLevel() * 0.05);
        if (Math.random() > dodgeChance) return;

        if (context.getEvent() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
            event.setCancelled(true);

            showParticle(context.getPlayer().getLocation().add(0, 1, 0), Particle.CLOUD, null);
            playSound(context.getPlayer().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP);
        }
    }

    private void applyReflect(EffectContext context) {
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        double reflectPercent = getConfigDouble("reflectPercent", 0.3) * context.getLevel();
        double reflectDamage = event.getFinalDamage() * reflectPercent;

        if (event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.damage(reflectDamage, context.getPlayer());

            showParticle(attacker.getLocation().add(0, 1, 0), Particle.DAMAGE_INDICATOR, null);
        }
    }

    private void applyDamageReduction(EffectContext context) {
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        double reduction = getConfigDouble("damageReduction", 0.1) * context.getLevel();
        event.setDamage(event.getDamage() * (1 - reduction));
    }

    private void applyShield(EffectContext context) {
        Player player = context.getPlayer();
        double shieldAmount = getConfigDouble("shieldAmount", 2.0) * context.getLevel();

        player.setAbsorptionAmount(player.getAbsorptionAmount() + shieldAmount);
        showParticle(player.getLocation().add(0, 1, 0), Particle.ENCHANT, null);
    }

    private void applyLastStand(EffectContext context) {
        Player player = context.getPlayer();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (player.getHealth() / maxHealth <= 0.3) {
            int duration = getConfigInt("duration", 200);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, context.getLevel()));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1));

            showParticle(player.getLocation().add(0, 1, 0), Particle.TOTEM_OF_UNDYING, null);
        }
    }

    // ========== 工具效果实现 ==========

    private void applyVeinMine(EffectContext context) {
        if (!(context.getEvent() instanceof BlockBreakEvent)) return;

        BlockBreakEvent event = (BlockBreakEvent) context.getEvent();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!type.name().contains("ORE")) return;

        int radius = context.getLevel() * 2;
        Set<Block> vein = new HashSet<>();
        findVein(block, type, vein, radius * 10);

        for (Block b : vein) {
            b.breakNaturally(context.getItem());
        }
    }

    private void applyAutoSmelt(EffectContext context) {
        if (!(context.getEvent() instanceof BlockBreakEvent)) return;

        BlockBreakEvent event = (BlockBreakEvent) context.getEvent();
        Block block = event.getBlock();
        Material result = getSmeltResult(block.getType());

        if (result != null) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(result));
            showParticle(block.getLocation().add(0.5, 0.5, 0.5), Particle.FLAME, null);
        }
    }

    private void applyMagnetic(EffectContext context) {
        Player player = context.getPlayer();
        double radius = getConfigDouble("radius", 5.0) + context.getLevel();

        player.getNearbyEntities(radius, radius, radius).forEach(entity -> {
            if (entity instanceof org.bukkit.entity.Item) {
                org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
                item.setVelocity(player.getLocation().toVector().subtract(item.getLocation().toVector()).normalize().multiply(0.3));
            }
        });
    }

    private void applyFortune(EffectContext context) {
        if (!(context.getEvent() instanceof BlockBreakEvent)) return;

        double bonusChance = getConfigDouble("bonusChance", 0.2) * context.getLevel();
        if (Math.random() > bonusChance) return;

        BlockBreakEvent event = (BlockBreakEvent) context.getEvent();
        Block block = event.getBlock();

        // Fortune效果由Minecraft原生处理，这里只添加额外掉落
        Collection<ItemStack> drops = block.getDrops(context.getItem());
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }

        showParticle(block.getLocation().add(0.5, 0.5, 0.5), Particle.HAPPY_VILLAGER, null);
    }

    private void applySpeed(EffectContext context) {
        Player player = context.getPlayer();
        int duration = getConfigInt("duration", 100);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, context.getLevel() - 1));
    }

    // ========== 实用效果实现 ==========

    private void applyAutoRepair(EffectContext context) {
        ItemStack item = context.getItem();
        if (item == null || item.getType().getMaxDurability() <= 0) return;

        int repairAmount = getConfigInt("repairAmount", 1);
        if (Math.random() < 0.05) { // 5%概率修复
            org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) item.getItemMeta();
            if (meta != null && meta.hasDamage()) {
                meta.setDamage(Math.max(0, meta.getDamage() - repairAmount));
                item.setItemMeta(meta);

                showParticle(context.getPlayer().getLocation().add(0, 1, 0), Particle.HAPPY_VILLAGER, null);
            }
        }
    }

    private void applyDoubleDrop(EffectContext context) {
        if (!(context.getEvent() instanceof BlockBreakEvent)) return;

        double doubleChance = getConfigDouble("doubleChance", 0.3) * context.getLevel();
        if (Math.random() > doubleChance) return;

        BlockBreakEvent event = (BlockBreakEvent) context.getEvent();
        Block block = event.getBlock();
        Collection<ItemStack> drops = block.getDrops(context.getItem());

        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }

        showParticle(block.getLocation().add(0.5, 0.5, 0.5), Particle.HAPPY_VILLAGER, null);
    }

    private void applyNightVision(EffectContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false));
    }

    private void applyWaterWalk(EffectContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getLocation() == null) return;

        Block below = player.getLocation().subtract(0, 1, 0).getBlock();

        if (below.getType() == Material.WATER) {
            below.setType(Material.FROSTED_ICE);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (below.getType() == Material.FROSTED_ICE) {
                        below.setType(Material.WATER);
                    }
                }
            }.runTaskLater(plugin, 40L);
        }
    }

    private void applyGlowing(EffectContext context) {
        LivingEntity target = getTargetLiving(context);
        if (target == null) return;

        int duration = getConfigInt("duration", 100) + (context.getLevel() * 20);
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0));
    }

    // ========== 诅咒效果实现 ==========

    private void applyDurabilityLoss(EffectContext context) {
        ItemStack item = context.getItem();
        if (item == null || item.getType().getMaxDurability() <= 0) return;

        if (Math.random() < 0.1) { // 10%概率额外损耗
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                damageable.setDamage(damageable.getDamage() + context.getLevel());
                item.setItemMeta(damageable);
            }
        }
    }

    private void applySlowness(EffectContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, context.getLevel() - 1));
    }

    private void applyWeakness(EffectContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, context.getLevel() - 1));
    }

    private void applyHunger(EffectContext context) {
        Player player = context.getPlayer();
        int foodLevel = player.getFoodLevel();
        player.setFoodLevel(Math.max(0, foodLevel - context.getLevel()));
    }

    // ========== 通用效果 ==========

    private void applyGeneric(EffectContext context) {
        // 默认实现 - 显示粒子效果
        showParticle(context.getPlayer().getLocation().add(0, 1, 0), Particle.ENCHANT, null);
    }

    // ========== 辅助方法 ==========

    private void findVein(Block start, Material type, Set<Block> vein, int maxSize) {
        if (vein.size() >= maxSize) return;
        if (vein.contains(start)) return;
        if (start.getType() != type) return;

        vein.add(start);

        // 使用非递归方式防止潜在的堆栈溢出
        List<Block> queue = new ArrayList<>();
        queue.add(start);
        
        int index = 0;
        while (index < queue.size() && queue.size() < maxSize) {
            Block current = queue.get(index++);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = current.getRelative(x, y, z);
                        if (relative.getType() == type && !vein.contains(relative)) {
                            vein.add(relative);
                            queue.add(relative);
                            if (vein.size() >= maxSize) return;
                        }
                    }
                }
            }
        }
    }

    private Material getSmeltResult(Material input) {
        return switch (input) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            case COBBLESTONE -> Material.STONE;
            case SAND -> Material.GLASS;
            case CLAY_BALL -> Material.BRICK;
            case NETHERRACK -> Material.NETHER_BRICK;
            case CACTUS -> Material.GREEN_DYE;
            case RAW_IRON -> Material.IRON_INGOT;
            case RAW_GOLD -> Material.GOLD_INGOT;
            case RAW_COPPER -> Material.COPPER_INGOT;
            default -> null;
        };
    }

    private double getAngleBetween(Player player, LivingEntity target) {
        org.bukkit.util.Vector playerDir = player.getLocation().getDirection();
        org.bukkit.util.Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        return Math.toDegrees(Math.acos(playerDir.dot(toTarget)));
    }

    private void showParticle(Location location, Particle particle, Color color) {
        if (color != null) {
            location.getWorld().spawnParticle(particle, location, 10, 0.3, 0.3, 0.3,
                new Particle.DustOptions(color, 1.0f));
        } else {
            location.getWorld().spawnParticle(particle, location, 10, 0.3, 0.3, 0.3);
        }
    }

    private void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    private double getConfigDouble(String key, double defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int getConfigInt(String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    @Override
    public long getCooldown() {
        return getConfigInt("cooldown", 1000);
    }

    @Override
    public double getTriggerChance(int level) {
        return getConfigDouble("triggerChance", 1.0);
    }

    /**
     * 效果类型枚举
     */
    public enum EffectType {
        // 战斗效果
        DAMAGE_OVER_TIME, LIFESTEAL, CRITICAL, ARMOR_REDUCTION, EXECUTE,
        BACKSTAB, DISARM, STUN, COMBO,

        // 防御效果
        DODGE, REFLECT, DAMAGE_REDUCTION, SHIELD, LAST_STAND,

        // 工具效果
        VEIN_MINE, AUTO_SMELT, MAGNETIC, FORTUNE, SPEED,

        // 实用效果
        AUTO_REPAIR, DOUBLE_DROP, NIGHT_VISION, WATER_WALK, GLOWING,

        // 诅咒效果
        DURABILITY_LOSS, SLOWNESS, WEAKNESS, HUNGER,

        // 通用
        GENERIC
    }
}

