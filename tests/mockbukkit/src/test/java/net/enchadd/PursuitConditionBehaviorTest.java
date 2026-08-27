package net.enchadd;

import net.enchadd.enchants.PursuitEnchant;
import net.enchadd.listeners.PursuitListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

class PursuitConditionBehaviorTest {

    @Test
    void pursuitOnlyTriggersAgainstSprintingTargets() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player attacker = Mockito.mock(Player.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        Player victim = Mockito.mock(Player.class);

        PursuitListener listener = new PursuitListener();
        PursuitEnchant config = Mockito.mock(PursuitEnchant.class);
        when(config.getBonusDamagePerLevel()).thenReturn(0.8);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

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

            when(victim.isSprinting()).thenReturn(true);
            listener.onMeleeHit(event);
            assertEquals(6.6, appliedDamage.get(), 0.0001, "sprinting target should receive pursuit bonus damage");

            appliedDamage.set(null);
            when(victim.isSprinting()).thenReturn(false);
            listener.onMeleeHit(event);
            assertNull(appliedDamage.get(), "non-sprinting target should not trigger pursuit");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
