package net.enchadd;

import net.enchadd.enchants.AirbagEnchant;
import net.enchadd.listeners.AirbagListener;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.ParticleQueue;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AirbagBehaviorTest {

    @Test
    void airbagReducesFallImpactAndSkipsNonCushionDamageCauses() throws Exception {
        ItemStack chest = Mockito.mock(ItemStack.class);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getArmorContents()).thenReturn(new ItemStack[]{chest});

        World world = Mockito.mock(World.class);
        LivingEntity livingEntity = Mockito.mock(LivingEntity.class);
        when(livingEntity.getEquipment()).thenReturn(equipment);
        when(livingEntity.getWorld()).thenReturn(world);
        when(livingEntity.getLocation()).thenAnswer(invocation -> new Location(world, 0.0, 64.0, 0.0));

        AirbagListener listener = new AirbagListener();
        AirbagEnchant config = Mockito.mock(AirbagEnchant.class);
        when(config.getDamageReductionPerLevel()).thenReturn(0.2);

        setField(listener, "airbag", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageEvent fallEvent = Mockito.mock(EntityDamageEvent.class);
        when(fallEvent.getEntity()).thenReturn(livingEntity);
        when(fallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(fallEvent.getDamage()).thenReturn(10.0);

        AtomicReference<Double> reducedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            reducedDamage.set(invocation.getArgument(0));
            return null;
        }).when(fallEvent).setDamage(anyDouble());

        try (MockedStatic<EnchantCache> enchantCache = Mockito.mockStatic(EnchantCache.class)) {
            enchantCache.when(() -> EnchantCache.getLevel(chest, Enchantment.UNBREAKING)).thenReturn(2);

            listener.onCushionedImpact(fallEvent);
            assertEquals(6.0, reducedDamage.get(), 0.0001, "airbag should reduce fall damage based on total armor enchant levels");
            verify(world).playSound(Mockito.any(Location.class), Mockito.eq(Sound.BLOCK_WOOL_BREAK), Mockito.eq(1f), Mockito.eq(0.5f));

            EntityDamageEvent otherCauseEvent = Mockito.mock(EntityDamageEvent.class);
            when(otherCauseEvent.getEntity()).thenReturn(livingEntity);
            when(otherCauseEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            when(otherCauseEvent.getDamage()).thenReturn(10.0);

            reducedDamage.set(null);
            clearInvocations(world);
            listener.onCushionedImpact(otherCauseEvent);
            assertNull(reducedDamage.get(), "airbag should ignore non-fall and non-wall-impact damage");
            verify(world, never()).playSound(Mockito.any(Location.class), Mockito.eq(Sound.BLOCK_WOOL_BREAK), Mockito.eq(1f), Mockito.eq(0.5f));
        }

        ParticleQueue.stop();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
