package net.enchadd;

import net.enchadd.enchants.PivotEnchant;
import net.enchadd.listeners.PivotListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PivotBehaviorTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pivotGrantsShortSpeedAfterABlockAndSkipsWhenNotBlocking() throws Exception {
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);

        Player player = Mockito.mock(Player.class);
        when(player.addPotionEffect(Mockito.any(PotionEffect.class))).thenReturn(true);

        PivotListener listener = new PivotListener();
        PivotEnchant config = Mockito.mock(PivotEnchant.class);
        NamespacedKey key = new NamespacedKey("enchadd", "pivot_test");
        when(config.getCooldownTicks()).thenReturn(50);
        when(config.getSpeedSecondsPerLevel()).thenReturn(1);
        when(config.getSpeedAmplifier()).thenReturn(0);
        when(config.getMaxLevel()).thenReturn(2);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.getActiveOffhandShieldLevel(player, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 50)).thenReturn(false);
            utils.when(() -> PerformanceUtils.setCooldown(pdc, key)).thenAnswer(invocation -> null);

            listener.onShieldBlock(event);
            utils.verify(() -> PerformanceUtils.setCooldown(pdc, key));
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

    @Test
    void pivotSpendsCooldownOnlyAfterSpeedIsActuallyApplied() throws Exception {
        Player player = Mockito.mock(Player.class);
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        when(player.getPotionEffect(PotionEffectType.SPEED)).thenReturn(
                new PotionEffect(PotionEffectType.SPEED, 10, 1),
                new PotionEffect(PotionEffectType.SPEED, 80, 0),
                new PotionEffect(PotionEffectType.SPEED, -1, 0),
                null);
        when(player.addPotionEffect(Mockito.any(PotionEffect.class))).thenReturn(false, true);

        PivotEnchant config = Mockito.mock(PivotEnchant.class);
        when(config.getMaxLevel()).thenReturn(2);
        when(config.getSpeedSecondsPerLevel()).thenReturn(1);
        when(config.getCooldownTicks()).thenReturn(50);
        NamespacedKey key = new NamespacedKey("enchadd", "pivot_effect_test");
        PivotListener listener = new PivotListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);
        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true);
            utils.when(() -> PerformanceUtils.getActiveOffhandShieldLevel(player, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);

            for (int attempt = 0; attempt < 3; attempt++) listener.onShieldBlock(event);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class));
            listener.onShieldBlock(event);
            verify(player).addPotionEffect(Mockito.any(PotionEffect.class));
            utils.verify(() -> PerformanceUtils.setCooldown(pdc, key), never());

            listener.onShieldBlock(event);
            utils.verify(() -> PerformanceUtils.setCooldown(pdc, key));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
