package net.enchadd.mod;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.common.NeoForge;
final class EnchADDNeoForgeMobExperience { static void register(){NeoForge.EVENT_BUS.addListener(LivingExperienceDropEvent.class,EnchADDNeoForgeMobExperience::onDrop);} private static void onDrop(LivingExperienceDropEvent e){Player p=e.getAttackingPlayer();if(p!=null)e.setDroppedExperience(MobExperienceBehavior.adjustDeathExperience(e.getEntity(),p,e.getDroppedExperience()));} }
