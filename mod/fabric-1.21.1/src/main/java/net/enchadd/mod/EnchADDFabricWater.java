package net.enchadd.mod;
import net.enchadd.mod.common.WaterMovementBehavior;
import net.enchadd.mod.common.WaysongBehavior;
import net.enchadd.mod.common.StarwishBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
final class EnchADDFabricWater { static void init() {
    UseItemCallback.EVENT.register((p,l,h)->{if(p instanceof ServerPlayer s&&!l.isClientSide()){var item=p.getItemInHand(h);WaterMovementBehavior.useShell(s,item);WaysongBehavior.use(s,item);StarwishBehavior.use(s,item);}return InteractionResultHolder.pass(p.getItemInHand(h));});
    ServerTickEvents.END_SERVER_TICK.register(s->s.getPlayerList().getPlayers().forEach(WaterMovementBehavior::tick));
}}
