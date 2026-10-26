package net.enchadd.mod;

import net.enchadd.mod.common.EvasionBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeEvasion {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeEvasion()::onAttack); }
    @SubscribeEvent public boolean onAttack(LivingAttackEvent event) {
        return event.getEntity() instanceof ServerPlayer player
                && EvasionBehavior.shouldDodge(player, event.getSource());
    }
}
