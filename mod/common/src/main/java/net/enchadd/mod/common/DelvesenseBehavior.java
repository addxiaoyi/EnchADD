package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public final class DelvesenseBehavior {

    private static final ResourceKey<Enchantment> DELVESENSE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "delvesense")
    );
    private static final double MAX_ACTIVATION_Y = 56.0;
    private static final double RADIUS = 10.0;
    private static final int GLOW_TICKS = 120;
    private static final int NIGHT_VISION_TICKS = 80;
    private static final int COOLDOWN_TICKS = 600;
    private static final int MAX_TARGETS = 32;

    private DelvesenseBehavior() {
    }

    public static boolean activate(ServerPlayer player, ItemStack item) {
        if (item.isEmpty() || !item.is(Items.COMPASS) || player.getY() > MAX_ACTIVATION_Y) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(item)) {
            return false;
        }
        Holder<Enchantment> enchantment = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DELVESENSE);
        if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, item) <= 0) {
            return false;
        }

        boolean changed = player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, false, false, true));
        AABB search = player.getBoundingBox().inflate(RADIUS);
        int affected = 0;
        for (Monster monster : player.level().getEntities(EntityTypeTest.forClass(Monster.class), search, monster -> monster.isAlive())) {
            changed |= monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false, true));
            if (++affected >= MAX_TARGETS) {
                break;
            }
        }
        if (changed) {
            player.getCooldowns().addCooldown(item, COOLDOWN_TICKS);
        }
        return changed;
    }
}
