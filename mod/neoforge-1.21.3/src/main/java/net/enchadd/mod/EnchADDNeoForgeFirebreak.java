package net.enchadd.mod;
import net.enchadd.mod.common.FirebreakBehavior;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
final class EnchADDNeoForgeFirebreak { static void register(){NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class,EnchADDNeoForgeFirebreak::onDamage);} private static void onDamage(LivingIncomingDamageEvent e){e.setAmount(FirebreakBehavior.adjustDamage(e.getEntity(),e.getSource(),e.getAmount()));} }
