package net.enchadd;

import net.enchadd.enchants.MeteorEnchant;
import net.enchadd.listeners.MeteorListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class MeteorConditionBehaviorTest {

    @Test
    void meteorOnlyTriggersAfterMeaningfulFallDistance() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        MeteorListener listener = new MeteorListener();
        MeteorEnchant config = Mockito.mock(MeteorEnchant.class);
        when(config.getRequiredFallDistance()).thenReturn(1.5);
        when(config.getBonusDamagePerLevel()).thenReturn(0.9);
        when(config.getMaxBonusDamage()).thenReturn(2.7);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.SHARPNESS))).thenReturn(2);

            when(attacker.getFallDistance()).thenReturn(2.0f);
            listener.onMeleeHit(event);
            assertEquals(6.8, appliedDamage.get(), 0.0001, "falling mace hit should receive meteor bonus damage");

            appliedDamage.set(null);
            when(attacker.getFallDistance()).thenReturn(0.5f);
            listener.onMeleeHit(event);
            assertNull(appliedDamage.get(), "short hop should not trigger meteor");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
