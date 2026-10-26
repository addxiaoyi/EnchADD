package net.enchadd.mod;

import net.enchadd.mod.common.BackfireBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeBackfire {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBackfire()::onDamage); }
    @SubscribeEvent public void onDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            BackfireBehavior.onAttack(player, event.getSource(), event.getAmount());
        }
    }
}
