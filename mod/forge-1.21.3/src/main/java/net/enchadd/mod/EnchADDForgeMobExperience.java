package net.enchadd.mod;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
final class EnchADDForgeMobExperience { static void register(){net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeMobExperience()::onDrop);} @SubscribeEvent public void onDrop(LivingExperienceDropEvent e){if(e.getAttackingPlayer()!=null)e.setDroppedExperience(MobExperienceBehavior.adjustDeathExperience(e.getEntity(),e.getAttackingPlayer(),e.getDroppedExperience()));} }
