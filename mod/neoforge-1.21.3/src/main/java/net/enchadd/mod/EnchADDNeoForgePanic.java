package net.enchadd.mod;
import net.enchadd.mod.common.PanicBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgePanic { static void register(){NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgePanic::onDamage);} private static void onDamage(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p)PanicBehavior.maybePanic(p,e.getSource());} }
