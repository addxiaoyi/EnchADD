package net.enchadd;

import net.enchadd.enchants.RetchingEnchant;
import net.enchadd.listeners.RetchingListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetchingCurseBehaviorTest {

    @Test
    void retchingReducesFoodGainAndAppliesNauseaOnEatingOnly() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player player = Mockito.mock(Player.class);
        when(player.getEquipment()).thenReturn(equipment);
        when(player.getFoodLevel()).thenReturn(12);

        RetchingListener listener = new RetchingListener();
        RetchingEnchant config = Mockito.mock(RetchingEnchant.class);
        when(config.getFoodGainReductionPerLevel()).thenReturn(1);
        when(config.getMaxFoodGainReduction()).thenReturn(3);
        when(config.getNauseaSecondsPerLevel()).thenReturn(2);
        when(config.getNauseaAmplifier()).thenReturn(0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        FoodLevelChangeEvent event = Mockito.mock(FoodLevelChangeEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getFoodLevel()).thenReturn(16);

        AtomicInteger adjustedFood = new AtomicInteger(-1);
        Mockito.doAnswer(invocation -> {
            adjustedFood.set(invocation.getArgument(0));
            return null;
        }).when(event).setFoodLevel(Mockito.anyInt());

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.SHARPNESS))).thenReturn(2);

            listener.onFoodLevelChange(event);
            assertEquals(14, adjustedFood.get(), "retching should reduce part of the restored food");
            verify(player).addPotionEffect(Mockito.argThat(effect ->
                    effect.getType() == PotionEffectType.NAUSEA
                            && effect.getDuration() == 80
                            && effect.getAmplifier() == 0
            ));

            adjustedFood.set(-1);
            when(event.getFoodLevel()).thenReturn(11);
            listener.onFoodLevelChange(event);
            assertEquals(-1, adjustedFood.get(), "food loss should not be altered by retching");
            verify(player, never()).addPotionEffect(Mockito.argThat(PotionEffect::isInfinite));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
