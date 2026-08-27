package net.enchadd;

import net.enchadd.enchants.TracerEnchant;
import net.enchadd.listeners.TracerListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class TracerConditionBehaviorTest {

    @Test
    void tracerOnlyTriggersAgainstGlowingTargets() throws Exception {
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInMainHand()).thenReturn(weapon);

        Player attacker = Mockito.mock(Player.class);
        when(attacker.getEquipment()).thenReturn(equipment);

        LivingEntity victim = Mockito.mock(LivingEntity.class);

        TracerListener listener = new TracerListener();
        TracerEnchant config = Mockito.mock(TracerEnchant.class);
        when(config.getBonusDamagePerLevel()).thenReturn(0.8);
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

        when(victim.hasPotionEffect(PotionEffectType.GLOWING)).thenReturn(true);
        listener.onMeleeHit(event);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "glowing target should receive tracer bonus damage");

        appliedDamage.set(null);
        when(victim.hasPotionEffect(PotionEffectType.GLOWING)).thenReturn(false);
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "non-glowing target should not trigger tracer");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
