package net.enchadd.mod;
import net.enchadd.mod.common.FrostbrandBehavior;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeFrostbrand { static void register(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeFrostbrand()::onHurt);} @SubscribeEvent public void onHurt(LivingHurtEvent e){FrostbrandBehavior.onHit(e.getEntity(),e.getSource(),e.getAmount());} }
