package net.enchadd.mod;
import net.enchadd.mod.common.OverwhelmBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgeOverwhelm { static void register(){NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgeOverwhelm::onDamage);} private static void onDamage(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&e.getSource().getDirectEntity()==a)OverwhelmBehavior.onHit(e.getEntity(),a,e.getSource(),e.getAmount());} }
