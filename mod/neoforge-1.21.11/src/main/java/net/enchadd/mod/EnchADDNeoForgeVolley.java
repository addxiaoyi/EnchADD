package net.enchadd.mod;
import net.enchadd.mod.common.VolleyBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
final class EnchADDNeoForgeVolley { static void register(){NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class,EnchADDNeoForgeVolley::onJoin);} private static void onJoin(EntityJoinLevelEvent event){if(event.getLevel() instanceof ServerLevel&&event.getEntity() instanceof AbstractArrow arrow)VolleyBehavior.onLaunch(arrow);} }
