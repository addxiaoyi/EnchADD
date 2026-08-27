package net.enchadd.listeners.support;

import net.enchadd.EnchADD;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public final class VampirismRuntimeSupport {

    private final Enchantment vampirism;

    public VampirismRuntimeSupport(@Nullable Enchantment vampirism) {
        this.vampirism = vampirism;
    }

    public boolean hasEnchantment() {
        return vampirism != null;
    }

    public boolean hasVampirismEnchant(@Nullable Player player) {
        if (player == null || vampirism == null) {
            return false;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe((LivingEntity) player);
        if (equipment == null) {
            return false;
        }
        return EnchADD.getSumOfEnchantLevels(equipment, vampirism) > 0;
    }

    public void reconcileTracking(@Nullable Player player, @NotNull Set<UUID> activeCandidates) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        if (!player.isOnline() || player.isDead()) {
            activeCandidates.remove(id);
            return;
        }
        if (hasVampirismEnchant(player)) {
            activeCandidates.add(id);
            return;
        }
        activeCandidates.remove(id);
    }
}
