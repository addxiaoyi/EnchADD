package net.enchadd;

import net.enchadd.enchants.AfterglideEnchant;
import net.enchadd.listeners.AfterglideListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.EntityEquipment;
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

class AfterglideBehaviorTest {

    @Test
    void afterglideOnlyGrantsSlowFallingWhenGlideEndsInAir() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        Player player = Mockito.mock(Player.class);
        when(player.getEquipment()).thenReturn(equipment);

        AfterglideListener listener = new AfterglideListener();
        AfterglideEnchant config = Mockito.mock(AfterglideEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getSlowFallingSecondsPerLevel()).thenReturn(2);
        when(config.getSlowFallingAmplifier()).thenReturn(0);

        setField(listener, "config", config);
        setField(listener, "key", new NamespacedKey("enchadd", "afterglide_test"));

        EntityGlideEventBuilder builder = new EntityGlideEventBuilder();
        EntityToggleGlideEvent event = builder.mock(player, false);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.UNBREAKING))).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, new NamespacedKey("enchadd", "afterglide_test"), 100)).thenReturn(false);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(2, 2)).thenReturn(80);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, new NamespacedKey("enchadd", "afterglide_test"))).thenAnswer(invocation -> null);

            setField(listener, "enchant", Enchantment.UNBREAKING);
            when(player.isOnGround()).thenReturn(false);
            listener.onToggleGlide(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SLOW_FALLING
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(player);
            when(player.isOnGround()).thenReturn(true);
            listener.onToggleGlide(event);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class EntityGlideEventBuilder {
        EntityToggleGlideEvent mock(Player player, boolean gliding) {
            EntityToggleGlideEvent event = Mockito.mock(EntityToggleGlideEvent.class);
            when(event.getEntity()).thenReturn(player);
            when(event.isGliding()).thenReturn(gliding);
            return event;
        }
    }
}
