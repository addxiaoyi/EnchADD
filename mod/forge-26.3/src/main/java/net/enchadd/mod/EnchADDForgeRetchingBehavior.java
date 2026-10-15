package net.enchadd.mod;

import net.enchadd.mod.common.RetchingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeRetchingBehavior {
    private EnchADDForgeRetchingBehavior() {}

    static void register() { LivingEntityUseItemEvent.Start.BUS.addListener(new EnchADDForgeRetchingBehavior()::onStart);
        LivingEntityUseItemEvent.Finish.BUS.addListener(new EnchADDForgeRetchingBehavior()::onFinish); }

    @SubscribeEvent
    public void onStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) RetchingBehavior.beforeFood(player, event.getItem());
    }

    @SubscribeEvent
    public void onFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) RetchingBehavior.afterFood(player, event.getItem());
    }
}
