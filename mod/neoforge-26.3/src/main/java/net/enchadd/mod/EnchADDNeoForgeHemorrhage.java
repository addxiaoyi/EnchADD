package net.enchadd.mod;
import net.enchadd.mod.common.HemorrhageBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgeHemorrhage { static void register(){NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgeHemorrhage::onDamage);} private static void onDamage(LivingIncomingDamageEvent e){HemorrhageBehavior.onHit(e.getEntity(),e.getSource(),e.getAmount());} }
