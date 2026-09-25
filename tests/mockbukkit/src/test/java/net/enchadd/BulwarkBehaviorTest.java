package net.enchadd;

import net.enchadd.enchants.BulwarkEnchant;
import net.enchadd.listeners.BulwarkListener;
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

class BulwarkBehaviorTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void bulwarkGrantsResistanceAfterShieldBlockAndSkipsWhenShieldGateFails() throws Exception {
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        Player player = Mockito.mock(Player.class);
        when(player.addPotionEffect(Mockito.any(PotionEffect.class))).thenReturn(true);

        BulwarkListener listener = new BulwarkListener();
        BulwarkEnchant config = Mockito.mock(BulwarkEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getResistanceSecondsPerLevel()).thenReturn(2);
        when(config.getResistanceAmplifier()).thenReturn(1);
        when(config.getMaxLevel()).thenReturn(2);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.getActiveOffhandShieldLevel(player, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class),
                    Mockito.eq(80)
            )).thenReturn(false);
            utils.when(() -> PerformanceUtils.setCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class)
            )).thenAnswer(invocation -> null);

            listener.onShieldBlock(event);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.RESISTANCE
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 1
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
