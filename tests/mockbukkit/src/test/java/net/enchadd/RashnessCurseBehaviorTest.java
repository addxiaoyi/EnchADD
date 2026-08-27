package net.enchadd;

import net.enchadd.enchants.RashnessEnchant;
import net.enchadd.listeners.RashnessListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RashnessCurseBehaviorTest {

    @Test
    void rashnessPunishesHastyAttacksOnly() throws Exception {
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);

        Player attacker = Mockito.mock(Player.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        RashnessListener listener = new RashnessListener();
        RashnessEnchant config = Mockito.mock(RashnessEnchant.class);
        when(config.getRequiredAttackCooldown()).thenReturn(0.85);
        when(config.getSelfDamagePerLevel()).thenReturn(0.6);
        when(config.getMaxSelfDamage()).thenReturn(1.5);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(attacker)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(Mockito.any(), Mockito.eq(Enchantment.SHARPNESS))).thenReturn(2);

            when(attacker.getAttackCooldown()).thenReturn(0.5f);
            listener.onMeleeHit(event);
            verify(attacker).damage(1.2);

            Mockito.clearInvocations(attacker);
            when(attacker.getAttackCooldown()).thenReturn(1.0f);
            listener.onMeleeHit(event);
            verify(attacker, never()).damage(Mockito.anyDouble());
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
