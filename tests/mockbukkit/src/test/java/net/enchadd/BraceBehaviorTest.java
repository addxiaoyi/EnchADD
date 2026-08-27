package net.enchadd;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.enchadd.enchants.BraceEnchant;
import net.enchadd.listeners.BraceListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class BraceBehaviorTest {

    @Test
    void braceReducesKnockbackOnlyForRaisedFrontFacingShieldBlocks() throws Exception {
        ItemStack shield = Mockito.mock(ItemStack.class);
        when(shield.getType()).thenReturn(Material.SHIELD);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(shield);

        Player player = Mockito.mock(Player.class);
        when(player.isBlocking()).thenReturn(true, true, false);
        when(player.isHandRaised()).thenReturn(true, true, false);
        when(player.getHandRaisedTime()).thenReturn(5);
        when(player.getActiveItem()).thenReturn(shield);
        Location location = new Location(null, 0.0, 64.0, 0.0);
        location.setDirection(new Vector(0.0, 0.0, 1.0));
        when(player.getLocation()).thenReturn(location);

        BraceListener listener = new BraceListener();
        BraceEnchant config = Mockito.mock(BraceEnchant.class);
        when(config.getKnockbackReductionPerLevel()).thenReturn(0.2);
        when(config.getMaxReduction()).thenReturn(0.5);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityKnockbackEvent event = Mockito.mock(EntityKnockbackEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(
                EntityKnockbackEvent.Cause.SHIELD_BLOCK,
                EntityKnockbackEvent.Cause.SHIELD_BLOCK,
                EntityKnockbackEvent.Cause.SHIELD_BLOCK,
                EntityKnockbackEvent.Cause.ENTITY_ATTACK
        );
        AtomicReference<Vector> currentKnockback = new AtomicReference<>(new Vector(1.0, 0.4, -0.2));
        when(event.getKnockback()).thenAnswer(invocation -> currentKnockback.get());

        AtomicReference<Vector> adjusted = new AtomicReference<>();
        doAnswer(invocation -> {
            adjusted.set(invocation.getArgument(0));
            return null;
        }).when(event).setKnockback(Mockito.any(Vector.class));

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(shield, Enchantment.UNBREAKING)).thenReturn(2);

            listener.onKnockback(event);
            assertEquals(0.6, adjusted.get().getX(), 0.0001);
            assertEquals(0.24, adjusted.get().getY(), 0.0001);
            assertEquals(-0.12, adjusted.get().getZ(), 0.0001);

            adjusted.set(null);
            currentKnockback.set(new Vector(1.0, 0.4, 0.0));
            Mockito.clearInvocations(event);
            listener.onKnockback(event);
            assertNull(adjusted.get(), "brace should not modify knockback for side hits that a real shield block would not catch");

            adjusted.set(null);
            currentKnockback.set(new Vector(1.0, 0.4, -0.2));
            Mockito.clearInvocations(event);
            listener.onKnockback(event);
            assertNull(adjusted.get(), "brace should not modify knockback when the shield is not raised");

            adjusted.set(null);
            currentKnockback.set(new Vector(0.0, 0.4, -1.0));
            Mockito.clearInvocations(event);
            listener.onKnockback(event);
            assertNull(adjusted.get(), "brace should ignore non-shield knockback causes");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
