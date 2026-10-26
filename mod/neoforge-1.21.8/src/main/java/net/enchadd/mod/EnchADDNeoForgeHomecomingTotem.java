package net.enchadd.mod;

import net.enchadd.mod.common.HomecomingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

final class EnchADDNeoForgeHomecomingTotem {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingUseTotemEvent.class, EnchADDNeoForgeHomecomingTotem::onTotem); }
    private static void onTotem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) HomecomingBehavior.onTotem(player, event.getTotem());
    }
}
