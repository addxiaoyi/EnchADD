package net.enchadd;

import net.enchadd.enchants.UpdraftEnchant;
import net.enchadd.listeners.UpdraftListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdraftBehaviorTest {

    @Test
    void updraftOnlyGrantsSlowFallingWhenAttackerIsAirborne() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        UpdraftListener listener = new UpdraftListener();
        UpdraftEnchant config = Mockito.mock(UpdraftEnchant.class);
        when(config.getSlowFallingSecondsPerLevel()).thenReturn(1);
        when(config.getSlowFallingAmplifier()).thenReturn(0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.SHARPNESS))).thenReturn(2);

            when(attacker.isOnGround()).thenReturn(false);
            listener.onMeleeHit(event);
            verify(attacker).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SLOW_FALLING
                            && effect.getDuration() == 40
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(attacker);
            when(attacker.isOnGround()).thenReturn(true);
            listener.onMeleeHit(event);
            verify(attacker, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
