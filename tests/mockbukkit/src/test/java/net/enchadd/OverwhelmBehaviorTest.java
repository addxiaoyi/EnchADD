package net.enchadd;

import net.enchadd.enchants.OverwhelmEnchant;
import net.enchadd.listeners.OverwhelmListener;
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

class OverwhelmBehaviorTest {

    @Test
    void overwhelmOnlyWeakensGroundedTargets() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        OverwhelmListener listener = new OverwhelmListener();
        OverwhelmEnchant config = Mockito.mock(OverwhelmEnchant.class);
        when(config.getWeaknessSecondsPerLevel()).thenReturn(1);
        when(config.getWeaknessAmplifier()).thenReturn(0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.SHARPNESS))).thenReturn(2);

            when(victim.isOnGround()).thenReturn(true);
            listener.onMeleeHit(event);
            verify(victim).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.WEAKNESS
                            && effect.getDuration() == 40
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(victim);
            when(victim.isOnGround()).thenReturn(false);
            listener.onMeleeHit(event);
            verify(victim, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
