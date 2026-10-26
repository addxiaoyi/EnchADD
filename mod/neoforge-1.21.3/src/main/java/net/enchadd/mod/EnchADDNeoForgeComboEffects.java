package net.enchadd.mod;

import net.enchadd.mod.common.LodestarBehavior;
import net.enchadd.mod.common.HighgroundBehavior;
import net.enchadd.mod.common.MeasuredBehavior;
import net.enchadd.mod.common.ResonanceBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeComboEffects {
    private EnchADDNeoForgeComboEffects() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeComboEffects::onDamage); }

    private static void onDamage(LivingIncomingDamageEvent event) {
        float amount = HighgroundBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount());
        amount = MeasuredBehavior.adjustDamage(event.getEntity(), event.getSource(), amount);
        amount = LodestarBehavior.adjustDamage(event.getEntity(), event.getSource(), amount);
        event.setAmount(ResonanceBehavior.adjustDamage(event.getEntity(), event.getSource(), amount));
    }
}
