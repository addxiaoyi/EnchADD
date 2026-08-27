package net.enchadd;

import net.enchadd.enchants.WingguardEnchant;
import net.enchadd.listeners.WingguardListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
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

class WingguardBehaviorTest {

    @Test
    void wingguardCancelsLethalFallAndSkipsNonLethalHits() throws Exception {
        ItemStack chest = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getChestplate()).thenReturn(chest);

        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        Player player = Mockito.mock(Player.class);
        when(player.getHealth()).thenReturn(6.0, 20.0);

        WingguardListener listener = new WingguardListener();
        WingguardEnchant config = Mockito.mock(WingguardEnchant.class);
        when(config.getCooldownTicks()).thenReturn(1200);
        when(config.getTriggerChance()).thenReturn(0.45);
        when(config.getMaxTriggerChance()).thenReturn(0.9);
        when(config.getSafeSecondsPerLevel()).thenReturn(4);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageEvent lethalEvent = Mockito.mock(EntityDamageEvent.class);
        when(lethalEvent.getEntity()).thenReturn(player);
        when(lethalEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(lethalEvent.getFinalDamage()).thenReturn(8.0);

        EntityDamageEvent nonLethalEvent = Mockito.mock(EntityDamageEvent.class);
        when(nonLethalEvent.getEntity()).thenReturn(player);
        when(nonLethalEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(nonLethalEvent.getFinalDamage()).thenReturn(4.0);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(chest, Enchantment.UNBREAKING)).thenReturn(2);
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            utils.when(() -> PerformanceUtils.isOnCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class),
                    Mockito.eq(1200)
            )).thenReturn(false);
            utils.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            utils.when(() -> PerformanceUtils.calculateDurationTicksPerLevel(4, 2)).thenReturn(80);
            utils.when(() -> PerformanceUtils.setCooldown(
                    Mockito.eq(pdc),
                    Mockito.any(NamespacedKey.class)
            )).thenAnswer(invocation -> null);

            listener.onFallDamage(lethalEvent);
            verify(lethalEvent).setCancelled(true);
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.SLOW_FALLING
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.RESISTANCE
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));

            clearInvocations(player);
            listener.onFallDamage(nonLethalEvent);
            verify(nonLethalEvent, never()).setCancelled(true);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
