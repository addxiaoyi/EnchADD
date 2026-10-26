package net.enchadd.mod;

import net.enchadd.mod.common.PivotBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

final class EnchADDNeoForgePivot {
    private EnchADDNeoForgePivot() {}
    static void register() { NeoForge.EVENT_BUS.addListener(LivingShieldBlockEvent.class, EnchADDNeoForgePivot::onShieldBlock); }
    private static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PivotBehavior.blocked(player, player.getUseItem());
            RiposteBehavior.blocked(player, player.getUseItem(), event.getDamageSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker ? attacker : null);
        }
    }
}
