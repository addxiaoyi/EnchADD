package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.portal.TeleportTransition;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HomecomingBehavior {
    private static final ResourceKey<Enchantment> HOMECOMING = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "homecoming")
    );
    private static final Map<UUID, Boolean> ARMED = new ConcurrentHashMap<>();

    private HomecomingBehavior() {}

    public static void capture(ServerPlayer player, DamageSource source) {
        if (hasHomecomingTotem(player, player.getMainHandItem(), source)
                || hasHomecomingTotem(player, player.getOffhandItem(), source)) {
            ARMED.put(player.getUUID(), true);
        }
    }

    public static void finish(ServerPlayer player, boolean rescued) {
        if (!rescued || !ARMED.remove(player.getUUID())) return;
        teleportHome(player);
    }

    public static void onTotem(ServerPlayer player, ItemStack totem) {
        if (!hasHomecomingTotem(player, totem, null)) return;
        teleportHome(player);
    }

    private static void teleportHome(ServerPlayer player) {
        player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));
    }

    private static boolean hasHomecomingTotem(ServerPlayer player, ItemStack stack, DamageSource source) {
        if (stack.isEmpty() || !stack.is(Items.TOTEM_OF_UNDYING)) return false;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HOMECOMING);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0;
    }
}
