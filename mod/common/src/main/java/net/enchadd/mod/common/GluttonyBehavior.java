package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class GluttonyBehavior {

    private static final ResourceKey<Enchantment> GLUTTONY = key("gluttony");

    private GluttonyBehavior() {
    }

    public static void tick(ServerPlayer player) {
        if (player.level().getGameTime() % 20L != 0L) {
            return;
        }
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GLUTTONY);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            sanitize(player.getInventory().getItem(slot), holder);
        }
    }

    private static void sanitize(ItemStack item, Holder<Enchantment> gluttony) {
        if (item.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(gluttony, item) <= 0) {
            return;
        }
        EnchantmentHelper.updateEnchantments(item, mutable ->
                mutable.removeIf(other -> !other.equals(gluttony)));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
