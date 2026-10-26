package net.enchadd.mod;
import net.enchadd.mod.common.PanicBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
final class EnchADDForgePanic { static void register(){LivingAttackEvent.BUS.addListener(new EnchADDForgePanic()::onAttack);} @SubscribeEvent public void onAttack(LivingAttackEvent e){if(e.getEntity() instanceof ServerPlayer p)PanicBehavior.maybePanic(p,e.getSource());} }
