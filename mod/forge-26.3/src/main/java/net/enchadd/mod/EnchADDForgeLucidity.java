package net.enchadd.mod;

import net.enchadd.mod.common.LucidityBehavior;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class EnchADDForgeLucidity {
    private static final Set<UUID> SUPPRESS = ConcurrentHashMap.newKeySet();

    private EnchADDForgeLucidity() {}

    static void register() { MobEffectEvent.Added.BUS.addListener(new EnchADDForgeLucidity()::onAdded); }

    @SubscribeEvent
    public void onAdded(MobEffectEvent.Added event) {
        UUID id = event.getEntity().getUUID();
        if (!SUPPRESS.remove(id)) {
            MobEffectInstance original = event.getEffectInstance();
            MobEffectInstance adjusted = LucidityBehavior.adjust(event.getEntity(), original);
            if (adjusted != original && adjusted.getDuration() < original.getDuration()) {
                event.getEntity().removeEffect(original.getEffect());
                SUPPRESS.add(id);
                event.getEntity().addEffect(adjusted);
            }
        }
    }
}
