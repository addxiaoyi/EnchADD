package net.enchadd;

import net.enchadd.enchants.PivotEnchant;
import net.enchadd.listeners.PivotListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PivotBehaviorTest {

    @Test
    void pivotGrantsShortSpeedAfterABlockAndSkipsWhenNotBlocking() throws Exception {
        ItemStack shield = Mockito.mock(ItemStack.class);
        when(shield.getType()).thenReturn(Material.SHIELD);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(shield);

        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        Player player = Mockito.mock(Player.class);
        when(player.isBlocking()).thenReturn(true, false);

        PivotListener listener = new PivotListener();
        PivotEnchant config = Mockito.mock(PivotEnchant.class);
        NamespacedKey key = new NamespacedKey("enchadd", "pivot_test");
        when(config.getCooldownTicks()).thenReturn(50);
        when(config.getSpeedSecondsPerLevel()).thenReturn(1);
        when(config.getSpeedAmplifier()).thenReturn(0);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(shield, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 50)).thenReturn(false);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(1, 2)).thenReturn(40);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onShieldBlock(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SPEED
                            && effect.getDuration() == 40
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(player);
            listener.onShieldBlock(event);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
