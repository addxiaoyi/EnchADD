package net.enchadd.mod;
import net.enchadd.mod.common.HemorrhageBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
final class EnchADDFabricHemorrhage { static void init(){ServerLivingEntityEvents.AFTER_DAMAGE.register((e,s,b,t,blocked)->HemorrhageBehavior.onHit(e,s,t));} }
