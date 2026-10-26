package net.enchadd.mod;
import net.enchadd.mod.common.TenderstepBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
final class EnchADDNeoForgeTenderstep { static void register(){NeoForge.EVENT_BUS.addListener(BlockEvent.FarmlandTrampleEvent.class,EnchADDNeoForgeTenderstep::onTrample);} private static void onTrample(BlockEvent.FarmlandTrampleEvent event){if(TenderstepBehavior.protects(event.getEntity(),event.getLevel().getBlockState(event.getPos())))event.setCanceled(true);} }
