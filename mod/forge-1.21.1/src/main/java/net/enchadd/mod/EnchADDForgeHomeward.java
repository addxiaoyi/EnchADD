package net.enchadd.mod;

import net.enchadd.mod.common.HomewardBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeHomeward {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeHomeward()::onHurt); }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.setAmount(HomewardBehavior.adjustDamage(player, event.getSource(), event.getAmount()));
        }
    }
}
