package net.enchadd;

import net.enchadd.enchants.SkimEnchant;
import net.enchadd.listeners.SkimListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkimBehaviorTest {

    @Test
    void skimSoftensWallImpactsAndCanNullifySmallScrapes() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player player = Mockito.mock(Player.class);
        when(player.getEquipment()).thenReturn(equipment);

        SkimListener listener = new SkimListener();
        SkimEnchant config = Mockito.mock(SkimEnchant.class);
        when(config.getFlatDamageReductionPerLevel()).thenReturn(1.0);
        when(config.getMaxDamageReduction()).thenReturn(3.0);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageEvent event = Mockito.mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FLY_INTO_WALL);
        when(event.getDamage()).thenReturn(5.0);

        AtomicReference<Double> adjustedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            adjustedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.UNBREAKING))).thenReturn(2);

            listener.onWallImpact(event);
            assertEquals(3.0, adjustedDamage.get(), 0.0001, "skim should subtract a flat amount from wall damage");

            adjustedDamage.set(null);
            when(event.getDamage()).thenReturn(2.0);
            listener.onWallImpact(event);
            verify(event).setCancelled(true);
        }
    }

    @Test
    void skimIgnoresNonWallDamage() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player player = Mockito.mock(Player.class);
        when(player.getEquipment()).thenReturn(equipment);

        SkimListener listener = new SkimListener();
        SkimEnchant config = Mockito.mock(SkimEnchant.class);
        when(config.getFlatDamageReductionPerLevel()).thenReturn(1.0);
        when(config.getMaxDamageReduction()).thenReturn(3.0);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageEvent event = Mockito.mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);

        listener.onWallImpact(event);
        verify(event, never()).setDamage(Mockito.anyDouble());
        verify(event, never()).setCancelled(true);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
