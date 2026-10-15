package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class BreakguardBehavior {

    private static final ResourceKey<Enchantment> BREAKGUARD = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "breakguard")
    );
    private static final int MAX_LEVEL = 3;
    private static final float BONUS_PER_LEVEL = 0.75f;
    private static final float MAX_BONUS = 2.25f;

    private BreakguardBehavior() {
    }

    /**
     * Raises an incoming melee hit before vanilla shield mitigation is applied.
     * The returned value is deliberately bounded because it is fed into the
     * loader's damage event or the vanilla shield-blocking path.
     */
    public static float adjustIncoming(LivingEntity defender, DamageSource source, float amount) {
        if (!isFinitePositive(amount) || !isMelee(source) || !hasShield(defender)) return amount;
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null) return amount;

        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BREAKGUARD);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) return amount;

        float adjusted = amount + Math.min(MAX_BONUS, BONUS_PER_LEVEL * level);
        return Float.isFinite(adjusted) ? adjusted : amount;
    }

    /**
     * Converts the bonus into reduced shield absorption for Fabric's
     * applyItemBlocking return hook. A full block therefore leaves only the
     * bounded Breakguard bonus as damage to the defender.
     */
    public static float reduceBlockedAmount(LivingEntity defender, DamageSource source, float incoming, float blocked) {
        if (!isFinitePositive(blocked) || !isFinitePositive(incoming) || !isMelee(source) || !hasShield(defender)) {
            return blocked;
        }
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null) return blocked;

        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BREAKGUARD);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) return blocked;

        float bonus = Math.min(MAX_BONUS, BONUS_PER_LEVEL * level);
        return Math.max(0.0f, blocked - bonus);
    }

    private static boolean isMelee(DamageSource source) {
        return source.getEntity() != null && source.getDirectEntity() == source.getEntity();
    }

    private static boolean hasShield(LivingEntity defender) {
        ItemStack used = defender.getUseItem();
        ItemStack offhand = defender.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        return defender.isBlocking() && !used.isEmpty() && used.is(Items.SHIELD)
                && !offhand.isEmpty() && offhand.is(Items.SHIELD);
    }

    private static boolean isFinitePositive(float value) {
        return Float.isFinite(value) && value > 0.0f;
    }
}
