package net.enchadd.mod;

import net.enchadd.mod.common.InsomniaBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeInsomnia {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeInsomnia()::onSleep); }
    @SubscribeEvent public void onSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && InsomniaBehavior.preventsSleep(player)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }
}
