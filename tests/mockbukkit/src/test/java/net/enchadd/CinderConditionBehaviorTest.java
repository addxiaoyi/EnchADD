package net.enchadd;

import net.enchadd.enchants.CinderEnchant;
import net.enchadd.listeners.CinderListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CinderConditionBehaviorTest {

    @Test
    void cinderOnlyTriggersAgainstBurningTargets() throws Exception {
        ItemStack weapon = mock(ItemStack.class);
        EntityEquipment equipment = mock(EntityEquipment.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        Player attacker = mock(Player.class);
        LivingEntity victim = mock(LivingEntity.class);

        CinderListener listener = new CinderListener();
        CinderEnchant config = mock(CinderEnchant.class);
        when(config.getRequiredFireTicks()).thenReturn(20);
        when(config.getBonusDamagePerLevel()).thenReturn(0.8);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(weapon, Enchantment.SHARPNESS)).thenReturn(3);

            when(victim.getFireTicks()).thenReturn(60);
            listener.onMeleeHit(event);
            assertEquals(7.0, appliedDamage.get(), 0.0001, "burning target should receive cinder bonus damage");

            appliedDamage.set(null);
            when(victim.getFireTicks()).thenReturn(0);
            listener.onMeleeHit(event);
            assertNull(appliedDamage.get(), "non-burning target should not trigger cinder bonus");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
