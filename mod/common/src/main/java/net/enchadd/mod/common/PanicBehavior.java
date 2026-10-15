package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PanicBehavior {
    private static final ResourceKey<Enchantment> PANIC = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "panic"));
    private static final ConcurrentHashMap<UUID, Long> LAST_TRIGGER = new ConcurrentHashMap<>();
    private PanicBehavior() {}

    public static void maybePanic(ServerPlayer player, DamageSource source) {
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PANIC);
        int level = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            level += EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(slot));
        }
        if (level <= 0 || player.level().getGameTime() < LAST_TRIGGER.getOrDefault(player.getUUID(), 0L)) return;
        if (player.getRandom().nextDouble() >= Math.min(0.6, 0.025 * Math.min(1, level))) return;
        ItemStack[] hotbar = new ItemStack[9];
        for (int slot = 0; slot < hotbar.length; slot++) hotbar[slot] = player.getInventory().getItem(slot).copy();
        for (int slot = hotbar.length - 1; slot > 0; slot--) {
            int swap = player.getRandom().nextInt(slot + 1);
            ItemStack item = hotbar[slot];
            hotbar[slot] = hotbar[swap];
            hotbar[swap] = item;
        }
        for (int slot = 0; slot < hotbar.length; slot++) player.getInventory().setItem(slot, hotbar[slot]);
        LAST_TRIGGER.put(player.getUUID(), player.level().getGameTime() + 20);
    }
}
