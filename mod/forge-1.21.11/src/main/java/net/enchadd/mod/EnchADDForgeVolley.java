package net.enchadd.mod;
import net.enchadd.mod.common.VolleyBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
final class EnchADDForgeVolley { static void register(){EntityJoinLevelEvent.BUS.addListener(new EnchADDForgeVolley()::onJoin);} @SubscribeEvent public void onJoin(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel&&event.getEntity() instanceof AbstractArrow arrow)VolleyBehavior.onLaunch(arrow);} }
