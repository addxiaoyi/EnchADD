package net.enchadd.mod;
import net.enchadd.mod.common.OverwhelmBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
final class EnchADDFabricOverwhelm { static void init(){ServerLivingEntityEvents.AFTER_DAMAGE.register((e,s,b,t,blocked)->{if(s.getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&s.getDirectEntity()==a)OverwhelmBehavior.onHit(e,a,s,t);});} }
