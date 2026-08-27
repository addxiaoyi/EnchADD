package net.enchadd;

import net.enchadd.enchants.BreakguardEnchant;
import net.enchadd.listeners.BreakguardListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;
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

class BreakguardConditionBehaviorTest {

    @Test
    void breakguardOnlyTriggersAgainstBlockingTargets() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player attacker = Mockito.mock(Player.class);
        when(attacker.getEquipment()).thenReturn(equipment);
        Location frontLocation = new Location(Mockito.mock(World.class), 0.0, 64.0, 2.0);
        Location sideLocation = new Location(Mockito.mock(World.class), 2.0, 64.0, 0.0);
        when(attacker.getLocation()).thenReturn(frontLocation, frontLocation, sideLocation, sideLocation);

        Player victim = Mockito.mock(Player.class);
        Location victimLocation = new Location(Mockito.mock(World.class), 0.0, 64.0, 0.0);
        victimLocation.setDirection(new Vector(0.0, 0.0, 1.0));
        when(victim.getLocation()).thenReturn(victimLocation);
        when(victim.getHandRaisedTime()).thenReturn(6, 6, 0);
        when(victim.isHandRaised()).thenReturn(true, true, false);
        ItemStack raisedShield = Mockito.mock(ItemStack.class);
        when(raisedShield.getType()).thenReturn(Material.SHIELD);
        when(victim.getActiveItem()).thenReturn(raisedShield);

        BreakguardListener listener = new BreakguardListener();
        BreakguardEnchant config = Mockito.mock(BreakguardEnchant.class);
        when(config.getBonusDamagePerLevel()).thenReturn(0.9);
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

            when(victim.isBlocking()).thenReturn(true, false, true);
            listener.onMeleeHit(event);
            assertEquals(6.8, appliedDamage.get(), 0.0001, "blocking target should receive breakguard bonus damage");

            appliedDamage.set(null);
            when(victim.isBlocking()).thenReturn(false);
            listener.onMeleeHit(event);
            assertNull(appliedDamage.get(), "non-blocking target should not trigger breakguard");

            appliedDamage.set(null);
            listener.onMeleeHit(event);
            assertNull(appliedDamage.get(), "holding a shield without a real block should not trigger breakguard");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
