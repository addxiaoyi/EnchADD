package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.NourishEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("UnstableApiUsage")
public class NourishListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(NourishEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(NourishEnchant.KEY);

    // 性能优化: 缓存配置对象
    private final NourishEnchant config;

    public NourishListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(NourishEnchant.KEY);
        this.config = (enchantObj instanceof NourishEnchant) ? (NourishEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (enchant == null || config == null) return;
        Player player = event.getPlayer();

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) return;

        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;

        // 性能优化: 使用 PerformanceUtils 的冷却检查
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getAbsorptionSecondsPerLevel(), level);
        int amplifier = Math.max(0, config.getAbsorptionAmplifier());
        PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION, durationTicks, amplifier, false, false, true);
        player.addPotionEffect(effect);
    }
}
