package net.enchadd.mod;

import net.enchadd.mod.common.ParryBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeParry {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeParry()::onHurt);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (event.getEntity().isBlocking()) {
            ParryBehavior.arm(event.getEntity(), event.getSource(), event.getAmount());
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                    && event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
                RiposteBehavior.blocked(player, player.getUseItem(), attacker);
            }
        }
        event.setAmount(ParryBehavior.adjustCounterattack(
                event.getEntity(), event.getSource(), event.getAmount()));
    }
}
