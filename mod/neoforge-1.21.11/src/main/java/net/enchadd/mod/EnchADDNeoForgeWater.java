package net.enchadd.mod;
import net.enchadd.mod.common.WaterMovementBehavior;
import net.enchadd.mod.common.WaysongBehavior;
import net.enchadd.mod.common.StarwishBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
final class EnchADDNeoForgeWater { static void register(){NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class,EnchADDNeoForgeWater::use);NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class,EnchADDNeoForgeWater::tick);}
 private static void use(PlayerInteractEvent.RightClickItem e){if(e.getEntity() instanceof ServerPlayer p){var item=p.getItemInHand(e.getHand());WaterMovementBehavior.useShell(p,item);WaysongBehavior.use(p,item);StarwishBehavior.use(p,item);}}
 private static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p)WaterMovementBehavior.tick(p);}}
