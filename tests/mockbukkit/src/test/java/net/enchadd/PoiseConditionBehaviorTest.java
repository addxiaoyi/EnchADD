package net.enchadd;

import net.enchadd.enchants.PoiseEnchant;
import net.enchadd.listeners.PoiseListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class PoiseConditionBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void poiseOnlyTriggersWhenAttackerIsCalmAndStable() throws Exception {
        PlayerMock attacker = server.addPlayer("poise_attacker");
        PlayerMock victim = server.addPlayer("poise_victim");

        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        attacker.getInventory().setItemInMainHand(weapon);

        PoiseListener listener = new PoiseListener();
        PoiseEnchant config = Mockito.mock(PoiseEnchant.class);
        when(config.isRequireOnGround()).thenReturn(false);
        when(config.isDisableWhileSprinting()).thenReturn(true);
        when(config.getMovementThresholdSquared()).thenReturn(0.05);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(1.5);

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

        attacker.setSprinting(false);
        attacker.setVelocity(new Vector(0, 0, 0));
        listener.onMeleeHit(event);
        assertEquals(6.5, appliedDamage.get(), 0.0001, "poise should add bonus when player is calm");

        appliedDamage.set(null);
        attacker.setSprinting(true);
        attacker.setVelocity(new Vector(0, 0, 0));
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "poise should not trigger while sprinting");

        appliedDamage.set(null);
        attacker.setSprinting(false);
        attacker.setVelocity(new Vector(1, 0, 0));
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "poise should not trigger while moving too fast");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
