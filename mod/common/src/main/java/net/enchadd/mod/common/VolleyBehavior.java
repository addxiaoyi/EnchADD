package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VolleyBehavior {
    private static final ResourceKey<Enchantment> VOLLEY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "volley"));
    private static final Set<UUID> PROCESSED = ConcurrentHashMap.newKeySet();
    private static final int MAX_LEVEL = 3;
    private static final int MAX_EXTRA_ARROWS = 32;

    private VolleyBehavior() {}

    public static void onLaunch(AbstractArrow arrow) {
        if (!(arrow.level() instanceof ServerLevel level) || !(arrow.getOwner() instanceof ServerPlayer shooter)) return;
        if (!PROCESSED.add(arrow.getUUID())) return;
        Holder<Enchantment> holder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(VOLLEY);
        int extra = Math.min(MAX_EXTRA_ARROWS, Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shooter.getMainHandItem())));
        Vec3 velocity = arrow.getDeltaMovement();
        if (extra <= 0 || velocity.lengthSqr() <= 0.0) return;
        for (int i = 0; i < extra; i++) {
            AbstractArrow copy = arrow instanceof SpectralArrow
                    ? new SpectralArrow(level, arrow.getX(), arrow.getY(), arrow.getZ(), ItemStack.EMPTY, ItemStack.EMPTY)
                    : new Arrow(level, arrow.getX(), arrow.getY(), arrow.getZ(), ItemStack.EMPTY, ItemStack.EMPTY);
            copy.setOwner(shooter);
            copy.setBaseDamage(baseDamage(arrow));
            copy.setDeltaMovement(spread(velocity, RandomSource.create()));
            copy.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            PROCESSED.add(copy.getUUID());
            level.addFreshEntity(copy);
        }
    }

    private static Vec3 spread(Vec3 velocity, RandomSource random) {
        Vec3 direction = velocity.normalize().add(random.nextGaussian() * 0.025, random.nextGaussian() * 0.025, random.nextGaussian() * 0.025).normalize();
        return direction.scale(velocity.length());
    }

    private static double baseDamage(AbstractArrow arrow) {
        try {
            Field field = AbstractArrow.class.getDeclaredField("baseDamage");
            field.setAccessible(true);
            Object value = field.get(arrow);
            return value instanceof Number number && Double.isFinite(number.doubleValue()) ? Math.max(0.0, number.doubleValue()) : 2.0;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 2.0;
        }
    }
}
