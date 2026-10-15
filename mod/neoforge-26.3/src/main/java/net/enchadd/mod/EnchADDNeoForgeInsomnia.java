package net.enchadd.mod;

import net.enchadd.mod.common.InsomniaBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;

final class EnchADDNeoForgeInsomnia {
    static void register() { NeoForge.EVENT_BUS.addListener(CanPlayerSleepEvent.class, EnchADDNeoForgeInsomnia::onSleep); }
    private static void onSleep(CanPlayerSleepEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && InsomniaBehavior.preventsSleep(player)) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }
}
