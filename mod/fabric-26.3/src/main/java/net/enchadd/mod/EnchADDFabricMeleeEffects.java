package net.enchadd.mod;
import net.enchadd.mod.common.MeleeEffectBehavior;
import net.enchadd.mod.common.BackfireBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
final class EnchADDFabricMeleeEffects { static void init(){ ServerLivingEntityEvents.AFTER_DAMAGE.register((e,s,b,t,blocked)->{ if(s.getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&s.getDirectEntity()==a){ BackfireBehavior.onAttack(a,s,t); MeleeEffectBehavior.maybeUpdraft(a); MeleeEffectBehavior.maybeTremor(a,e); } }); } }
