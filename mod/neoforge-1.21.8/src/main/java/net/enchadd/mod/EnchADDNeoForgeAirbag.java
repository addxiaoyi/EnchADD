package net.enchadd.mod;

import net.enchadd.mod.common.AirbagBehavior;
import net.enchadd.mod.common.DamageProtectionBehavior;
import net.enchadd.mod.common.SpearDefenseBehavior;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeAirbag {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeAirbag::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) {
        float protectedDamage = SpearDefenseBehavior.adjustDamage(event.getEntity(), event.getSource(), DamageProtectionBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getEntity() instanceof net.minecraft.world.entity.player.Player p ? MobExperienceBehavior.adjustIncomingDamage(p, event.getAmount()) : event.getAmount()));
        event.setAmount(AirbagBehavior.adjustDamage(event.getEntity(), event.getSource(), protectedDamage));
    }
}
