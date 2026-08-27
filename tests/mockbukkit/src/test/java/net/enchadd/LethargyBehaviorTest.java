package net.enchadd;

import net.enchadd.enchants.LethargyEnchant;
import net.enchadd.listeners.LethargyListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LethargyBehaviorTest {

    @Test
    void lethargySlowsSprintAndRespectsCooldown() throws Exception {
        Player player = mock(Player.class);
        PlayerToggleSprintEvent event = mock(PlayerToggleSprintEvent.class);
        when(event.isSprinting()).thenReturn(true);
        when(event.getPlayer()).thenReturn(player);

        EntityEquipment equipment = mock(EntityEquipment.class);
        ItemStack boots = mock(ItemStack.class);
        when(equipment.getBoots()).thenReturn(boots);

        LethargyListener listener = new LethargyListener();
        LethargyEnchant config = mock(LethargyEnchant.class);
        when(config.getCooldownTicks()).thenReturn(60);
        when(config.getSlowSecondsPerLevel()).thenReturn(2);
        when(config.getSlowAmplifier()).thenReturn(0);

        NamespacedKey key = new NamespacedKey("enchadd", "lethargy_test");
        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);
        setField(listener, "key", key);

        PersistentDataContainer pdc = mock(PersistentDataContainer.class);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(boots, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 60)).thenReturn(false, true);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(2, 2)).thenReturn(80);

            listener.onSprintToggle(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SLOWNESS
                            && effect.getDuration() == 80
            ));

            Mockito.clearInvocations(player);
            listener.onSprintToggle(event);
            assertTrue(Mockito.mockingDetails(player).getInvocations().isEmpty(), "cooldown should block the second lethargy proc");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
