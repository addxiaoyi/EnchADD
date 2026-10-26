package net.enchadd.mod;

import net.enchadd.mod.common.ParryBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeParry {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeParry::onBlocked);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeParry::onIncoming);
    }

    private static void onBlocked(LivingDamageEvent.Post event) {
        if (event.getBlockedDamage() > 0.0f) {
            ParryBehavior.arm(event.getEntity(), event.getSource(), event.getBlockedDamage());
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                    && event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
                RiposteBehavior.blocked(player, player.getUseItem(), attacker);
            }
        }
    }

    private static void onIncoming(LivingIncomingDamageEvent event) {
        event.setAmount(ParryBehavior.adjustCounterattack(
                event.getEntity(), event.getSource(), event.getAmount()));
    }
}
