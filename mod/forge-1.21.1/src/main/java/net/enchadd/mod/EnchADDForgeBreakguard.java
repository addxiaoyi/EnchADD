package net.enchadd.mod;

import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;

final class EnchADDForgeBreakguard {
    static void register() {
        MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBreakguard()::onShieldBlock);
    }

    // ShieldBlockEvent fires after vanilla shield absorption; lowering blockedDamage
    // converts the Breakguard bonus into damage the defender still takes.
    private void onShieldBlock(ShieldBlockEvent event) {
        float reduced = BreakguardBehavior.reduceBlockedAmount(
                event.getEntity(), event.getDamageSource(), event.getOriginalBlockedDamage(), event.getBlockedDamage());
        if (reduced < event.getBlockedDamage()) {
            event.setBlockedDamage(reduced);
        }
    }
}
