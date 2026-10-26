package net.enchadd.mod;
import net.enchadd.mod.common.WaterMovementBehavior;
import net.enchadd.mod.common.WaysongBehavior;
import net.enchadd.mod.common.StarwishBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
final class EnchADDForgeWater { static void register(){PlayerInteractEvent.RightClickItem.BUS.addListener(new EnchADDForgeWater()::onUse);
        TickEvent.PlayerTickEvent.BUS.addListener(new EnchADDForgeWater()::onTick);}
 @SubscribeEvent public void onUse(PlayerInteractEvent.RightClickItem e){if(e.getEntity() instanceof ServerPlayer p){var item=p.getItemInHand(e.getHand());WaterMovementBehavior.useShell(p,item);WaysongBehavior.use(p,item);StarwishBehavior.use(p,item);}}
 @SubscribeEvent public void onTick(TickEvent.PlayerTickEvent e){if(e.phase != TickEvent.Phase.END) return; if(e.player instanceof ServerPlayer p)WaterMovementBehavior.tick(p);}}
