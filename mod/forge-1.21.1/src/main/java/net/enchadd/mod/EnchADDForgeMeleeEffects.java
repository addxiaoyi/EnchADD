package net.enchadd.mod;
import net.enchadd.mod.common.MeleeEffectBehavior;
import net.enchadd.mod.common.SpearCombatBehavior;
import net.enchadd.mod.common.SunderBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeMeleeEffects { static void register(){ net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeMeleeEffects()::onHurt); } @SubscribeEvent public void onHurt(LivingHurtEvent e){ e.setAmount(SunderBehavior.adjustDamage(e.getEntity(),e.getSource(),MeleeEffectBehavior.adjustDamage(e.getEntity(),e.getSource(),e.getAmount()))); if(e.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&e.getSource().getDirectEntity()==a){ SpearCombatBehavior.onHit(e.getEntity(),a,e.getSource(),e.getAmount()); MeleeEffectBehavior.maybeUpdraft(a); MeleeEffectBehavior.maybeTremor(a,e.getEntity()); } } }
