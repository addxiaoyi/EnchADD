package net.enchadd.mod;

import net.enchadd.mod.common.ShroudBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeShroud {
    private EnchADDForgeShroud() {}
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeShroud()::onTarget); }
    @SubscribeEvent public void onTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() != null && event.getEntity() instanceof net.minecraft.world.entity.Mob mob
                && ShroudBehavior.shouldBreakTarget(mob, event.getNewTarget())) {
            event.setNewTarget(null);
        }
    }
}
