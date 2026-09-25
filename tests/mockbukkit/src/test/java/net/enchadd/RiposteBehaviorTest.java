package net.enchadd;

import net.enchadd.enchants.RiposteEnchant;
import net.enchadd.listeners.RiposteListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
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

class RiposteBehaviorTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void riposteAppliesWeaknessOnSuccessfulBlockAndRespectsChanceGate() throws Exception {
        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        Player victim = Mockito.mock(Player.class);
        LivingEntity attacker = Mockito.mock(LivingEntity.class);
        when(attacker.addPotionEffect(Mockito.any(PotionEffect.class))).thenReturn(true);

        RiposteListener listener = new RiposteListener();
        RiposteEnchant config = Mockito.mock(RiposteEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(0.5);
        when(config.getWeaknessSecondsPerLevel()).thenReturn(2);
        when(config.getMaxLevel()).thenReturn(2);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(attacker);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.isSuccessfulShieldBlock(victim, event)).thenReturn(true, true);
            utils.when(() -> PerformanceUtils.getActiveOffhandShieldLevel(victim, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(victim)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class),
                    Mockito.eq(100)
            )).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true, false);
            utils.when(() -> PerformanceUtils.setCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class)
            )).thenAnswer(invocation -> null);

            listener.onBlocked(event);
            verify(attacker).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.WEAKNESS
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(attacker);
            listener.onBlocked(event);
            verify(attacker, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
