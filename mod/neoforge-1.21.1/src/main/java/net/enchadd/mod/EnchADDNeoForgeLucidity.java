package net.enchadd.mod;

import net.enchadd.mod.common.LucidityBehavior;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class EnchADDNeoForgeLucidity {
    private static final Set<UUID> SUPPRESS = ConcurrentHashMap.newKeySet();

    private EnchADDNeoForgeLucidity() {}

    static void register() { NeoForge.EVENT_BUS.addListener(MobEffectEvent.Added.class, EnchADDNeoForgeLucidity::onAdded); }

    private static void onAdded(MobEffectEvent.Added event) {
        UUID id = event.getEntity().getUUID();
        if (SUPPRESS.remove(id)) return;
        MobEffectInstance original = event.getEffectInstance();
        MobEffectInstance adjusted = LucidityBehavior.adjust(event.getEntity(), original);
        if (adjusted != original && adjusted.getDuration() < original.getDuration()) {
            event.getEntity().removeEffect(original.getEffect());
            SUPPRESS.add(id);
            event.getEntity().addEffect(adjusted);
        }
    }
}
