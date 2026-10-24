package net.enchadd.mod;
import net.enchadd.mod.common.HemorrhageBehavior;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeHemorrhage { static void register(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeHemorrhage()::onHurt);} @SubscribeEvent public void onHurt(LivingHurtEvent e){HemorrhageBehavior.onHit(e.getEntity(),e.getSource(),e.getAmount());} }
