package net.enchadd.mod;

import net.enchadd.mod.common.PurifyBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgePurify {
    private EnchADDForgePurify() {}

    static void register() { LivingEntityUseItemEvent.Finish.BUS.addListener(new EnchADDForgePurify()::onFinish); }

    @SubscribeEvent
    public void onFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) PurifyBehavior.afterConsume(player);
    }
}
