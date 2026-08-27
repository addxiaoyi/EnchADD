package net.enchadd.listeners.support;

import net.enchadd.enchants.StonewakeEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public final class StonewakeEffectSupport {

    private static final Set<Material> ORE_TYPES = buildOreTypes();

    private final StonewakeEnchant config;

    public StonewakeEffectSupport(@NotNull StonewakeEnchant config) {
        this.config = config;
    }

    public void handleBlockBreak(@NotNull BlockBreakEvent event, @NotNull Enchantment enchant) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = PerformanceUtils.getEnchantLevel(tool, enchant);
        if (level <= 0) {
            return;
        }
        if (!ORE_TYPES.contains(event.getBlock().getType())) {
            return;
        }

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getDurationSecondsPerLevel(), level);
        int amplifier = config.getHasteAmplifier();
        PotionEffect current = player.getActivePotionEffects().stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.HASTE))
                .findFirst()
                .orElse(null);
        if (current != null) {
            if (current.getAmplifier() > amplifier) {
                return;
            }
            if (current.getAmplifier() == amplifier && current.getDuration() >= durationTicks) {
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, durationTicks, amplifier, false, false, true));
    }

    private static Set<Material> buildOreTypes() {
        Set<Material> ores = EnumSet.noneOf(Material.class);
        for (Material material : Material.values()) {
            String name = material.name();
            if (name.endsWith("_ORE") || material == Material.ANCIENT_DEBRIS) {
                ores.add(material);
            }
        }
        return ores;
    }
}
