package net.enchadd.mod;

import net.enchadd.mod.common.ThirstBehavior;
import net.enchadd.mod.common.MortalWoundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeThirstBehavior {
    private EnchADDNeoForgeThirstBehavior() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeThirstBehavior::onDamage);
        NeoForge.EVENT_BUS.addListener(LivingHealEvent.class, EnchADDNeoForgeThirstBehavior::onHeal);
    }

    private static void onDamage(LivingIncomingDamageEvent event) {
        ThirstBehavior.markFromDamage(event.getEntity(), event.getSource(), event.getAmount());
    }

    private static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            float amount = ThirstBehavior.adjustHealing(player, event.getAmount());
            event.setAmount(MortalWoundBehavior.adjustHealing(player, amount));
        }
    }
}
