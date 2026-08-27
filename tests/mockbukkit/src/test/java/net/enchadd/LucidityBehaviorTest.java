package net.enchadd;

import net.enchadd.enchants.LucidityEnchant;
import net.enchadd.listeners.LucidityListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LucidityBehaviorTest {

    @Test
    void lucidityShortensBlindnessLikeEffectsOnHelmetWearer() throws Exception {
        ItemStack helmet = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getHelmet()).thenReturn(helmet);

        Player player = Mockito.mock(Player.class);

        LucidityListener listener = new LucidityListener();
        LucidityEnchant config = Mockito.mock(LucidityEnchant.class);
        when(config.getDurationReductionPerLevel()).thenReturn(0.25);
        when(config.getMaxDurationReduction()).thenReturn(0.75);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false, true);

        EntityPotionEffectEvent event = Mockito.mock(EntityPotionEffectEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getModifiedType()).thenReturn(PotionEffectType.BLINDNESS);
        when(event.getNewEffect()).thenReturn(effect);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(helmet, Enchantment.UNBREAKING)).thenReturn(2);

            listener.onPotionEffect(event);
            verify(event).setCancelled(true);
            verify(player).addPotionEffect(Mockito.argThat(adjusted ->
                    adjusted.getType() == PotionEffectType.BLINDNESS
                            && adjusted.getDuration() == 50
                            && adjusted.getAmplifier() == 0
            ), Mockito.eq(true));
        }
    }

    @Test
    void lucidityIgnoresUnsupportedPotionTypes() throws Exception {
        ItemStack helmet = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getHelmet()).thenReturn(helmet);

        Player player = Mockito.mock(Player.class);

        LucidityListener listener = new LucidityListener();
        LucidityEnchant config = Mockito.mock(LucidityEnchant.class);
        when(config.getDurationReductionPerLevel()).thenReturn(0.25);
        when(config.getMaxDurationReduction()).thenReturn(0.75);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        PotionEffect effect = new PotionEffect(PotionEffectType.POISON, 100, 0, false, false, true);

        EntityPotionEffectEvent event = Mockito.mock(EntityPotionEffectEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getModifiedType()).thenReturn(PotionEffectType.POISON);
        when(event.getNewEffect()).thenReturn(effect);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(helmet, Enchantment.UNBREAKING)).thenReturn(3);

            listener.onPotionEffect(event);
            verify(event, never()).setCancelled(true);
            verify(player, never()).addPotionEffect(Mockito.any(PotionEffect.class), Mockito.eq(true));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
