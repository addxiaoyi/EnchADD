package net.enchadd;

import net.enchadd.enchants.BackfireEnchant;
import net.enchadd.listeners.BackfireListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackfireBehaviorTest {

    @Test
    void backfireDamagesAttackerAndRespectsCooldown() throws Exception {
        Player attacker = mock(Player.class);
        EntityEquipment equipment = mock(EntityEquipment.class);
        ItemStack weapon = mock(ItemStack.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        BackfireListener listener = new BackfireListener();
        BackfireEnchant config = mock(BackfireEnchant.class);
        when(config.getCooldownTicks()).thenReturn(40);
        when(config.getTriggerChancePerLevel()).thenReturn(0.5);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getSelfDamageMultiplier()).thenReturn(0.4);

        NamespacedKey key = new NamespacedKey("enchadd", "backfire_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getFinalDamage()).thenReturn(10.0);

        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(weapon, Enchantment.SHARPNESS)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(attacker)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 40)).thenReturn(false, true);
            utils.when(() -> PerformanceUtils.rollChance(1.0)).thenReturn(true);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onHit(event);
            verify(attacker).damage(4.0);

            Mockito.clearInvocations(attacker);
            listener.onHit(event);
            assertTrue(Mockito.mockingDetails(attacker).getInvocations().isEmpty(), "cooldown should block the second backfire proc");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
