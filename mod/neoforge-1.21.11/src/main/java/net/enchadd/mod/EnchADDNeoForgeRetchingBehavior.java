package net.enchadd.mod;

import net.enchadd.mod.common.RetchingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

final class EnchADDNeoForgeRetchingBehavior {
    private EnchADDNeoForgeRetchingBehavior() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Start.class, EnchADDNeoForgeRetchingBehavior::onStart);
        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Finish.class, EnchADDNeoForgeRetchingBehavior::onFinish);
    }

    private static void onStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) RetchingBehavior.beforeFood(player, event.getItem());
    }

    private static void onFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) RetchingBehavior.afterFood(player, event.getItem());
    }
}
