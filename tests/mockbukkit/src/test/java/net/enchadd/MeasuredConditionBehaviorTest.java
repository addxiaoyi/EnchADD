package net.enchadd;

import net.enchadd.enchants.MeasuredEnchant;
import net.enchadd.listeners.MeasuredListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class MeasuredConditionBehaviorTest {

    @Test
    void measuredOnlyTriggersOnChargedMeleeHits() throws Exception {
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        Player attacker = Mockito.mock(Player.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        MeasuredListener listener = new MeasuredListener();
        MeasuredEnchant config = Mockito.mock(MeasuredEnchant.class);
        when(config.getRequiredAttackCooldown()).thenReturn(0.9);
        when(config.getBonusDamagePerLevel()).thenReturn(0.9);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        when(attacker.getAttackCooldown()).thenReturn(1.0f);
        listener.onMeleeHit(event);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "charged swing should receive bonus damage");

        appliedDamage.set(null);
        when(attacker.getAttackCooldown()).thenReturn(0.4f);
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "uncharged swing should not trigger measured bonus");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
