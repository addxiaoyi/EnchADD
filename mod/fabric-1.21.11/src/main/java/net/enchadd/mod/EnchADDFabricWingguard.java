package net.enchadd.mod;
import net.enchadd.mod.common.WingguardBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
final class EnchADDFabricWingguard { static void init(){ ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->!(e instanceof ServerPlayer p&&WingguardBehavior.rescue(p,s,a))); } }
