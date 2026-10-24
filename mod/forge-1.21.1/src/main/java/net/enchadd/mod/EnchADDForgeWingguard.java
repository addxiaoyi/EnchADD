package net.enchadd.mod;
import net.enchadd.mod.common.WingguardBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeWingguard { static void register(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeWingguard()::onDeath);} @SubscribeEvent public boolean onDeath(LivingDeathEvent e){return e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p&&WingguardBehavior.rescue(p,e.getSource(),Float.MAX_VALUE);} }
