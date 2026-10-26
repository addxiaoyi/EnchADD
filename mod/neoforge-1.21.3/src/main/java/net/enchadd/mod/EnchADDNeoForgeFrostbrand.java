package net.enchadd.mod;
import net.enchadd.mod.common.FrostbrandBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgeFrostbrand { static void register(){NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgeFrostbrand::onDamage);} private static void onDamage(LivingIncomingDamageEvent e){FrostbrandBehavior.onHit(e.getEntity(),e.getSource(),e.getAmount());} }
