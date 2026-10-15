package net.enchadd.mod;
import net.enchadd.mod.common.OverwhelmBehavior;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
final class EnchADDForgeOverwhelm { static void register(){LivingHurtEvent.BUS.addListener(new EnchADDForgeOverwhelm()::onHurt);} @SubscribeEvent public void onHurt(LivingHurtEvent e){if(e.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity a&&e.getSource().getDirectEntity()==a)OverwhelmBehavior.onHit(e.getEntity(),a,e.getSource(),e.getAmount());} }
