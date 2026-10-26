package net.enchadd.mod;

import net.enchadd.mod.common.ThirstBehavior;
import net.enchadd.mod.common.MortalWoundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeThirstBehavior {
    private EnchADDForgeThirstBehavior() {}

    static void register() { LivingDamageEvent.BUS.addListener(new EnchADDForgeThirstBehavior()::onDamage);
        LivingHealEvent.BUS.addListener(new EnchADDForgeThirstBehavior()::onHeal); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        ThirstBehavior.markFromDamage(event.getEntity(), event.getSource(), event.getAmount());
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            float amount = ThirstBehavior.adjustHealing(player, event.getAmount());
            event.setAmount(MortalWoundBehavior.adjustHealing(player, amount));
        }
    }
}
