package net.enchadd.mod;

import net.enchadd.mod.common.PivotBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgePivot {
    private EnchADDForgePivot() {}
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgePivot()::onShieldBlock); }
    @SubscribeEvent public void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PivotBehavior.blocked(player, player.getUseItem());
            RiposteBehavior.blocked(player, player.getUseItem(), event.getDamageSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker ? attacker : null);
        }
    }
}
