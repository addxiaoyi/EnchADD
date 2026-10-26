package net.enchadd.mod;
import net.enchadd.mod.common.FrostbrandBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
final class EnchADDFabricFrostbrand { static void init(){ ServerLivingEntityEvents.AFTER_DAMAGE.register((e,s,b,t,blocked)->FrostbrandBehavior.onHit(e,s,t)); } }
