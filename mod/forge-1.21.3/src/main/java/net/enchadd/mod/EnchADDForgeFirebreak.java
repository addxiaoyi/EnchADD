package net.enchadd.mod;
import net.enchadd.mod.common.FirebreakBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeFirebreak { static void register(){ net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeFirebreak()::onHurt); } @SubscribeEvent public void onHurt(LivingHurtEvent event){event.setAmount(FirebreakBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));} }
