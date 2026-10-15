package net.enchadd.mod;
import net.enchadd.mod.common.WingguardBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
final class EnchADDNeoForgeWingguard { static void register(){NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class,EnchADDNeoForgeWingguard::onDeath);} private static void onDeath(LivingDeathEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p&&WingguardBehavior.rescue(p,e.getSource(),Float.MAX_VALUE))e.setCanceled(true);} }
