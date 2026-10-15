package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SoulboundBehavior {

    private static final ResourceKey<Enchantment> SOULBOUND = key("soulbound");
    private static final Map<UUID, List<ItemStack>> PENDING = new ConcurrentHashMap<>();

    private SoulboundBehavior() {
    }

    public static void capture(ServerPlayer player) {
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SOULBOUND);
        List<ItemStack> saved = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(holder, item) <= 0) {
                continue;
            }
            saved.add(item.copy());
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        if (!saved.isEmpty()) {
            PENDING.put(player.getUUID(), saved);
        }
    }

    public static void restore(ServerPlayer player) {
        List<ItemStack> saved = PENDING.remove(player.getUUID());
        if (saved == null || saved.isEmpty()) {
            return;
        }
        for (ItemStack item : saved) {
            if (!player.getInventory().add(item)) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), item));
            }
        }
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
