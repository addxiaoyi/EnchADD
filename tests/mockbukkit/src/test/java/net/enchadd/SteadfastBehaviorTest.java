package net.enchadd;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.enchadd.enchants.SteadfastEnchant;
import net.enchadd.listeners.SteadfastListener;
import net.enchadd.listeners.support.SteadfastKnockbackSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SteadfastBehaviorTest {

    @Test
    void steadfastReducesKnockbackWhenTriggeredAndSkipsFailedRolls() {
        SteadfastEnchant config = Mockito.mock(SteadfastEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getTriggerChance()).thenReturn(0.3);
        when(config.getReductionPerLevel()).thenReturn(0.15);

        NamespacedKey key = new NamespacedKey("enchadd", "steadfast_test");
        SteadfastListener listener = new SteadfastListener(
                Enchantment.UNBREAKING,
                key,
                config,
                new SteadfastKnockbackSupport()
        );

        Player player = Mockito.mock(Player.class);
        ItemStack boots = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getBoots()).thenReturn(boots);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        EntityKnockbackEvent event = Mockito.mock(EntityKnockbackEvent.class);
        when(event.getEntity()).thenReturn(player);
        Vector baseKnockback = new Vector(1.0, 0.4, -0.2);
        when(event.getKnockback()).thenReturn(baseKnockback);

        final Vector[] adjusted = new Vector[1];
        doAnswer(invocation -> {
            adjusted[0] = invocation.getArgument(0);
            return null;
        }).when(event).setKnockback(Mockito.any(Vector.class));

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(boots, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 80)).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(0.6)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onKnockback(event);
            assertEquals(0.7, adjusted[0].getX(), 0.0001);
            assertEquals(0.28, adjusted[0].getY(), 0.0001);
            assertEquals(-0.14, adjusted[0].getZ(), 0.0001);
            verify(event).setKnockback(Mockito.any(Vector.class));

            adjusted[0] = null;
            clearInvocations(event);
            listener.onKnockback(event);
            assertNull(adjusted[0], "steadfast should not modify knockback when chance check fails");
        }
    }
}
