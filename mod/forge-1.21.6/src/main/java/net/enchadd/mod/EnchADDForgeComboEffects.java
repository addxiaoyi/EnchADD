package net.enchadd.mod;

import net.enchadd.mod.common.LodestarBehavior;
import net.enchadd.mod.common.HighgroundBehavior;
import net.enchadd.mod.common.MeasuredBehavior;
import net.enchadd.mod.common.ResonanceBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeComboEffects {
    private EnchADDForgeComboEffects() {}

    static void register() { LivingDamageEvent.BUS.addListener(new EnchADDForgeComboEffects()::onDamage); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        float amount = HighgroundBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount());
        amount = MeasuredBehavior.adjustDamage(event.getEntity(), event.getSource(), amount);
        amount = LodestarBehavior.adjustDamage(event.getEntity(), event.getSource(), amount);
        event.setAmount(ResonanceBehavior.adjustDamage(event.getEntity(), event.getSource(), amount));
    }
}
