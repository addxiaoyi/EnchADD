package net.enchadd.mod;
import net.enchadd.mod.common.PanicBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
final class EnchADDFabricPanic { static void init(){ServerLivingEntityEvents.ALLOW_DAMAGE.register((e,s,a)->{if(e instanceof ServerPlayer p)PanicBehavior.maybePanic(p,s);return true;});} }
