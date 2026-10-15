package net.enchadd.mod;

import net.enchadd.mod.common.ProjectileStatusBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeProjectileStatus {
    static void register() {
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, EnchADDNeoForgeProjectileStatus::onJoin);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeProjectileStatus::onDamage);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeProjectileStatus::onIncoming);
    }
    private static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof Projectile projectile) {
            ProjectileStatusBehavior.recordLaunch(projectile);
        }
    }
    private static void onDamage(LivingDamageEvent.Post event) {
        ProjectileStatusBehavior.applyOnHit(event.getEntity(), event.getSource(), event.getHealthDamage());
    }
    private static void onIncoming(LivingIncomingDamageEvent event) {
        event.setAmount(ProjectileStatusBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }
}
