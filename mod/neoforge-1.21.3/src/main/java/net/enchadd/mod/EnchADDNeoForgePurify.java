package net.enchadd.mod;

import net.enchadd.mod.common.PurifyBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

final class EnchADDNeoForgePurify {
    private EnchADDNeoForgePurify() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Finish.class, EnchADDNeoForgePurify::onFinish); }

    private static void onFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) PurifyBehavior.afterConsume(player);
    }
}
