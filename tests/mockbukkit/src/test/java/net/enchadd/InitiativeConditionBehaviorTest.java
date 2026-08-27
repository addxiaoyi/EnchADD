package net.enchadd;

import net.enchadd.enchants.InitiativeEnchant;
import net.enchadd.listeners.InitiativeListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
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

class InitiativeConditionBehaviorTest {

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
    void initiativeOnlyTriggersAgainstHealthyTargetsAtFightStart() throws Exception {
        PlayerMock attacker = server.addPlayer("initiative_attacker");
        PlayerMock victim = server.addPlayer("initiative_victim");

        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        attacker.getInventory().setItemInMainHand(weapon);

        InitiativeListener listener = new InitiativeListener();
        InitiativeEnchant config = Mockito.mock(InitiativeEnchant.class);
        when(config.getRequiredTargetHealthFraction()).thenReturn(0.9);
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

        victim.setHealth(20.0);
        listener.onMeleeHit(event);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "healthy target should receive initiative opener bonus");

        appliedDamage.set(null);
        victim.setHealth(15.0);
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "already-wounded target should not trigger initiative bonus");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
