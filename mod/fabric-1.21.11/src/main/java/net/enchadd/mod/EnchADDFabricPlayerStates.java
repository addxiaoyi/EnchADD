package net.enchadd.mod;

import net.enchadd.mod.common.PlayerStateBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

final class EnchADDFabricPlayerStates {
    private EnchADDFabricPlayerStates() {}
    static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(PlayerStateBehavior::tick));
    }
}
