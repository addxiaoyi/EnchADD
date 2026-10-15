package net.enchadd.mod;

import net.enchadd.mod.common.PivotBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgePivot {
    private EnchADDForgePivot() {}
    static void register() { ShieldBlockEvent.BUS.addListener(new EnchADDForgePivot()::onShieldBlock); }
    @SubscribeEvent public void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PivotBehavior.blocked(player, event.getBlockedWith());
            RiposteBehavior.blocked(player, event.getBlockedWith(), event.getDamageSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker ? attacker : null);
        }
    }
}
