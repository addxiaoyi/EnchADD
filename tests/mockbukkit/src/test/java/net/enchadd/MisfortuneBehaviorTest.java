package net.enchadd;

import net.enchadd.enchants.MisfortuneEnchant;
import net.enchadd.listeners.MisfortuneListener;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MisfortuneBehaviorTest {

    @Test
    void misfortuneReducesDroppedExp() throws Exception {
        MisfortuneListener listener = new MisfortuneListener();
        MisfortuneEnchant config = mock(MisfortuneEnchant.class);
        when(config.getXpPenaltyPerLevel()).thenReturn(0.2);
        when(config.getMaxXpPenalty()).thenReturn(0.6);

        Player killer = mock(Player.class);
        EntityEquipment equipment = mock(EntityEquipment.class);
        ItemStack weapon = mock(ItemStack.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        var damageSource = TestDamageSources.directPlayerDamage(killer);
        when(event.getDamageSource()).thenReturn(damageSource);
        when(event.getDroppedExp()).thenReturn(10);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        try (var utils = Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.getEquipmentSafe(killer)).thenReturn(equipment);
            utils.when(() -> net.enchadd.utils.PerformanceUtils.getEnchantLevel(weapon, Enchantment.SHARPNESS)).thenReturn(2);

            listener.onEntityDeath(event);

            Mockito.verify(event).setDroppedExp(6);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
