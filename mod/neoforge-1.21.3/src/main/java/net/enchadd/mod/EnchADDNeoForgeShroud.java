package net.enchadd.mod;

import net.enchadd.mod.common.ShroudBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

final class EnchADDNeoForgeShroud {
    private EnchADDNeoForgeShroud() {}
    static void register() { NeoForge.EVENT_BUS.addListener(LivingChangeTargetEvent.class, EnchADDNeoForgeShroud::onTarget); }
    private static void onTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() != null && event.getEntity() instanceof net.minecraft.world.entity.Mob mob
                && ShroudBehavior.shouldBreakTarget(mob, event.getNewAboutToBeSetTarget())) {
            event.setNewAboutToBeSetTarget(null);
        }
    }
}
