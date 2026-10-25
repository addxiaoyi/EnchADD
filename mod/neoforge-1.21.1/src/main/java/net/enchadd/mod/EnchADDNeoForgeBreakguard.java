package net.enchadd.mod;

import net.enchadd.mod.common.BreakguardBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

final class EnchADDNeoForgeBreakguard {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingShieldBlockEvent.class, EnchADDNeoForgeBreakguard::onShieldBlock);
    }

    // LivingShieldBlockEvent fires after vanilla shield absorption; lowering blockedDamage
    // converts the Breakguard bonus into damage the defender still takes.
    private static void onShieldBlock(LivingShieldBlockEvent event) {
        float reduced = BreakguardBehavior.reduceBlockedAmount(
                event.getEntity(), event.getDamageSource(), event.getOriginalBlockedDamage(), event.getBlockedDamage());
        if (reduced < event.getBlockedDamage()) {
            event.setBlockedDamage(reduced);
        }
    }
}
