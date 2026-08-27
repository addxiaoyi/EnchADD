package net.enchadd;

import net.enchadd.enchants.FragilityEnchant;
import net.enchadd.listeners.FragilityListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FragilityBehaviorTest {

    @Test
    void fragilityScalesItemDamage() throws Exception {
        FragilityListener listener = new FragilityListener();
        FragilityEnchant config = mock(FragilityEnchant.class);
        when(config.getExtraDurabilityPerLevel()).thenReturn(0.5);
        when(config.getMaxDurabilityMultiplier()).thenReturn(2.0);

        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(org.bukkit.Material.DIAMOND_SWORD);

        PlayerItemDamageEvent event = mock(PlayerItemDamageEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getDamage()).thenReturn(4);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEnchantLevel(item, Enchantment.UNBREAKING)).thenReturn(2);
            listener.onItemDamage(event);
        }

        Mockito.verify(event).setDamage(8);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
