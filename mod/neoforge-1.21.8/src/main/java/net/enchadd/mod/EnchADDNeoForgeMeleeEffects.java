package net.enchadd.mod;
import net.enchadd.mod.common.MeleeEffectBehavior;
import net.enchadd.mod.common.SpearCombatBehavior;
import net.enchadd.mod.common.SunderBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgeMeleeEffects { static void register(){ NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgeMeleeEffects::onDamage); } private static void onDamage(LivingIncomingDamageEvent e){ e.setAmount(SunderBehavior.adjustDamage(e.getEntity(),e.getSource(),MeleeEffectBehavior.adjustDamage(e.getEntity(),e.getSource(),e.getAmount()))); if(e.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&e.getSource().getDirectEntity()==a){ SpearCombatBehavior.onHit(e.getEntity(),a,e.getSource(),e.getAmount()); MeleeEffectBehavior.maybeUpdraft(a); MeleeEffectBehavior.maybeTremor(a,e.getEntity()); } } }


