package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeBulwark {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBulwark()::onBlock); }
    @SubscribeEvent public void onBlock(ShieldBlockEvent event) {
        if (event.getBlockedDamage() > 0.0f && event.getEntity() instanceof ServerPlayer player) {
            BulwarkBehavior.maybeApply(player);
        }
    }
}
