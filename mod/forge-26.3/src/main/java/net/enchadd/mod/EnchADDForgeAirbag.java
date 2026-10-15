package net.enchadd.mod;

import net.enchadd.mod.common.AirbagBehavior;
import net.enchadd.mod.common.DamageProtectionBehavior;
import net.enchadd.mod.common.SpearDefenseBehavior;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeAirbag {
    static void register() { LivingHurtEvent.BUS.addListener(new EnchADDForgeAirbag()::onHurt); }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) {
        float protectedDamage = SpearDefenseBehavior.adjustDamage(event.getEntity(), event.getSource(), DamageProtectionBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getEntity() instanceof net.minecraft.world.entity.player.Player p ? MobExperienceBehavior.adjustIncomingDamage(p, event.getAmount()) : event.getAmount()));
        event.setAmount(AirbagBehavior.adjustDamage(event.getEntity(), event.getSource(), protectedDamage));
    }
}
