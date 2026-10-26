package net.enchadd.mod;

import net.enchadd.mod.common.ProjectileStatusBehavior;
import net.enchadd.mod.common.RicochetBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeProjectileStatus {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeProjectileStatus()::onJoin);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeProjectileStatus()::onDamage); }
    @SubscribeEvent public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof Projectile projectile) {
            ProjectileStatusBehavior.recordLaunch(projectile);
            if (projectile instanceof AbstractArrow arrow) RicochetBehavior.recordLaunch(arrow);
        }
    }
    @SubscribeEvent public void onDamage(LivingDamageEvent event) {
        float amount = ProjectileStatusBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount());
        event.setAmount(amount);
        ProjectileStatusBehavior.applyOnHit(event.getEntity(), event.getSource(), amount);
    }
}
