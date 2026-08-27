package net.enchadd;

import net.enchadd.enchants.ResonanceEnchant;
import net.enchadd.listeners.ResonanceListener;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResonanceComboBehaviorTest {

    @Test
    void resonanceProcsOnRequiredComboAndResetsCounter() throws Exception {
        LivingEntity attacker = mock(LivingEntity.class);
        LivingEntity victim = mock(LivingEntity.class);

        ItemStack weapon = mock(ItemStack.class);
        EntityEquipment equipment = mock(EntityEquipment.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        ResonanceListener listener = new ResonanceListener();
        ResonanceEnchant config = mock(ResonanceEnchant.class);
        when(config.getHitsPerProcBase()).thenReturn(3);
        when(config.getHitsPerProcReductionPerLevel()).thenReturn(1);
        when(config.getMinHitsPerProc()).thenReturn(2);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(2.5);

        NamespacedKey comboKey = new NamespacedKey("enchadd", "resonance_test_combo");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "comboKey", comboKey);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(6.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        PersistentDataContainer pdc = mock(PersistentDataContainer.class);
        AtomicInteger comboState = new AtomicInteger(0);
        when(pdc.getOrDefault(comboKey, PersistentDataType.INTEGER, 0)).thenAnswer(invocation -> comboState.get());
        doAnswer(invocation -> {
            comboState.set(invocation.getArgument(2));
            return null;
        }).when(pdc).set(Mockito.eq(comboKey), Mockito.eq(PersistentDataType.INTEGER), Mockito.anyInt());

        try (var utils = Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> net.enchadd.utils.PerformanceUtils.getEnchantLevel(weapon, Enchantment.SHARPNESS)).thenReturn(2);
            utils.when(() -> net.enchadd.utils.PerformanceUtils.getPDCSafe(attacker)).thenReturn(pdc);

            listener.onMeleeHit(event);
            assertEquals(1, comboState.get(), "first hit should accumulate combo");
            assertNull(appliedDamage.get(), "first hit should not proc");

            listener.onMeleeHit(event);
            assertEquals(0, comboState.get(), "second hit should proc then reset combo");
            assertEquals(8.0, appliedDamage.get(), 0.0001, "proc hit should receive configured bonus damage");

            appliedDamage.set(null);
            listener.onMeleeHit(event);
            assertEquals(1, comboState.get(), "after reset, combo should start from one again");
            assertNull(appliedDamage.get(), "post-reset first hit should not immediately proc");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
