package net.enchadd.mod;
import net.enchadd.mod.common.TenderstepBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeTenderstep { static void register(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeTenderstep()::onTrample);} @SubscribeEvent public boolean onTrample(BlockEvent.FarmlandTrampleEvent event){
        return TenderstepBehavior.protects(event.getEntity(), event.getLevel().getBlockState(event.getPos()));
    } }
